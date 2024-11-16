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
    public void onGameStart(SbStartGame event, UUID client) {
        gameLogic.gameStart(gameSocketHandler.getSessions());
        gameSocketHandler.broadcastCbMessage(gameStartMessage());

        gameStartMillis.set(System.currentTimeMillis());
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                GameLogicEvents.this.sendCbTimerTick();
            }
        }, TIMER_INTERVAL_MILLIS, TIMER_INTERVAL_MILLIS);

        gameLogic.newTurn();
        gameSocketHandler.broadcastCbMessage(newTurnMessage());
    }

    @SbMessageListener
    public void onGuess(SbGuess event, UUID client) {
        var input = event.getGuessedWord();

        boolean guessCorrect = gameLogic.guess(input); /* makes a turn if a guess is correct */

        gameSocketHandler.broadcastCbMessage(guessMessage(input, guessCorrect));
        if (guessCorrect) {
            gameSocketHandler.broadcastCbMessage(newTurnMessage());
        }
    }

    private CbMessage gameStartMessage()
    {
        var players = gameLogic.getPlayers();
        var gameTime = GameLogic.TOTAL_GAME_TIME_SECONDS;

        var msgPlayers = players.stream().map(player ->
                dev.totallyspies.spydle.shared.proto.messages.Player.newBuilder()
                        .setPlayerName(player.getName())
                        .setScore(player.getScore())
                        .build()
        ).toList();

        return CbMessage
                .newBuilder()
                .setGameStart(
                        CbGameStart.newBuilder()
                                .setTotalGameTimeSeconds(gameTime)
                                .addAllPlayers(msgPlayers)
                ).build();
    }

    private CbMessage guessMessage(String guess, boolean correct) {
        return CbMessage
                .newBuilder()
                .setGuessResult(
                        CbGuessResult
                                .newBuilder()
                                .setGuess(guess)
                                .setCorrect(correct)
                )
                .build();
    }

    private CbMessage newTurnMessage() {
        return CbMessage
                .newBuilder()
                .setNewTurn(
                        CbNewTurn.newBuilder()
                                .setAssignedString(gameLogic.getCurrentSubString())
                                .setCurrentPlayerName(gameLogic.getCurrentPlayer().getName())
                )
                .build();
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

        gameSocketHandler.broadcastCbMessage(cbMessage);
    }
}