package dev.totallyspies.spydle.frontend.client;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.totallyspies.spydle.frontend.client.message.CbMessageListenerProcessor;
import dev.totallyspies.spydle.shared.SharedConstants;
import dev.totallyspies.spydle.shared.proto.messages.CbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import jakarta.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

public class ClientSocketHandler extends BinaryWebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(ClientSocketHandler.class);

    private final WebSocketClient client = new StandardWebSocketClient();

    @Getter
    @Nullable
    private UUID clientId;

    private final AtomicReference<WebSocketSession> session = new AtomicReference<>(null);
    private final CbMessageListenerProcessor annotationProcessor;
    private final ApplicationEventPublisher eventPublisher;

    public ClientSocketHandler(CbMessageListenerProcessor annotationProcessor, ApplicationEventPublisher eventPublisher) {
        this.annotationProcessor = annotationProcessor;
        this.eventPublisher = eventPublisher;
    }

    public void open(String address, int port, UUID clientId, String playerName) {
<<<<<<< HEAD
        if (isOpen()) {
            throw new IllegalStateException("Cannot open client socket when it is already open!");
        }
        this.clientId = clientId;
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add(SharedConstants.CLIENT_ID_HTTP_HEADER, clientId.toString());
        headers.add(SharedConstants.CLIENT_NAME_HTTP_HEADER, playerName);
        try {
            URI uri = new URI("ws://" + address + ":" + port + SharedConstants.GAME_SOCKET_ENDPOINT);
            session = client.execute(this, headers, uri).get();
        } catch (Exception exception) {
            throw new RuntimeException("Failed to open client socket " + exception.getMessage(), exception);
=======
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
>>>>>>> main
        }
        logger.info("Opened socket connection with gameserver at {}:{}, with client ID {} and name {}", address, port, clientId, playerName);
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws InvalidProtocolBufferException {
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

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        this.session.set(null);
        this.clientId = null;
        logger.info("Closed connection to websocket, status: {}", status);
        eventPublisher.publishEvent(new CloseEvent(this, clientId, status));
    }

    public void sendSbMessage(SbMessage message) {
        synchronized (session) {
            if (!isOpen()) throw new IllegalStateException("Cannot send server-bound message when session is closed!");
            byte[] messageBytes = message.toByteArray();
            try {
                session.get().sendMessage(new BinaryMessage(messageBytes));
                logger.debug("Sending server message {}", message.toString().replaceAll("\\s+", ""));
            } catch (IOException exception) {
                throw new RuntimeException("Failed to send server packet of type " + message.getPayloadCase().name(), exception);
            }
        }
    }

    public boolean isOpen() {
        WebSocketSession socketSession = session.get();
        return socketSession != null && socketSession.isOpen();
    }

    public void close() {
        if (!isOpen()) {
            throw new IllegalStateException("Cannot close non-open session!");
        }
        try {
            session.get().close(CloseStatus.GOING_AWAY);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to close socket handler", exception);
        }
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