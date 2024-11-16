package dev.totallyspies.spydle.gameserver.game;

import dev.totallyspies.spydle.gameserver.message.GameSocketHandler;
import dev.totallyspies.spydle.gameserver.message.SbMessageListener;
import dev.totallyspies.spydle.shared.proto.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class GameLogicEvents {
    private final long TIMER_INTERVAL_MILLIS = 1000;
    private final AtomicLong TOTAL_GAME_TIME_MILLIS = new AtomicLong(30000);
    private final Timer timer = new Timer();
    private final AtomicLong gameStartMillis = new AtomicLong(0);

    @Autowired
    private GameLogic gameLogic;

    @Autowired
    private GameSocketHandler gameSocketHandler;

    @SbMessageListener
    public void onPlayerJoin(SbJoinGame event, UUID client) {
        gameLogic.onPlayerJoin(event, client);
    }

    @SbMessageListener
    public void onGameStart(SbStartGame event, UUID client) {
        if (gameLogic.isGameInProgress())
            return;

        gameSocketHandler.sendToAllPlayers(gameLogic.onGameStart(client));

        this.gameStartMillis.set(System.currentTimeMillis());
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                GameLogicEvents.this.sendCbTimerTick();
            }
        }, TIMER_INTERVAL_MILLIS, TIMER_INTERVAL_MILLIS);

        gameSocketHandler.sendToAllPlayers(gameLogic.newTurn());
    }

    @SbMessageListener
    public void onGuess(SbGuess event, UUID client) {
        if (event.getGuessedWord().equals(this.gameLogic.getCurrentSubString())) {
            gameSocketHandler.sendToAllPlayers(gameLogic.newTurn());
        }
    }

    private void sendCbTimerTick() {
        long millisPassed = System.currentTimeMillis() - gameStartMillis.get();
        long millisLeft = TOTAL_GAME_TIME_MILLIS.get() - millisPassed;
        if (millisLeft < 0) {
            this.timer.cancel();
            return;
        }

        var secondsLeft = (int) Math.round(millisLeft / 1000.0);

        var cbMessage = CbMessage
                        .newBuilder()
                        .setTimerTick(CbTimerTick.newBuilder().setTimeLeftSeconds(secondsLeft))
                        .build();

        gameSocketHandler.sendToAllPlayers(cbMessage);
    }
}