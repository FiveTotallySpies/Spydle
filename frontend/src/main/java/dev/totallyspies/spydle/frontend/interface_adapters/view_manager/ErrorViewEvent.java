package dev.totallyspies.spydle.frontend.interface_adapters.view_manager;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ErrorViewEvent extends ApplicationEvent {

    private final String message;

    public ErrorViewEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

}