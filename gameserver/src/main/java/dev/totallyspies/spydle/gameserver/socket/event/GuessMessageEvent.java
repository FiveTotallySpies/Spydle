package dev.totallyspies.spydle.gameserver.socket.event;

import dev.totallyspies.spydle.shared.proto.GameMessages;
import java.util.UUID;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class GuessMessageEvent extends ServerBoundMessageEvent {

    private final GameMessages.ServerBoundGuess message;

    public GuessMessageEvent(Object source, WebSocketSession session, GameMessages.ServerBoundMessage rawMessage, UUID clientId) {
        super(source, session, rawMessage, clientId);
        message = rawMessage.getGuess();
    }

}