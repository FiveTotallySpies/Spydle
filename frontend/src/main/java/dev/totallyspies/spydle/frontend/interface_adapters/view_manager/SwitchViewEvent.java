package dev.totallyspies.spydle.frontend.interface_adapters.view_manager;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/*
 * Fired when we want to switch views
 */
@Getter
public class SwitchViewEvent extends ApplicationEvent {

    private final Class<?> viewClass;

    public SwitchViewEvent(Object source, Class<?> viewClass) {
        super(source);
        this.viewClass = viewClass;
    }

}
