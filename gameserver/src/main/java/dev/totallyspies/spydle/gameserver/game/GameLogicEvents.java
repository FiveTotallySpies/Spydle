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

@Component
public class GameLogicEvents {
    private final int TIMER_INTERVAL_MILLIS = 1000;
    private final AtomicInteger TOTAL_GAME_TIME_SECONDS = new AtomicInteger(30);
    private final Timer timer = new Timer();
    private final StopWatch stopWatch = new StopWatch();

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

        synchronized (this.stopWatch) {
            this.stopWatch.start();
        }

        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                GameLogicEvents.this.sendCbTimerTick();
            }
        }, TIMER_INTERVAL_MILLIS, TIMER_INTERVAL_MILLIS);
    }

    private void sendCbTimerTick() {
        double secondsPassed;
        synchronized (this.stopWatch) {
            secondsPassed = this.stopWatch.getTotalTimeSeconds();
        }

        var secondsLeft = (int) Math.round(TOTAL_GAME_TIME_SECONDS.doubleValue() - secondsPassed);

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