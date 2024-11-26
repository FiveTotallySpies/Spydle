package dev.totallyspies.spydle.gameserver.game;

import dev.totallyspies.spydle.gameserver.message.GameSocketHandler;
import dev.totallyspies.spydle.gameserver.message.SbMessageListener;
import dev.totallyspies.spydle.gameserver.message.session.SessionCloseEvent;
import dev.totallyspies.spydle.gameserver.message.session.SessionOpenEvent;
import dev.totallyspies.spydle.gameserver.storage.CurrentGameServerConfiguration;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import dev.totallyspies.spydle.shared.proto.messages.*;
import dev.totallyspies.spydle.shared.proto.messages.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class GameLogicEvents {
    private final Logger logger = LoggerFactory.getLogger(GameLogicEvents.class);
    private final Timer timer = new Timer();

    @Autowired
    private GameLogic gameLogic;

    @Autowired
    private GameSocketHandler gameSocketHandler;

    @Autowired
    public CurrentGameServerConfiguration gameServerConfiguration;

    @Autowired
    public GameServer gameServer;

    @EventListener(SessionOpenEvent.class)
    public void onSessionOpen() {
        if (gameServer.getState() == GameServer.State.READY) { // This is our first client! Set us to WAITING
            gameServer.setState(GameServer.State.WAITING);
            gameServerConfiguration.updateInStorage();
        }
        broadcastPlayers();
    }

    @EventListener(SessionCloseEvent.class)
    public void onSessionClose() {
        broadcastPlayers();
    }

    @SbMessageListener
    public void onGameStart(SbStartGame event, UUID client) {
        /* 1. Change the game server state. */
        if (gameServer.getState() != GameServer.State.WAITING) {
            return;
        }
        gameServer.setState(GameServer.State.PLAYING);
        gameServerConfiguration.updateInStorage();

        /* 2. Update the game logic state, send the game start message to every player. */
        /* Sessions should be sorted */
        gameLogic.gameStart(getSessionsSorted(), event.getTotalGameTimeSeconds(), event.getTurnTimeSeconds());
        gameSocketHandler.broadcastCbMessage(gameStartMessage());

        /* 3. Start the game timer. */
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                GameLogicEvents.this.onTimerTick();
            }
        }, 0, 1000);

        /* 4. Make a new turn. */
        gameLogic.newTurn();
        gameSocketHandler.broadcastCbMessage(newTurnMessage());
    }

    @SbMessageListener
    public void onGuess(SbGuess event, UUID client) {
        if (!gameLogic.isPlayerTurn(client)) {
            logger.info("A player made a guess when it's not their turn! client that made a guess: {}", client);
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
            broadcastPlayers();
        }
    }

    private void onTimerTick() {
        gameLogic.updateTickTime();
        long gamePassedMillis = gameLogic.getTickTime() - gameLogic.getGameStartMillis();
        long gameMillisLeft = gameLogic.getTotalGameTimeMillis() - gamePassedMillis;

        long turnPassedMillis = gameLogic.getTickTime() - gameLogic.getLastTurnStartMillis();
        long turnMillisLeft = Math.min(gameLogic.getTurnTimeMillis() - turnPassedMillis, gameMillisLeft);

        if (gameMillisLeft <= 0) {
            this.onGameEnd();
            this.timer.cancel();
            return;
        }

        if (turnMillisLeft <= 0) {
            gameLogic.newTurn();
            gameSocketHandler.broadcastCbMessage(newTurnMessage());
            /* We made a move and need to reset how many seconds are left for a turn */
            turnPassedMillis = gameLogic.getTickTime() - gameLogic.getLastTurnStartMillis();
            turnMillisLeft = Math.min(gameLogic.getTurnTimeMillis() - turnPassedMillis, gameMillisLeft);
        }

        var gameSecondsLeft = (int) Math.round(gameMillisLeft / 1000.0);
        var turnSecondsLeft = (int) Math.round(turnMillisLeft / 1000.0);

        var cbMessage = CbMessage
                .newBuilder()
                .setTimerTick(CbTimerTick
                                .newBuilder()
                                .setGameTimeLeftSeconds(gameSecondsLeft)
                                .setTurnTimeLeftSeconds(turnSecondsLeft))
                .build();

        gameSocketHandler.broadcastCbMessage(cbMessage);
    }

    private void onGameEnd() {
        /* The game timer has ended, we can use the players list from the game logic*/
        var players = gameLogic.getPlayersScoreSorted()
                .stream()
                .map(this::playerMessage)
                .toList();

        gameSocketHandler.broadcastCbMessage(gameEndMessage(players));
    }

    private void broadcastPlayers() {
        if (gameServer.getState().equals(GameServer.State.PLAYING)) {
            /* If the game has started, use the player list from the game logic */
            var players = gameLogic.getPlayers()
                    .stream()
                    .map(this::playerMessage)
                    .toList();

            gameSocketHandler.broadcastCbMessage(updatePlayerListMessage(players));
        } else {
            /* Otherwise send a list of connected sessions */
            var players = getSessionsSorted()
                    .stream().map(
                            clientSession ->
                                    Player.newBuilder()
                                            .setPlayerName(clientSession.getPlayerName())
                                            .setScore(0)
                                            .build()
                    ).toList();
            gameSocketHandler.broadcastCbMessage(updatePlayerListMessage(players));
        }
    }

    private List<ClientSession> getSessionsSorted() {
        var sessions = gameSocketHandler.getSessions();
        sessions.sort(Comparator.comparing(ClientSession::getPlayerName));
        return sessions;
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
                                .setTurnTimeSeconds(gameLogic.getTurnTimeSeconds())
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
                                .setCurrentPlayer(Player.newBuilder()
                                        .setPlayerName(gameLogic.getCurrentPlayer().getName())
                                        .setScore(gameLogic.getCurrentPlayer().getScore()))
                )
                .build();
    }

    private CbMessage gameEndMessage(List<Player> players) {
        return CbMessage
                .newBuilder()
                .setGameEnd(
                        CbGameEnd.newBuilder()
                                .addAllPlayers(players)
                )
                .build();
    }

    private Player playerMessage(dev.totallyspies.spydle.gameserver.game.Player player) {
        return dev.totallyspies.spydle.shared.proto.messages.Player.newBuilder()
                .setPlayerName(player.getName())
                .setScore(player.getScore())
                .build();
    }

    private CbMessage updatePlayerListMessage(List<Player> players) {
        return CbMessage.newBuilder().setUpdatePlayerList(
                CbUpdatePlayerList.newBuilder().addAllPlayers(players)
        ).build();
    }
}