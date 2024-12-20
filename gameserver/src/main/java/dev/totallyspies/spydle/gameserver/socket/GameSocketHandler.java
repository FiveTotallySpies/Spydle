package dev.totallyspies.spydle.gameserver.socket;

import dev.totallyspies.spydle.gameserver.session.ClientSessionValidator;
import dev.totallyspies.spydle.gameserver.session.SessionCloseEvent;
import dev.totallyspies.spydle.gameserver.session.SessionOpenEvent;
import dev.totallyspies.spydle.gameserver.storage.GameServerStorage;
import dev.totallyspies.spydle.shared.SharedConstants;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import dev.totallyspies.spydle.shared.proto.messages.CbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.infumia.agones4j.Agones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

@Component
public class GameSocketHandler extends BinaryWebSocketHandler {

  private final Logger logger = LoggerFactory.getLogger(GameSocketHandler.class);

  private final GameServerStorage storage;
  private final ClientSessionValidator sessionValidator;
  private final SbMessageListenerProcessor annotationProcessor;
  private final ApplicationEventPublisher publisher;
  private final GameServer currentGameServer;

  private final boolean agonesEnabled;
  @Nullable private final Agones agones; // Is set to null if agones is not enabled

  private final Map<ClientSession, WebSocketSession> sessions =
      Collections.synchronizedMap(new LinkedHashMap<>());

  public GameSocketHandler(
      GameServerStorage storage,
      ClientSessionValidator sessionValidator,
      SbMessageListenerProcessor annotationProcessor,
      ApplicationEventPublisher publisher,
      GameServer currentGameServer,
      @Value("${agones.enabled}") boolean agonesEnabled,
      @Autowired(required = false) Agones agones) {
    this.storage = storage;
    this.sessionValidator = sessionValidator;
    this.annotationProcessor = annotationProcessor;
    this.publisher = publisher;
    this.currentGameServer = currentGameServer;
    this.agonesEnabled = agonesEnabled;
    this.agones = agones;
  }

  @Nullable
  private static String getHeader(WebSocketSession session, String headerName) {
    List<String> headerValues = session.getHandshakeHeaders().get(headerName);
    if (headerValues == null || headerValues.isEmpty()) {
      return null;
    }
    return headerValues.get(0);
  }

  public List<ClientSession> getSessions() {
    return new LinkedList<>(sessions.keySet());
  }

  @Override
  protected void handleBinaryMessage(WebSocketSession socketSession, BinaryMessage message) {
    String rawClientId = getHeader(socketSession, SharedConstants.CLIENT_ID_HTTP_HEADER);
    try {
      UUID clientId = sessionValidator.parseClientId(rawClientId);
      // Validate session has clientId
      if (clientId == null) {
        logger.warn(
            "Received message on session {} without header {}",
            rawClientId,
            SharedConstants.CLIENT_ID_HTTP_HEADER);
        return;
      }

      String clientName = getHeader(socketSession, SharedConstants.CLIENT_NAME_HTTP_HEADER);
      if (clientName == null) {
        logger.warn(
            "Received message on session {} without header {}",
            rawClientId,
            SharedConstants.CLIENT_NAME_HTTP_HEADER);
        return;
      }

      // Validate that session is allowed to communicate with this gameserver
      if (!sessionValidator.validateClientSession(clientId, clientName)) {
        logger.warn("Received message from unconfirmed session {}", rawClientId);
        socketSession.close(
            new CloseStatus(
                CloseStatus.NOT_ACCEPTABLE.getCode(),
                "Message from unconfirmed session client ID!"));
        return;
      }

      // Deserialize packet using protobuf
      byte[] payload = message.getPayload().array();
      SbMessage sbMessage = SbMessage.parseFrom(payload);

      // Execute
      annotationProcessor.getHandler().execute(sbMessage, clientId);
    } catch (Exception exception) {
      logger.error(
          "FATAL: Failed to handle incoming message from client {}", rawClientId, exception);
    }
  }

  public void sendCbMessage(UUID clientId, CbMessage message) {
    ClientSession targetSession = getSessionFromClientId(clientId);
    if (targetSession == null) {
      throw new IllegalArgumentException("Cannot send message to invalid client " + clientId);
    }
    sendCbMessage(targetSession, message);
  }

  public void sendCbMessage(ClientSession targetSession, CbMessage message) {
    try {
      WebSocketSession session = sessions.get(targetSession);
      synchronized (session) {
        byte[] messageBytes = message.toByteArray();
        session.sendMessage(new BinaryMessage(messageBytes));
        logger.debug(
            "Sending client {} message of type {}",
            targetSession.getClientId().toString(),
            message.getPayloadCase().name());
      }
    } catch (Exception exception) {
      logger.error(
          "FATAL: Failed to send client {} packet of type {}",
          targetSession.getClientId().toString(),
          message.getPayloadCase().name(),
          exception);
    }
  }

  public void broadcastCbMessage(CbMessage message) {
    for (ClientSession session : this.getSessions()) {
      this.sendCbMessage(session, message);
    }
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession socketSession) throws Exception {
    // Validate that the client has been assigned to us
    String rawClientId = getHeader(socketSession, SharedConstants.CLIENT_ID_HTTP_HEADER);
    UUID clientId = sessionValidator.parseClientId(rawClientId);
    String clientName = getHeader(socketSession, SharedConstants.CLIENT_NAME_HTTP_HEADER);
    if (clientName == null) {
      logger.warn("Client attempted to open session {} with no name, closing...", rawClientId);
      socketSession.close(
          new CloseStatus(CloseStatus.NOT_ACCEPTABLE.getCode(), "No name provided!"));
      return;
    }
    if (clientId == null || !sessionValidator.validateClientSession(clientId, clientName)) {
      logger.warn("Client attempted to open unconfirmed session {}, closing...", rawClientId);
      socketSession.close(
          new CloseStatus(CloseStatus.NOT_ACCEPTABLE.getCode(), "Unconfirmed session client ID!"));
      return;
    }
    if (hasSessionWithPlayerName(clientName)) {
      logger.warn(
          "Client attempted to open session {} but their name already exists, closing...",
          rawClientId);
      socketSession.close(
          new CloseStatus(CloseStatus.NOT_ACCEPTABLE.getCode(), "Name already exists!"));
      return;
    }
    ClientSession storedSession = storage.getClientSession(clientId);
    if (storedSession == null) {
      logger.warn(
          "Client attempted to open session {} with no stored client session for this UUID, closing...",
          rawClientId);
      socketSession.close(
          new CloseStatus(CloseStatus.NOT_ACCEPTABLE.getCode(), "Unknown client session!"));
      return;
    }
    if (storedSession.getState() != ClientSession.State.ASSIGNED) {
      logger.warn(
          "Client attempt to open session {} but they are already connected, closing...",
          rawClientId);
      socketSession.close(
          new CloseStatus(CloseStatus.NOT_ACCEPTABLE.getCode(), "Already connected!"));
      return;
    }
    // Update client session to CONNECTED
    storedSession.setState(ClientSession.State.CONNECTED);
    storage.storeClientSession(storedSession);

    sessions.put(storedSession, socketSession);
    logger.info("Initiated connection with client {} and name {}", clientId, clientName);

    // We need to wait until the session state is OPEN before we fire our SessionOpenEvent
    // This is because some event listeners might want to immediately send the client a message

    AtomicBoolean socketOpened = new AtomicBoolean(false);
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    // Poll the session state periodically until it is ready
    executor.scheduleAtFixedRate(
        () -> {
          try {
            if (socketSession.isOpen()) {
              // Publish event
              socketOpened.set(true);
              logger.info("Socket for client {} is open and healthy", rawClientId);
              publisher.publishEvent(new SessionOpenEvent(this, storedSession, socketSession));
              executor.shutdown(); // Stop polling once the message is sent
            }
          } catch (Exception exception) {
            logger.error(
                "FATAL: Failed to wait for client session {} to open, closing...",
                rawClientId,
                exception);
            try {
              socketSession.close();
            } catch (Exception closeException) {
              logger.error("FATAL: Failed to close client session {}", rawClientId, closeException);
            } finally {
              executor.shutdown();
            }
          }
        },
        0,
        10,
        TimeUnit.MILLISECONDS); // Check every 10 milliseconds

    // Set a timeout
    executor.schedule(
        () -> {
          if (!socketOpened.get()) {
            logger.error("FATAL: Session {} opening timed-out, closing", rawClientId);
            try {
              socketSession.close();
            } catch (Exception closeException) {
              logger.error("FATAL: Failed to close client session {}", rawClientId, closeException);
            } finally {
              executor.shutdown(); // Stop polling
            }
          }
        },
        5000,
        TimeUnit.MILLISECONDS);

    logger.debug("Registered scheduler for socket for client {}", rawClientId);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession socketSession, CloseStatus status) {
    // Validate that the client had a connection with us before deleting the socketSession
    String rawClientId = getHeader(socketSession, SharedConstants.CLIENT_ID_HTTP_HEADER);
    UUID clientId = sessionValidator.parseClientId(rawClientId);
    String clientName = getHeader(socketSession, SharedConstants.CLIENT_NAME_HTTP_HEADER);
    if (clientName == null) {
      logger.warn("Client {} closed with no name", clientId);
    } else {
      if (clientId != null && sessionValidator.validateClientSession(clientId, clientName)) {
        storage.deleteClientSession(clientId);
      }
    }

    if (clientId != null) {
      ClientSession session = getSessionFromClientId(clientId);
      if (session != null) {
        sessions.remove(session);
        publisher.publishEvent(new SessionCloseEvent(this, session, socketSession));
      }
    }
    logger.info("Closed connection with client {} for reason {}", rawClientId, status);

    // Close server if everyone has left!
    // Check that we are either PLAYING or WAITING
    if (agonesEnabled
        && currentGameServer.getState() != GameServer.State.READY
        && sessions.isEmpty()
        && agones != null) {
      logger.info("All players left! Shutting down...");
      agones.shutdown(); // Stop server
    }
  }

  public void closeAllSessions(CloseStatus status) {
    synchronized (sessions) {
      // New list to avoid CME
      for (WebSocketSession session : new LinkedList<>(sessions.values())) {
        try {
          session.close(status);
        } catch (Exception exception) {
          logger.error("FATAL: Failed to close session {}", session, exception);
        }
      }
    }
  }

  @Nullable
  public ClientSession getSessionFromClientId(UUID clientId) {
    return sessions.keySet().stream()
        .filter(session -> session.getClientId().equals(clientId))
        .findAny()
        .orElse(null);
  }

  public boolean hasSessionWithPlayerName(String name) {
    return sessions.keySet().stream()
        .anyMatch(session -> session.getPlayerName().equalsIgnoreCase(name));
  }

  protected Map<ClientSession, WebSocketSession> getSessionsMap() {
    return sessions;
  }
}
