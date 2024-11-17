package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import dev.totallyspies.spydle.frontend.views.SwitchViewEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class GameRoomViewController {

    @Autowired
    private ApplicationEventPublisher publisher;

    /*
    Method called when View All Rooms Button is Pressed
     */
    public void openWelcomeView() {
        publisher.publishEvent(new SwitchViewEvent(this, "WelcomeView"));
    }

}
