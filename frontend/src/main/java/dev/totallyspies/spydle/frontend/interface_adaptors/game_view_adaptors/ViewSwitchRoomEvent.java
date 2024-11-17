package dev.totallyspies.spydle.frontend.interface_adaptors.game_view_adaptors;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/*
Presentor for the GameView
 */
@Getter
public class ViewSwitchRoomEvent extends ApplicationEvent {
    private final String viewName;

    public ViewSwitchRoomEvent(Object source, String viewName) {
        super(source);
        this.viewName = viewName;
    }

}
