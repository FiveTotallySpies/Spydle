package dev.totallyspies.spydle.frontend.client;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.totallyspies.spydle.frontend.client.message.CbMessageListenerProcessor;
import dev.totallyspies.spydle.shared.SharedConstants;
import dev.totallyspies.spydle.shared.proto.messages.CbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Getter
    private final UUID clientId = UUID.randomUUID();

    private WebSocketSession session;
    private final WebSocketClient client = new StandardWebSocketClient();
    private final CbMessageListenerProcessor annotationProcessor;

    public ClientSocketHandler(String address, int port, CbMessageListenerProcessor annotationProcessor) {
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add(SharedConstants.CLIENT_ID_HTTP_HEADER, clientId.toString());
        this.annotationProcessor = annotationProcessor;
        try {
            URI uri = new URI("ws://" + address + ":" + port + SharedConstants.GAME_SOCKET_ENDPOINT);
            session = client.execute(this, headers, uri).get();
        } catch (Exception exception) {
            throw new RuntimeException("Failed to open client socket", exception);
        }
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws InvalidProtocolBufferException {
        // Deserialize packet using protobuf
        byte[] payload = message.getPayload().array();
        CbMessage cbMessage = CbMessage.parseFrom(payload);

        // Execute
        annotationProcessor.getHandler().execute(cbMessage, clientId);
        System.out.println("Received message: " + message.getPayload());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("Established connection to websocket");
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        this.session = null;
        logger.info("Closed connection to websocket, status: {}", status);
    }

    public void sendSbMessage(SbMessage message) {
        if (!isOpen()) throw new IllegalStateException("Cannot send server-bound message when session is closed!");
        byte[] messageBytes = message.toByteArray();
        try {
            session.sendMessage(new BinaryMessage(messageBytes));
            logger.debug("Sending server  message of type {}", message.getPayloadCase().name());
        } catch (IOException exception) {
            throw new RuntimeException("Failed to send server packet of type " + message.getPayloadCase().name(), exception);
        }
    }

    public boolean isOpen() {
        return session != null && session.isOpen();
    }

    public void close() {
        try {
            session.close(CloseStatus.GOING_AWAY);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to close socket handler", exception);
        }
    }

}