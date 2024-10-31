package dev.totallyspies.spydle.gameserver.socket.event;

import dev.totallyspies.spydle.shared.proto.GameMessages;
import java.util.UUID;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class LeaveGameMessageEvent extends ServerBoundMessageEvent {

    private final GameMessages.ServerBoundLeaveGame message;

    public LeaveGameMessageEvent(Object source, WebSocketSession session, GameMessages.ServerBoundMessage rawMessage, UUID clientId) {
        super(source, session, rawMessage, clientId);
        message = rawMessage.getLeaveGame();
    }

}