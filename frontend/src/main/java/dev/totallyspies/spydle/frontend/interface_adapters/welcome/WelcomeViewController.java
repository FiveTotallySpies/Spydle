package dev.totallyspies.spydle.frontend.interface_adapters.welcome;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewModel;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.ErrorViewEvent;
import dev.totallyspies.spydle.frontend.use_cases.create_game.CreateGameInputData;
import dev.totallyspies.spydle.frontend.use_cases.create_game.CreateGameInteractor;
import dev.totallyspies.spydle.frontend.use_cases.create_game.CreateGameOutputData;
import dev.totallyspies.spydle.frontend.use_cases.create_game.CreateGameOutputDataFail;
import dev.totallyspies.spydle.frontend.use_cases.create_game.CreateGameOutputDataSuccess;
import dev.totallyspies.spydle.frontend.use_cases.join_game.JoinGameInputData;
import dev.totallyspies.spydle.frontend.use_cases.join_game.JoinGameInteractor;
import dev.totallyspies.spydle.frontend.use_cases.join_game.JoinGameOutputData;
import dev.totallyspies.spydle.frontend.use_cases.join_game.JoinGameOutputDataFail;
import dev.totallyspies.spydle.frontend.use_cases.join_game.JoinGameOutputDataSuccess;
import dev.totallyspies.spydle.shared.proto.messages.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
public class WelcomeViewController {

    private final Logger logger = LoggerFactory.getLogger(WelcomeViewController.class);

    private final ApplicationEventPublisher publisher;
    private final WelcomeViewModel welcomeModel;
    private final GameRoomViewModel gameRoomModel;
    private final CreateGameInteractor createGameInteractor;
    private final JoinGameInteractor joinGameInteractor;
    private final ClientSocketHandler socketHandler;

    public WelcomeViewController(
            ApplicationEventPublisher publisher,
            WelcomeViewModel welcomeModel,
            GameRoomViewModel gameRoomModel,
            CreateGameInteractor createGameInteractor,
            JoinGameInteractor joinGameInteractor,
            ClientSocketHandler socketHandler
    ) {
        this.publisher = publisher;
        this.welcomeModel = welcomeModel;
        this.gameRoomModel = gameRoomModel;
        this.createGameInteractor = createGameInteractor;
        this.joinGameInteractor = joinGameInteractor;
        this.socketHandler = socketHandler;
    }

    /*
    Method called when View All Rooms Button is Pressed
     */
    public void openListRoomsView() {
        publisher.publishEvent(new SwitchViewEvent(this, "ListRoomsView"));
    }

    public void createGame() {
        if (welcomeModel.getPlayerName().isBlank() || welcomeModel.getPlayerName().length() > 32) {
            fireError("Invalid player name: \"" + welcomeModel.getPlayerName() + "\"");
            return;
        }
        CreateGameInputData input = new CreateGameInputData(welcomeModel.getPlayerName());
        CreateGameOutputData output = createGameInteractor.execute(input);
        if (output instanceof CreateGameOutputDataSuccess successOutput) {
            try {
                socketHandler.open(
                        successOutput.getGameHost(),
                        successOutput.getGamePort(),
                        successOutput.getClientId(),
                        successOutput.getPlayerName()
                );
                gameRoomModel.setRoomCode(successOutput.getRoomCode());
                gameRoomModel.setLocalPlayer(Player.newBuilder().setPlayerName(successOutput.getPlayerName()).setScore(0).build());
                publisher.publishEvent(new SwitchViewEvent(this, "GameRoomView"));
            } catch (Exception exception) {
                fireError("Failed to connect to game server: " + exception.getMessage());
                logger.error("Failed to connect to game server: ", exception);
            }
        } else {
            CreateGameOutputDataFail failOutput = (CreateGameOutputDataFail) output;
            fireError("Failed to send request to matchmaker:\n" + failOutput.getMessage());
        }
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
        JoinGameInputData input = new JoinGameInputData(welcomeModel.getPlayerName(), welcomeModel.getRoomCode());
        JoinGameOutputData output = joinGameInteractor.execute(input);
        if (output instanceof JoinGameOutputDataSuccess successOutput) {
            try {
                socketHandler.open(
                        successOutput.getGameHost(),
                        successOutput.getGamePort(),
                        successOutput.getClientId(),
                        successOutput.getPlayerName()
                );
                gameRoomModel.setRoomCode(successOutput.getRoomCode());
                gameRoomModel.setLocalPlayer(Player.newBuilder().setPlayerName(successOutput.getPlayerName()).setScore(0).build());
                publisher.publishEvent(new SwitchViewEvent(this, "GameRoomView"));
            } catch (Exception exception) {
                fireError("Failed to connect to game server: " + exception.getMessage());
                logger.error("Failed to connect to game server: ", exception);
            }
        } else {
            JoinGameOutputDataFail failOutput = (JoinGameOutputDataFail) output;
            fireError("Failed to send request to matchmaker:\n" + failOutput.getMessage());
        }
    }

    private void fireError(String message) {
        publisher.publishEvent(new ErrorViewEvent(this, message));
    }

}
