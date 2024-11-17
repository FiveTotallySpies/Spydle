package dev.totallyspies.spydle.frontend.interface_adapters.welcome;

import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class WelcomeViewController {

    @Autowired
    private ApplicationEventPublisher publisher;

    /*
    Method called when View All Rooms Button is Pressed
     */
    public void openListRoomsView() {
        publisher.publishEvent(new SwitchViewEvent(this, "ListRoomsView"));
    }

}
