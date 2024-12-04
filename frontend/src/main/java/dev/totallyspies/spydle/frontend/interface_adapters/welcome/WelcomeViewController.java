package dev.totallyspies.spydle.frontend.interface_adapters.welcome;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewModel;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.ErrorViewEvent;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import dev.totallyspies.spydle.frontend.use_cases.create_game.CreateGameInputBoundary;
import dev.totallyspies.spydle.frontend.use_cases.create_game.CreateGameInputData;
import dev.totallyspies.spydle.frontend.use_cases.create_game.CreateGameOutputBoundary;
import dev.totallyspies.spydle.frontend.use_cases.create_game.CreateGameOutputData;
import dev.totallyspies.spydle.frontend.use_cases.join_game.JoinGameInputBoundary;
import dev.totallyspies.spydle.frontend.use_cases.join_game.JoinGameInputData;
import dev.totallyspies.spydle.frontend.use_cases.join_game.JoinGameOutputBoundary;
import dev.totallyspies.spydle.frontend.use_cases.join_game.JoinGameOutputData;
import dev.totallyspies.spydle.frontend.views.ListRoomsView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class WelcomeViewController {

  private final Logger logger = LoggerFactory.getLogger(WelcomeViewController.class);

  private final ApplicationEventPublisher publisher;
  private final WelcomeViewModel welcomeModel;
  private final GameRoomViewModel gameRoomModel;
  private final CreateGameInputBoundary createGameInteractor;
  private final JoinGameInputBoundary joinGameInteractor;
  private final ClientSocketHandler socketHandler;
  private final CreateGameOutputBoundary createGamePresenter;
  private final JoinGameOutputBoundary joinGamePresenter;

  public WelcomeViewController(
      ApplicationEventPublisher publisher,
      WelcomeViewModel welcomeModel,
      GameRoomViewModel gameRoomModel,
      CreateGameInputBoundary createGameInteractor,
      JoinGameInputBoundary joinGameInteractor,
      ClientSocketHandler socketHandler,
      @Lazy CreateGameOutputBoundary createGamePresenter,
      @Lazy JoinGameOutputBoundary joinGamePresenter) {
    this.publisher = publisher;
    this.welcomeModel = welcomeModel;
    this.gameRoomModel = gameRoomModel;
    this.createGameInteractor = createGameInteractor;
    this.joinGameInteractor = joinGameInteractor;
    this.socketHandler = socketHandler;
    this.createGamePresenter = createGamePresenter;
    this.joinGamePresenter = joinGamePresenter;
  }

  /*
  Method called when View All Rooms Button is Pressed
   */
  public void openListRoomsView() {
    publisher.publishEvent(new SwitchViewEvent(this, ListRoomsView.class));
  }

  public void createGame() {
    if (welcomeModel.getPlayerName().isBlank() || welcomeModel.getPlayerName().length() > 32) {
      fireError("Invalid player name: \"" + welcomeModel.getPlayerName() + "\"");
      return;
    }
    CreateGameInputData input = new CreateGameInputData(welcomeModel.getPlayerName());
    CreateGameOutputData output = createGameInteractor.execute(input);
    createGamePresenter.presentCreateGame(output);
  }

  public void joinGame() {
    if (welcomeModel.getPlayerName().isBlank() || welcomeModel.getPlayerName().length() > 32) {
      fireError("Invalid player name: \"" + welcomeModel.getPlayerName() + "\"");
      return;
    }
    if (welcomeModel.getRoomCode().isBlank() || welcomeModel.getRoomCode().length() != 5) {
      fireError("Invalid room code: \"" + welcomeModel.getRoomCode() + "\"");
      return;
    }
    JoinGameInputData input =
        new JoinGameInputData(welcomeModel.getPlayerName(), welcomeModel.getRoomCode());
    JoinGameOutputData output = joinGameInteractor.execute(input);
    joinGamePresenter.presentJoinGame(output);
  }

  private void fireError(String message) {
    publisher.publishEvent(new ErrorViewEvent(this, message));
  }
}
