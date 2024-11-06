package dev.totallyspies.spydle.gameserver.game;

import dev.totallyspies.spydle.gameserver.message.GameSocketHandler;
import dev.totallyspies.spydle.gameserver.message.SbMessageListener;
import dev.totallyspies.spydle.shared.proto.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GameLogicEvents {

    @Autowired
    private GameLogic gameLogic;

    @Autowired
    private GameSocketHandler gameSocketHandler;

    @SbMessageListener
    public void onPlayerNameSelect(SbSelectName event, UUID client) {
        gameLogic.onPlayerNameSelect(event, client);
    }

    @SbMessageListener
    public void onGameStart(SbStartGame event, UUID client) {
        var cbGameStart = gameLogic.onGameStart(client);

        var cbMessage = CbMessage
                .newBuilder()
                .setGameStart(cbGameStart)
                .build();

        var connectedPlayers = gameSocketHandler.getSessions();
        for (UUID player : connectedPlayers) {
            gameSocketHandler.sendCbMessage(player, cbMessage);
        }
    }
}