package dev.totallyspies.spydle.gameserver.socket.event;

import dev.totallyspies.spydle.shared.proto.GameMessages;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

@Getter
public class StartGameMessageEvent extends ServerBoundMessageEvent {

    private final GameMessages.ServerBoundStartGame message;

    public StartGameMessageEvent(Object source, WebSocketSession session, GameMessages.ServerBoundMessage rawMessage, UUID clientId) {
        super(source, session, rawMessage, clientId);
        message = rawMessage.getStartGame();
    }

}