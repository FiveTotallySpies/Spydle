package dev.totallyspies.spydle.gameserver.socket.event;

import dev.totallyspies.spydle.gameserver.proto.GameMessages;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

@Getter
public class PlayerJoinEvent extends ServerBoundEvent {

    private final GameMessages.ServerBoundPlayerJoin message;

    public PlayerJoinEvent(Object source, WebSocketSession session, GameMessages.ServerBoundMessage rawMessage, UUID clientId) {
        super(source, session, rawMessage, clientId);
        message = rawMessage.getPlayerJoin();
    }

}