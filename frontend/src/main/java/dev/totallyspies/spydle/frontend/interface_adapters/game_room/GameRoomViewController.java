package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import dev.totallyspies.spydle.frontend.use_cases.guess_word.GuessWordInputData;
import dev.totallyspies.spydle.frontend.use_cases.guess_word.GuessWordInteractor;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbStartGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
public class GameRoomViewController {

    private final Logger logger = LoggerFactory.getLogger(GameRoomViewController.class);

    private final ApplicationEventPublisher publisher;
    private final ClientSocketHandler handler;
    private final GuessWordInteractor guessWordInteractor;
    private final GameRoomViewModel model;

    public GameRoomViewController(
            ApplicationEventPublisher publisher,
            ClientSocketHandler handler,
            GuessWordInteractor guessWordInteractor,
            GameRoomViewModel model
    ) {
        this.publisher = publisher;
        this.handler = handler;
        this.guessWordInteractor = guessWordInteractor;
        this.model = model;
    }

    /*
    Method called when View All Rooms Button is Pressed
     */
    public void openWelcomeView() {
        if (handler.isOpen()) {
            handler.close();
        }
        publisher.publishEvent(new SwitchViewEvent(this, "WelcomeView"));
    }

    public void startGame() {
        if (!handler.isOpen()) {
            logger.error("Cannot start game: client session is not open!");
            return;
        }
        handler.sendSbMessage(SbMessage.newBuilder().setStartGame(SbStartGame.newBuilder().build()).build());
    }

    public void guessWord() {
        guessWordInteractor.execute(new GuessWordInputData(model.getStringEntered()));
    }

}
