package dev.totallyspies.spydle.gameserver.socket.event;

import dev.totallyspies.spydle.shared.proto.GameMessages;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

@Getter
public abstract class ServerBoundMessageEvent extends ApplicationEvent {

    private final WebSocketSession session;
    private final GameMessages.ServerBoundMessage rawMessage;
    private final UUID clientId;

    public ServerBoundMessageEvent(Object source, WebSocketSession session, GameMessages.ServerBoundMessage rawMessage, UUID clientId) {
        super(source);
        this.session = session;
        this.rawMessage = rawMessage;
        this.clientId = clientId;
    }

}