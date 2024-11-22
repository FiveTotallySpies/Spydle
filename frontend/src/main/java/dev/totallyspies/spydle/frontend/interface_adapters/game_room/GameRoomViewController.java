package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbStartGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class GameRoomViewController {

    private final Logger logger = LoggerFactory.getLogger(GameRoomViewController.class);

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ClientSocketHandler handler;

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
