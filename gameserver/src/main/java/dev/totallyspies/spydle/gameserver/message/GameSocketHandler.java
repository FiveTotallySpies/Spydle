package dev.totallyspies.spydle.gameserver.message;

import dev.totallyspies.spydle.gameserver.message.session.ClientSessionValidator;
import dev.totallyspies.spydle.gameserver.message.session.SessionCloseEvent;
import dev.totallyspies.spydle.gameserver.message.session.SessionOpenEvent;
import dev.totallyspies.spydle.gameserver.storage.GameServerStorage;
import dev.totallyspies.spydle.shared.SharedConstants;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.proto.messages.CbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GameSocketHandler extends BinaryWebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(GameSocketHandler.class);

    @Autowired
    private GameServerStorage storage;

    @Autowired
    private ClientSessionValidator sessionValidator;

    @Autowired
    private SbMessageListenerProcessor annotationProcessor;

    @Autowired
    private ApplicationEventPublisher publisher;

    private final Map<ClientSession, WebSocketSession> sessions = Collections.synchronizedMap(new LinkedHashMap<>());
    
    public List<ClientSession> getSessions() {
        return new LinkedList<>(sessions.keySet());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession socketSession, BinaryMessage message) throws IOException {
        String rawClientId = getHeader(socketSession, SharedConstants.CLIENT_ID_HTTP_HEADER);
        UUID clientId = sessionValidator.parseClientId(rawClientId);
        // Validate session has clientId
        if (clientId == null) {
            logger.warn("Received message on session {} without header {}", rawClientId, SharedConstants.CLIENT_ID_HTTP_HEADER);
            return;
        }

        String clientName = getHeader(socketSession, SharedConstants.CLIENT_NAME_HTTP_HEADER);
        if (clientName == null) {
            logger.warn("Received message on session {} without header {}", rawClientId, SharedConstants.CLIENT_NAME_HTTP_HEADER);
            return;
        }

        // Validate that session is allowed to communicate with this gameserver
        if (!sessionValidator.validateClientSession(clientId, clientName)) {
            socketSession.close(CloseStatus.NOT_ACCEPTABLE);
            logger.warn("Received message from unconfirmed session {}", rawClientId);
            return;
        }

        // Deserialize packet using protobuf
        byte[] payload = message.getPayload().array();
        SbMessage sbMessage = SbMessage.parseFrom(payload);

        // Execute
        annotationProcessor.getHandler().execute(sbMessage, clientId);
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
            byte[] messageBytes = message.toByteArray();
            session.sendMessage(new BinaryMessage(messageBytes));
            logger.debug("Sending client {} message of type {}", targetSession.getClientId().toString(), message.getPayloadCase().name());
        } catch (IOException exception) {
            throw new RuntimeException("Failed to send client " + targetSession.getClientId().toString() + " packet of type " + message.getPayloadCase().name(), exception);
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
            socketSession.close(CloseStatus.NOT_ACCEPTABLE);
            logger.warn("Client attempted to open session {} with no name, closing...", rawClientId);
            return;
        }
        if (clientId == null || !sessionValidator.validateClientSession(clientId, clientName)) {
            socketSession.close(CloseStatus.NOT_ACCEPTABLE);
            logger.warn("Client attempted to open unconfirmed session {}, closing...", rawClientId);
            return;
        }
        if (hasSessionWithPlayerName(clientName)) {
            socketSession.close(CloseStatus.NOT_ACCEPTABLE); // TODO: have some way to notify the player that their name is taken already
            logger.warn("Client attempted to open session {} but their name already exists, closing...", rawClientId);
            return;
        }
        ClientSession storedSession = storage.getClientSession(clientId);
        if (storedSession == null) {
            socketSession.close(CloseStatus.NOT_ACCEPTABLE);
            logger.warn("Client attempted to open session {} with no stored client session for this UUID, closing...", rawClientId);
            return;
        }
        sessions.put(storedSession, socketSession);
        logger.info("Initiated connection with client {} and name {}", clientId, clientName);
        publisher.publishEvent(new SessionOpenEvent(this, storedSession, socketSession));
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
                try {
                    sessions.remove(session).close();
                } catch (IOException exception) {
                    logger.error("Failed to close session {}", clientId, exception);
                }
                publisher.publishEvent(new SessionCloseEvent(this, session, socketSession));
            }
        }
        logger.info("Closed connection with client {} for reason {}", rawClientId, status);
    }

    @Nullable
    private static String getHeader(WebSocketSession session, String headerName) {
        List<String> headerValues = session.getHandshakeHeaders().get(headerName);
        if (headerValues == null || headerValues.isEmpty()) {
            return null;
        }
        return headerValues.get(0);
    }

    @Nullable
    public ClientSession getSessionFromClientId(UUID clientId) {
        return sessions
                .keySet()
                .stream()
                .filter(session -> session.getClientId().equals(clientId))
                .findAny()
                .orElse(null);
    }

    private boolean hasSessionWithPlayerName(String name) {
        return sessions
                .keySet()
                .stream()
                .anyMatch(session -> session.getPlayerName().equalsIgnoreCase(name));
    }


}