package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import dev.totallyspies.spydle.frontend.use_cases.guess_word.GuessWordInputData;
import dev.totallyspies.spydle.frontend.use_cases.guess_word.GuessWordInteractor;
import dev.totallyspies.spydle.frontend.use_cases.update_string.UpdateStringInteractor;
import dev.totallyspies.spydle.frontend.views.WelcomeView;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbStartGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;

@Component
@Profile("!test")
public class GameRoomViewController {

    private final Logger logger = LoggerFactory.getLogger(GameRoomViewController.class);

    private final ApplicationEventPublisher publisher;
    private final ClientSocketHandler handler;
    private final GuessWordInteractor guessWordInteractor;
    private final GameRoomViewModel model;
    private final UpdateStringInteractor updateStringInteractor;

    public GameRoomViewController(
            ApplicationEventPublisher publisher,
            ClientSocketHandler handler,
            GuessWordInteractor guessWordInteractor,
            GameRoomViewModel model,
            UpdateStringInteractor updateStringInteractor
    ) {
        this.publisher = publisher;
        this.handler = handler;
        this.guessWordInteractor = guessWordInteractor;
        this.model = model;
        this.updateStringInteractor = updateStringInteractor;
    }

    /*
    Method called when View All Rooms Button is Pressed
     */
    public void openWelcomeView() {
        if (handler.isOpen()) {
            handler.close(new CloseStatus(CloseStatus.NORMAL.getCode(), "Client prompted session termination"));
        }
        publisher.publishEvent(new SwitchViewEvent(this, WelcomeView.class));
    }

    public void startGame() {
        if (!handler.isOpen()) {
            logger.error("Cannot start game: client session is not open!");
            return;
        }
        handler.sendSbMessage(SbMessage.newBuilder().setStartGame(SbStartGame.newBuilder().build()).build());
    }

    public void setUpdatedString(){
        updateStringInteractor.execute(String );
    }

    public void guessWord() {
        guessWordInteractor.execute(new GuessWordInputData(model.getStringEntered()));
        publisher.publishEvent(new GuessWordEvent(this, model.getStringEntered()));
    }

}
