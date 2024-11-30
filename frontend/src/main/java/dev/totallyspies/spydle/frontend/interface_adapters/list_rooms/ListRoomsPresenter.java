package dev.totallyspies.spydle.frontend.interface_adapters.list_rooms;

import dev.totallyspies.spydle.frontend.views.ListRoomsView;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class ListRoomsPresenter {

  private final ListRoomsView view;
  private final ListRoomsViewModel model;

  public ListRoomsPresenter(ListRoomsView view, ListRoomsViewModel model) {
    this.view = view;
    this.model = model;
  }

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
