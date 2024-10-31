package dev.totallyspies.spydle.gameserver.socket.event;

import dev.totallyspies.spydle.shared.proto.GameMessages;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

@Getter
public class JoinGameMessageEvent extends ServerBoundMessageEvent {

    private final GameMessages.ServerBoundJoinGame message;

    public JoinGameMessageEvent(Object source, WebSocketSession session, GameMessages.ServerBoundMessage rawMessage, UUID clientId) {
        super(source, session, rawMessage, clientId);
        message = rawMessage.getJoinGame();
    }

}