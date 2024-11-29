package dev.totallyspies.spydle.gameserver.session;

import dev.totallyspies.spydle.shared.model.ClientSession;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

// Fired AFTER socket closed
@Getter
public class SessionCloseEvent extends ApplicationEvent {

  private final ClientSession session;
  private final WebSocketSession socketSession;

  public SessionCloseEvent(Object source, ClientSession session, WebSocketSession socketSession) {
    super(source);
    this.session = session;
    this.socketSession = socketSession;
  }
}
