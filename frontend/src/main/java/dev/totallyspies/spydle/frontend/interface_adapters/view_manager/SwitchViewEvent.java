package dev.totallyspies.spydle.frontend.interface_adapters.view_manager;

import dev.totallyspies.spydle.frontend.views.CardView;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/*
 * Fired when we want to switch views
 */
@Getter
public class SwitchViewEvent extends ApplicationEvent {

    private final Class<? extends CardView> viewClass;

    public SwitchViewEvent(Object source, Class<? extends CardView> viewClass) {
        super(source);
        this.viewClass = viewClass;
    }

}
