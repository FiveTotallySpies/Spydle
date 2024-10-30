package dev.totallyspies.spydle.gameserver.socket;

import dev.totallyspies.spydle.gameserver.proto.GameMessages;
import dev.totallyspies.spydle.gameserver.redis.RedisRepositoryService;
import dev.totallyspies.spydle.gameserver.socket.event.ServerBoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameSocketHandler extends BinaryWebSocketHandler {

    private static final String CLIENT_ID_ATTRIBUTE = "clientId";

    private final Logger logger = LoggerFactory.getLogger(GameSocketHandler.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private Map<GameMessages.ServerBoundMessage.PayloadCase, Class<? extends ServerBoundEvent>> eventRegistry;

    @Autowired
    private RedisRepositoryService redisRepositoryService;

    private Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
        UUID clientId = redisRepositoryService.parseClientId(session.getAttributes().get(CLIENT_ID_ATTRIBUTE));
        // Validate session has clientId
        if (clientId == null) {
            logger.warn("Received packet on session {} without {}", session.getId(), CLIENT_ID_ATTRIBUTE);
            return;
        }

        // Validate that session is allowed to communicate with this gameserver
        if (!redisRepositoryService.hasClientSession(clientId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            logger.warn("Received packet from unconfirmed session {}", session.getId());
            return;
        }

        // Deserialize packet using protobuf
        byte[] payload = message.getPayload().array();
        GameMessages.ServerBoundMessage serverBoundMessage = GameMessages.ServerBoundMessage.parseFrom(payload);
        try {
            ServerBoundEvent event = (ServerBoundEvent) eventRegistry.get(serverBoundMessage.getPayloadCase())
                    .getDeclaredConstructors()[0]
                    .newInstance(this, session, serverBoundMessage, clientId);
            logger.debug("Firing event {} after receiving from client {}", event.getClass().getName(), clientId);
            try {
                eventPublisher.publishEvent(event);
            } catch (Exception exception) {
                logger.error("Failed to handle event of type {}", serverBoundMessage.getPayloadCase().name(), exception);
            }
        } catch (InvocationTargetException | NullPointerException | InstantiationException | IllegalAccessException exception) {
            logger.error("Failed to invoke event for payload case {}", serverBoundMessage.getPayloadCase().name(), exception);
        }
    }

    public void sendClientBoundMessage(UUID clientId, GameMessages.ClientBoundMessage message) throws IOException {
        WebSocketSession session = sessions.get(clientId);
        if (session == null) {
            throw new IllegalArgumentException("Cannot send message to invalid client " + clientId.toString());
        }
        byte[] messageBytes = message.toByteArray();
        session.sendMessage(new BinaryMessage(messageBytes));
        logger.debug("Sending client {} message of type {}", clientId.toString(), message.getPayloadCase().name());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Validate that the client has been assigned to us
        UUID clientId = redisRepositoryService.parseClientId(session.getAttributes().get(CLIENT_ID_ATTRIBUTE));
        if (clientId == null || !redisRepositoryService.hasClientSession(clientId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        sessions.put(clientId, session);
        logger.info("Initiated connection with client {}", clientId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Validate that the client had a connection with us before deleting the session
        UUID clientId = redisRepositoryService.parseClientId(session.getAttributes().get(CLIENT_ID_ATTRIBUTE));
        if (clientId != null && redisRepositoryService.hasClientSession(clientId)) {
            redisRepositoryService.removeClientSession(clientId);
        }
        sessions.remove(clientId);
        logger.info("Closed connection with client {} for reason {}", clientId, status);
    }
}