package dev.totallyspies.spydle.gameserver.game;

import dev.totallyspies.spydle.gameserver.message.GameSocketHandler;
import dev.totallyspies.spydle.gameserver.message.SbMessageListener;
import dev.totallyspies.spydle.gameserver.message.session.SessionCloseEvent;
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

    @EventListener(SessionCloseEvent.class)
    public void onSessionClose() {
        broadcastPlayers();
    }

    @SbMessageListener
    public void onGameStart(SbStartGame event, UUID client) {
        /* 1. Change the game server state. */
        if (currentGameServer.getState() != GameServer.State.WAITING) {
            return;
        }
        currentGameServer.setState(GameServer.State.PLAYING);
        currentGameServerConfiguration.updateInStorage();

        /* 2. Update the game logic state, send the game start message to every player. */
        gameLogic.gameStart(gameSocketHandler.getSessions(), event.getTotalGameTimeSeconds());
        gameSocketHandler.broadcastCbMessage(gameStartMessage());

        /* 3. Start the game timer. */
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
        }, gameLogic.getTotalGameTimeMillis());

        /* 4. Make a new turn. */
        gameLogic.newTurn();
        gameSocketHandler.broadcastCbMessage(newTurnMessage());
    }

    @SbMessageListener
    public void onGuess(SbGuess event, UUID client) {
        if (!gameLogic.isPlayerTurn(client)) {
            return;
        }

        var playerName = gameLogic.getCurrentPlayer().getName();
        var input = event.getGuessedWord();
        /* makes a turn, adds the score if a guess is correct */
        boolean guessCorrect = gameLogic.guess(input);

        gameSocketHandler.broadcastCbMessage(guessMessage(input, guessCorrect, playerName));
        if (guessCorrect) {
            /* .guess() updated a turn and a score */
            gameSocketHandler.broadcastCbMessage(newTurnMessage());
            gameSocketHandler.broadcastCbMessage(updatePlayerListMessage());
        }
    }

    private void onTimerTick(TimerTask task) {
        long millisPassed = System.currentTimeMillis() - gameStartMillis.get();
        long millisLeft = gameLogic.getTotalGameTimeMillis() - millisPassed;
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

    private void broadcastPlayers() {
        gameSocketHandler.broadcastCbMessage(updatePlayerListMessage());
    }

    private CbMessage gameStartMessage()
    {
        var players = gameLogic.getPlayers();

        var msgPlayers = players.stream().map(this::playerMessage).toList();

        return CbMessage
                .newBuilder()
                .setGameStart(
                        CbGameStart.newBuilder()
                                .setTotalGameTimeSeconds(gameLogic.getTotalGameTimeSeconds())
                                .addAllPlayers(msgPlayers)
                ).build();
    }

    private CbMessage guessMessage(String guess, boolean correct, String playerName) {
        return CbMessage
                .newBuilder()
                .setGuessResult(
                        CbGuessResult
                                .newBuilder()
                                .setPlayerName(playerName)
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

    private CbMessage gameEndMessage() {
        var winner = gameLogic.getWinner();
        return CbMessage
                .newBuilder()
                .setGameEnd(
                        CbGameEnd.newBuilder()
                                .setWinner(
                                        playerMessage(winner)
                                )
                )
                .build();
    }

    private Player playerMessage(dev.totallyspies.spydle.gameserver.game.Player player) {
        return dev.totallyspies.spydle.shared.proto.messages.Player.newBuilder()
                .setPlayerName(player.getName())
                .setScore(player.getScore())
                .build();
    }

    private CbMessage updatePlayerListMessage() {
        var players = gameLogic
                .getPlayers()
                .stream()
                .map(this::playerMessage)
                .toList();

        return CbMessage.newBuilder().setUpdatePlayerList(
                CbUpdatePlayerList.newBuilder().addAllPlayers(players)
        ).build();
    }
}