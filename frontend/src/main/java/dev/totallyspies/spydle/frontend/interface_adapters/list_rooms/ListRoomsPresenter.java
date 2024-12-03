package dev.totallyspies.spydle.frontend.interface_adapters.list_rooms;

import dev.totallyspies.spydle.frontend.use_cases.list_games.ListGamesOutputBoundary;
import dev.totallyspies.spydle.frontend.use_cases.list_games.ListGamesOutputData;
import dev.totallyspies.spydle.frontend.views.ListRoomsView;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class ListRoomsPresenter implements ListGamesOutputBoundary {

  private final ListRoomsView view;
  private final ListRoomsViewModel model;

  public ListRoomsPresenter(ListRoomsView view, ListRoomsViewModel model) {
    this.view = view;
    this.model = model;
  }

  @Override
  public void presentGamesList(ListGamesOutputData data) {
    String[] lines;
    if (data.getRoomCodes().isEmpty()) {
      lines = new String[] {"No Public Rooms Available"};
    } else {
      lines = data.getRoomCodes().toArray(new String[0]);
    }
    model.setLinesInRoomList(lines);
    view.updateRoomList();
  }
}
