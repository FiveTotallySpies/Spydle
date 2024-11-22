package dev.totallyspies.spydle.gameserver.game;

import dev.totallyspies.spydle.gameserver.message.GameSocketHandler;
import dev.totallyspies.spydle.gameserver.message.SbMessageListener;
import dev.totallyspies.spydle.gameserver.message.session.SessionOpenEvent;
import dev.totallyspies.spydle.gameserver.storage.CurrentGameServerConfiguration;
import dev.totallyspies.spydle.shared.model.GameServer;
import dev.totallyspies.spydle.shared.proto.messages.*;
import dev.totallyspies.spydle.shared.proto.messages.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class GameLogicEvents {
    private static final long TIMER_INTERVAL_MILLIS = 1000;
    private static final long TOTAL_GAME_TIME_MILLIS = 30000;

    private final Timer timer = new Timer();
    private final AtomicLong gameStartMillis = new AtomicLong(0);

    @Autowired
    private GameLogic gameLogic;

    @Autowired
    private GameSocketHandler gameSocketHandler;

    @Autowired
    public CurrentGameServerConfiguration currentGameServerConfiguration;

    @Autowired
    public GameServer currentGameServer;

    @EventListener(SessionOpenEvent.class)
    public void onSessionOpen() {
        if (currentGameServer.getState() == GameServer.State.READY) { // This is our first client! Set us to WAITING
            currentGameServer.setState(GameServer.State.WAITING);
            currentGameServerConfiguration.updateInStorage();
        }
    }

    @SbMessageListener
    public void onGameStart(SbStartGame event, UUID client) {
        /* 1. Change the game server state */
        if (currentGameServer.getState() != GameServer.State.WAITING) {
            return;
        }
        currentGameServer.setState(GameServer.State.PLAYING);
        currentGameServerConfiguration.updateInStorage();

        /* 2. Update the game logic state, send the game start message to every player. */
        gameLogic.gameStart(gameSocketHandler.getSessions());
        gameSocketHandler.broadcastCbMessage(gameStartMessage());

        /* 3. Start the game timer */
        gameStartMillis.set(System.currentTimeMillis());
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                GameLogicEvents.this.onTimerTick(this);
            }
        }, TIMER_INTERVAL_MILLIS, TIMER_INTERVAL_MILLIS);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                GameLogicEvents.this.onGameEnd();
            }
        }, TOTAL_GAME_TIME_MILLIS);

        /* 4. Make a new turn */
        gameLogic.newTurn();
        gameSocketHandler.broadcastCbMessage(newTurnMessage());
    }

    @SbMessageListener
    public void onGuess(SbGuess event, UUID client) {
        if (!gameLogic.isPlayerTurn(client)) {
            return;
        }

        var input = event.getGuessedWord();
        boolean guessCorrect = gameLogic.guess(input); /* makes a turn if a guess is correct */

        gameSocketHandler.broadcastCbMessage(guessMessage(input, guessCorrect));
        if (guessCorrect) {
            gameSocketHandler.broadcastCbMessage(newTurnMessage());
        }
    }

    private void onTimerTick(TimerTask task) {
        long millisPassed = System.currentTimeMillis() - gameStartMillis.get();
        long millisLeft = TOTAL_GAME_TIME_MILLIS - millisPassed;
        if (millisLeft < 0) {
            task.cancel();
            return;
        }

        var secondsLeft = (int) Math.round(millisLeft / 1000.0);

        var cbMessage = CbMessage
                .newBuilder()
                .setTimerTick(CbTimerTick.newBuilder().setTimeLeftSeconds(secondsLeft))
                .build();

        gameSocketHandler.broadcastCbMessage(cbMessage);
    }

    private void onGameEnd() {
        this.timer.cancel();
        gameSocketHandler.broadcastCbMessage(gameEndMessage());
    }

    private CbMessage gameStartMessage()
    {
        var players = gameLogic.getPlayers();
        var gameTime = GameLogic.TOTAL_GAME_TIME_SECONDS;

        var msgPlayers = players.stream().map(this::msgPlayer).toList();

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
                                .setCurrentPlayer(Player.newBuilder()
                                        .setPlayerName(gameLogic.getCurrentPlayer().getName())
                                        .setScore(gameLogic.getCurrentPlayer().getScore()))
                )
                .build();
    }

    private CbMessage gameEndMessage() {
        var winner = gameLogic.getWinner();
        return CbMessage
                .newBuilder()
                .setGameEnd(
                        CbGameEnd.newBuilder()
                                .setWinner(
                                        msgPlayer(winner)
                                )
                )
                .build();
    }

    private Player msgPlayer(dev.totallyspies.spydle.gameserver.game.Player player) {
        return dev.totallyspies.spydle.shared.proto.messages.Player.newBuilder()
                .setPlayerName(player.getName())
                .setScore(player.getScore())
                .build();
    }
}