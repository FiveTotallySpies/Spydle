package dev.totallyspies.spydle.frontend.interface_adapters.list_rooms;

import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import dev.totallyspies.spydle.frontend.use_cases.list_games.ListGamesInteractor;
import dev.totallyspies.spydle.frontend.use_cases.list_games.ListGamesOutputData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ListRoomsViewController {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ListGamesInteractor listGamesInteractor;

    /*
    Method called when View All Rooms Button is Pressed
     */
    public void openWelcomeView() {
        publisher.publishEvent(new SwitchViewEvent(this, "WelcomeView"));
    }

    public void updateRoomList() {
        ListGamesOutputData output = listGamesInteractor.execute();
        publisher.publishEvent(new ListRoomsUpdateEvent(this, output.getRoomCodes()));
    }

}
