package dev.totallyspies.spydle.gameserver.message;

import dev.totallyspies.spydle.gameserver.session.ClientSessionValidator;
import dev.totallyspies.spydle.gameserver.storage.GameServerStorage;
import dev.totallyspies.spydle.shared.SharedConstants;
import dev.totallyspies.spydle.shared.proto.messages.CbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import java.util.Collection;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameSocketHandler extends BinaryWebSocketHandler {

    private static final String HEADER = SharedConstants.CLIENT_ID_HTTP_HEADER;

    private final Logger logger = LoggerFactory.getLogger(GameSocketHandler.class);

    @Autowired
    private GameServerStorage storage;

    @Autowired
    private ClientSessionValidator sessionValidator;

    @Autowired
    private SbMessageListenerProcessor annotationProcessor;

    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public Collection<UUID> getSessions() {
        return sessions.keySet();
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
        String rawClientId = getClientId(session);
        UUID clientId = sessionValidator.parseClientId(rawClientId);
        // Validate session has clientId
        if (clientId == null) {
            logger.warn("Received packet on session {} without header {}", rawClientId, HEADER);
            return;
        }

        // Validate that session is allowed to communicate with this gameserver
        if (!sessionValidator.validateClientSession(clientId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            logger.warn("Received packet from unconfirmed session {}", rawClientId);
            return;
        }

        // Deserialize packet using protobuf
        byte[] payload = message.getPayload().array();
        SbMessage sbMessage = SbMessage.parseFrom(payload);

        // Execute
        annotationProcessor.getHandler().execute(sbMessage, clientId);
    }


    public void sendCbMessage(UUID clientId, CbMessage message) {
        try {
            WebSocketSession session = sessions.get(clientId);
            if (session == null) {
                throw new IllegalArgumentException("Cannot send message to invalid client " + clientId.toString());
            }
            byte[] messageBytes = message.toByteArray();
            session.sendMessage(new BinaryMessage(messageBytes));
            logger.debug("Sending client {} message of type {}", clientId.toString(), message.getPayloadCase().name());
        } catch (IOException exception) {
            throw new RuntimeException("Failed to send client " + clientId.toString() + " packet of type " + message.getPayloadCase().name(), exception);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Validate that the client has been assigned to us
        String rawClientId = getClientId(session);
        UUID clientId = sessionValidator.parseClientId(rawClientId);
        if (clientId == null || !sessionValidator.validateClientSession(clientId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            logger.warn("Client attempted to open unconfirmed session {}, closing...", rawClientId);
            return;
        }
        sessions.put(clientId, session);
        logger.info("Initiated connection with client {}", clientId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Validate that the client had a connection with us before deleting the session
        String rawClientId = getClientId(session);
        UUID clientId = sessionValidator.parseClientId(rawClientId);
        if (clientId != null && sessionValidator.validateClientSession(clientId)) {
            storage.deleteClientSession(clientId);
        }
        if (clientId != null) {
            sessions.remove(clientId);
        }
        logger.info("Closed connection with client {} for reason {}", rawClientId, status);
    }

    @Nullable
    private static String getClientId(WebSocketSession session) {
        List<String> headerValues = session.getHandshakeHeaders().get(HEADER);
        if (headerValues == null || headerValues.isEmpty()) {
            return null;
        }
        return headerValues.get(0);
    }

    @PreDestroy
    public void onShutdown() {
        for (UUID clientId : getSessions()) {
            storage.deleteClientSession(clientId);
        }
    }

}