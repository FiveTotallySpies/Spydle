package dev.totallyspies.spydle.frontend.client;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.totallyspies.spydle.frontend.client.message.CbMessageListenerProcessor;
import dev.totallyspies.spydle.shared.SharedConstants;
import dev.totallyspies.spydle.shared.proto.messages.CbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

public class ClientSocketHandler extends BinaryWebSocketHandler {

  private final Logger logger = LoggerFactory.getLogger(ClientSocketHandler.class);

  private final WebSocketClient client = new StandardWebSocketClient();
  private final AtomicReference<WebSocketSession> session = new AtomicReference<>(null);
  private final CbMessageListenerProcessor annotationProcessor;
  private final ApplicationEventPublisher eventPublisher;
  @Getter @Nullable private UUID clientId;

  public ClientSocketHandler(
      CbMessageListenerProcessor annotationProcessor, ApplicationEventPublisher eventPublisher) {
    this.annotationProcessor = annotationProcessor;
    this.eventPublisher = eventPublisher;
  }

  public void open(String address, int port, UUID clientId, String playerName) {
    /* Must be synchronized in order client/session to prevent deadlock */
    synchronized (client) {
      synchronized (session) {
        if (isOpen()) {
          throw new IllegalStateException("Cannot open client socket when it is already open!");
        }
        this.clientId = clientId;
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add(SharedConstants.CLIENT_ID_HTTP_HEADER, clientId.toString());
        headers.add(SharedConstants.CLIENT_NAME_HTTP_HEADER, playerName);
        try {
          URI uri = new URI("ws://" + address + ":" + port + SharedConstants.GAME_SOCKET_ENDPOINT);
          session.set(client.execute(this, headers, uri).get());
        } catch (Exception exception) {
          throw new RuntimeException("Failed to open client socket", exception);
        }
      }
    }
    logger.info(
        "Opened socket connection with gameserver at {}:{}, with client ID {} and name {}",
        address,
        port,
        clientId,
        playerName);
  }

  @Override
  public void handleBinaryMessage(WebSocketSession session, BinaryMessage message)
      throws InvalidProtocolBufferException {
    // Deserialize packet using protobuf
    byte[] payload = message.getPayload().array();
    CbMessage cbMessage = CbMessage.parseFrom(payload);

    // Execute
    annotationProcessor.getHandler().execute(cbMessage, clientId);
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    logger.info("Established connection to websocket");
  }

  @EventListener
  public void onShutdown(ContextClosedEvent event) {
    if (isOpen()) {
      close(new CloseStatus(CloseStatus.NORMAL.getCode(), "Client shutdown"));
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    this.session.set(null);
    logger.info("Closed connection to websocket, status: {}", status);
    eventPublisher.publishEvent(new CloseEvent(this, clientId, status));
    this.clientId = null;
  }

  public void sendSbMessage(SbMessage message) {
    synchronized (session) {
      if (!isOpen())
        throw new IllegalStateException("Cannot send server-bound message when session is closed!");
      byte[] messageBytes = message.toByteArray();
      try {
        session.get().sendMessage(new BinaryMessage(messageBytes));
        logger.debug("Sending server message {}", message.toString().replaceAll("\\s+", ""));
      } catch (IOException exception) {
        throw new RuntimeException(
            "Failed to send server packet of type " + message.getPayloadCase().name(), exception);
      }
    }
  }

  public boolean isOpen() {
    WebSocketSession socketSession = session.get();
    return socketSession != null && socketSession.isOpen();
  }

  public void close(CloseStatus status) {
    if (!isOpen()) {
      throw new IllegalStateException("Cannot close non-open session!");
    }
    try {
      session.get().close(status);
    } catch (IOException exception) {
      throw new RuntimeException("Failed to close socket handler", exception);
    }
  }

  protected void setSession(WebSocketSession session, UUID clientId) {
    if (session == null || clientId == null) {
      throw new IllegalArgumentException("Session and ClientId cannot be null");
    }
    this.session.set(session);
    this.clientId = clientId;
  }

  protected void removeSession() {
    session.set(null);
    clientId = null;
  }

  protected WebSocketSession getSession() {
    return session.get();
  }

  @Getter
  public static class CloseEvent extends ApplicationEvent {

    private final UUID clientId;
    private final CloseStatus status;

    public CloseEvent(Object source, UUID clientId, CloseStatus status) {
      super(source);
      this.clientId = clientId;
      this.status = status;
    }
  }
}
