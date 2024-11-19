package dev.totallyspies.spydle.frontend.interface_adapters.welcome;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class WelcomeViewController {

    private final Logger logger = LoggerFactory.getLogger(WelcomeViewController.class);

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private WelcomeViewModel model;

    @Autowired
    private CreateGameInteractor createGameInteractor;

    @Autowired
    private JoinGameInteractor joinGameInteractor;

    @Autowired
    private ClientSocketHandler socketHandler;

    /*
    Method called when View All Rooms Button is Pressed
     */
    public void openListRoomsView() {
        publisher.publishEvent(new SwitchViewEvent(this, "ListRoomsView"));
    }

    public void createGame() {
        if (model.getPlayerName().isBlank() || model.getPlayerName().length() > 32) {
            fireError("Invalid player name: \"" + model.getPlayerName() + "\"");
            return;
        }
        CreateGameInputData input = new CreateGameInputData(model.getPlayerName());
        CreateGameOutputData output = createGameInteractor.execute(input);
        if (output instanceof CreateGameOutputDataSuccess successOutput) {
            try {
                socketHandler.open(
                        successOutput.getGameHost(),
                        successOutput.getGamePort(),
                        successOutput.getClientId(),
                        successOutput.getPlayerName()
                );
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
        if (model.getPlayerName().isBlank() || model.getPlayerName().length() > 32) {
            fireError("Invalid player name: \"" + model.getPlayerName() + "\"");
            return;
        }
        if (model.getRoomCode().isBlank() || model.getRoomCode().length() != 5) {
            fireError("Invalid room code: \"" + model.getRoomCode() + "\"");
            return;
        }
        JoinGameInputData input = new JoinGameInputData(model.getPlayerName(), model.getRoomCode());
        JoinGameOutputData output = joinGameInteractor.execute(input);
        if (output instanceof JoinGameOutputDataSuccess successOutput) {
            try {
                socketHandler.open(
                        successOutput.getGameHost(),
                        successOutput.getGamePort(),
                        successOutput.getClientId(),
                        successOutput.getPlayerName()
                );
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
