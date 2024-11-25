package dev.totallyspies.spydle.frontend.interface_adapters.game_end;

import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class GameEndViewController {

    private final ApplicationEventPublisher publisher;

    public GameEndViewController(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /*
    Method called when back Button is Pressed
     */
    public void openWelcomeView() {
        publisher.publishEvent(new SwitchViewEvent(this, "WelcomeView"));
    }

}
