package dev.totallyspies.spydle.frontend.interface_adapters.list_rooms;

import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import dev.totallyspies.spydle.frontend.use_cases.list_games.ListGamesInputBoundary;
import dev.totallyspies.spydle.frontend.use_cases.list_games.ListGamesOutputData;
import dev.totallyspies.spydle.frontend.views.WelcomeView;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class ListRoomsViewController {

  private final ApplicationEventPublisher publisher;
  private final ListGamesInputBoundary listGamesInteractor;
  private final ListRoomsPresenter presenter;

  public ListRoomsViewController(
          ApplicationEventPublisher publisher,
          ListGamesInputBoundary listGamesInteractor,
          @Lazy ListRoomsPresenter presenter) {
    this.publisher = publisher;
    this.listGamesInteractor = listGamesInteractor;
    this.presenter = presenter;
  }

  /*
  Method called when View All Rooms Button is Pressed
   */
  public void openWelcomeView() {
    publisher.publishEvent(new SwitchViewEvent(this, WelcomeView.class));
  }

  public void updateRoomList() {
    ListGamesOutputData output = listGamesInteractor.execute();
    presenter.updateViewRomCodes(output.getRoomCodes());
  }
}
