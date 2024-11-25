package dev.totallyspies.spydle.frontend.interface_adapters.list_rooms;

import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import dev.totallyspies.spydle.frontend.use_cases.list_games.ListGamesInteractor;
import dev.totallyspies.spydle.frontend.use_cases.list_games.ListGamesOutputData;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class ListRoomsViewController {

    private final ApplicationEventPublisher publisher;
    private final ListGamesInteractor listGamesInteractor;

    public ListRoomsViewController(ApplicationEventPublisher publisher, ListGamesInteractor listGamesInteractor) {
        this.publisher = publisher;
        this.listGamesInteractor = listGamesInteractor;
    }

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
