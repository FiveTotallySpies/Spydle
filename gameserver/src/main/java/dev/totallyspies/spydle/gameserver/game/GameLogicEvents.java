package dev.totallyspies.spydle.gameserver.game;

import dev.totallyspies.spydle.gameserver.message.GameSocketHandler;
import dev.totallyspies.spydle.gameserver.message.SbMessageListener;
import dev.totallyspies.spydle.shared.proto.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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
    public void onPlayerNameSelect(SbSelectName event, UUID client) {
        gameLogic.onPlayerNameSelect(event, client);
    }

    @SbMessageListener
    public void onGameStart(SbStartGame event, UUID client) {
        if (gameLogic.isGameInProgress())
            return;

        var cbGameStart = gameLogic.onGameStart(client);

        var cbMessage = CbMessage
                .newBuilder()
                .setGameStart(cbGameStart)
                .build();

        var connectedPlayers = gameSocketHandler.getSessions();
        for (UUID player : connectedPlayers) {
            gameSocketHandler.sendCbMessage(player, cbMessage);
        }

        System.out.println("stopwatch started");
        this.gameStartMillis.set(System.currentTimeMillis());

        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                GameLogicEvents.this.sendCbTimerTick();
            }
        }, TIMER_INTERVAL_MILLIS, TIMER_INTERVAL_MILLIS);
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

        var connectedPlayers = gameSocketHandler.getSessions();
        for (UUID player : connectedPlayers) {
            gameSocketHandler.sendCbMessage(player, cbMessage);
        }
    }
}