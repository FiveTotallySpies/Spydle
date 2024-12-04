package dev.totallyspies.spydle.frontend.interface_adapters.welcome;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewModel;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.ErrorViewEvent;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import dev.totallyspies.spydle.frontend.use_cases.create_game.CreateGameOutputBoundary;
import dev.totallyspies.spydle.frontend.use_cases.create_game.CreateGameOutputData;
import dev.totallyspies.spydle.frontend.use_cases.create_game.CreateGameOutputDataFail;
import dev.totallyspies.spydle.frontend.use_cases.create_game.CreateGameOutputDataSuccess;
import dev.totallyspies.spydle.frontend.use_cases.join_game.JoinGameOutputBoundary;
import dev.totallyspies.spydle.frontend.use_cases.join_game.JoinGameOutputData;
import dev.totallyspies.spydle.frontend.use_cases.join_game.JoinGameOutputDataFail;
import dev.totallyspies.spydle.frontend.use_cases.join_game.JoinGameOutputDataSuccess;
import dev.totallyspies.spydle.frontend.views.GameRoomView;
import dev.totallyspies.spydle.shared.proto.messages.Player;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class WelcomePresenter implements CreateGameOutputBoundary, JoinGameOutputBoundary {

  private final Logger logger = LoggerFactory.getLogger(WelcomePresenter.class);

  private final ClientSocketHandler socketHandler;
  private final GameRoomViewModel gameRoomModel;
  private final ApplicationEventPublisher publisher;

  public WelcomePresenter(
      ClientSocketHandler socketHandler,
      GameRoomViewModel gameRoomModel,
      ApplicationEventPublisher publisher) {
    this.socketHandler = socketHandler;
    this.gameRoomModel = gameRoomModel;
    this.publisher = publisher;
  }

  @Override
  public void presentCreateGame(CreateGameOutputData data) {
    if (data instanceof CreateGameOutputDataSuccess successOutput) {
      presentRoom(
          successOutput.getGameHost(),
          successOutput.getGamePort(),
          successOutput.getClientId(),
          successOutput.getPlayerName(),
          successOutput.getRoomCode());
    } else {
      CreateGameOutputDataFail failOutput = (CreateGameOutputDataFail) data;
      fireError("Failed to send request to matchmaker:\n" + failOutput.getMessage());
    }
  }

  @Override
  public void presentJoinGame(JoinGameOutputData data) {
    if (data instanceof JoinGameOutputDataSuccess successOutput) {
      presentRoom(
          successOutput.getGameHost(),
          successOutput.getGamePort(),
          successOutput.getClientId(),
          successOutput.getPlayerName(),
          successOutput.getRoomCode());
    } else {
      JoinGameOutputDataFail failOutput = (JoinGameOutputDataFail) data;
      fireError("Failed to send request to matchmaker:\n" + failOutput.getMessage());
    }
  }

  private void presentRoom(
      String gameHost, int gamePort, UUID clientId, String playerName, String roomCode) {
    try {
      socketHandler.open(gameHost, gamePort, clientId, playerName);
      gameRoomModel.setRoomCode(roomCode);
      gameRoomModel.setLocalPlayer(
          Player.newBuilder().setPlayerName(playerName).setScore(0).build());
      publisher.publishEvent(new SwitchViewEvent(this, GameRoomView.class));
    } catch (Exception exception) {
      fireError("Failed to connect to game server: " + exception.getMessage());
      logger.error("Failed to connect to game server: ", exception);
    }
  }

  private void fireError(String message) {
    publisher.publishEvent(new ErrorViewEvent(this, message));
  }
}
