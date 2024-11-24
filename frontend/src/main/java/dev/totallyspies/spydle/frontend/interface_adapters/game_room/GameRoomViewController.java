package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbStartGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class GameRoomViewController {

    private final Logger logger = LoggerFactory.getLogger(GameRoomViewController.class);

    private final ApplicationEventPublisher publisher;
    private final ClientSocketHandler handler;

    public GameRoomViewController(ApplicationEventPublisher publisher, ClientSocketHandler handler) {
        this.publisher = publisher;
        this.handler = handler;
    }

    /*
    Method called when View All Rooms Button is Pressed
     */
    public void openWelcomeView() {
        publisher.publishEvent(new SwitchViewEvent(this, "WelcomeView"));
    }

    public void startGame() {
        if (!handler.isOpen()) {
            logger.error("Cannot start game: client session is not open!");
            return;
        }
        handler.sendSbMessage(SbMessage.newBuilder().setStartGame(SbStartGame.newBuilder().build()).build());
    }

}
