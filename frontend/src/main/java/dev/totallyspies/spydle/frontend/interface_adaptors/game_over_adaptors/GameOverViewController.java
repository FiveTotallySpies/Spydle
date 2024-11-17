package dev.totallyspies.spydle.frontend.interface_adaptors.game_over_adaptors;

import dev.totallyspies.spydle.frontend.interface_adaptors.game_view_adaptors.SwitchViewEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class GameOverViewController {

    @Autowired
    private ApplicationEventPublisher publisher;

    /*
    Method called when View All Rooms Button is Pressed
     */
    public void openWelcomeView() {
        publisher.publishEvent(new SwitchViewEvent(this, "WelcomeView"));
    }

}