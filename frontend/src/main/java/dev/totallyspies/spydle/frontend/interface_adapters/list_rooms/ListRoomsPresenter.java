package dev.totallyspies.spydle.frontend.interface_adapters.list_rooms;

import dev.totallyspies.spydle.frontend.views.ListRoomsView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListRoomsPresenter {

    @Autowired
    private ListRoomsView view;

    @Autowired
    private ListRoomsViewModel model;

    @EventListener
    public void onListRoomsUpdate(ListRoomsUpdateEvent event) {
        List<String> roomCodes = event.getRoomCodes();
        String[] lines;
        if (roomCodes.isEmpty()) {
            lines = new String[] {"No Public Rooms Available"};
        } else {
            lines = roomCodes.toArray(new String[0]);
        }
        model.setLinesInRoomList(lines);
        view.updateRoomList();
    }

}
