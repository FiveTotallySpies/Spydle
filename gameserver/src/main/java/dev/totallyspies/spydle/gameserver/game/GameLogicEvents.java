package dev.totallyspies.spydle.gameserver.game;

import dev.totallyspies.spydle.gameserver.session.SessionCloseEvent;
import dev.totallyspies.spydle.gameserver.session.SessionOpenEvent;
import dev.totallyspies.spydle.gameserver.socket.GameSocketHandler;
import dev.totallyspies.spydle.gameserver.socket.SbMessageListener;
import dev.totallyspies.spydle.gameserver.storage.CurrentGameServerConfig;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import dev.totallyspies.spydle.shared.proto.messages.*;
import dev.totallyspies.spydle.shared.proto.messages.Player;
import jakarta.annotation.Nullable;
import java.util.*;
import net.infumia.agones4j.Agones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;

@Component
public class GameLogicEvents {
  private final Logger logger = LoggerFactory.getLogger(GameLogicEvents.class);
  private final Timer timer = new Timer();

  private final GameLogic gameLogic;

  private final GameSocketHandler gameSocketHandler;

  private final CurrentGameServerConfig gameServerConfiguration;

  private final GameServer gameServer;

  private final ConfigurableApplicationContext context;

  @Nullable private final Agones agones;

  public GameLogicEvents(
      GameLogic logic,
      GameSocketHandler handler,
      GameServer server,
      CurrentGameServerConfig config,
      ConfigurableApplicationContext context,
      @Autowired(required = false) Agones agones) {
    this.gameLogic = logic;
    this.gameSocketHandler = handler;
    this.gameServer = server;
    this.gameServerConfiguration = config;
    this.context = context;
    this.agones = agones;
  }

  @EventListener(SessionOpenEvent.class)
  public void onSessionOpen() {
    if (gameServer.getState()
        == GameServer.State.READY) { // This is our first client! Set us to WAITING
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
    gameLogic.gameStart(
        getSessionsSorted(), event.getTotalGameTimeSeconds(), event.getTurnTimeSeconds());
    gameSocketHandler.broadcastCbMessage(gameStartMessage());

    /* 3. Start the game timer. */
    timer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            GameLogicEvents.this.onTimerTick();
          }
        },
        1000,
        1000);

    /* Since the gameStart also makes a new turn, we need to send a newTurn message. */
    gameSocketHandler.broadcastCbMessage(newTurnMessage());
  }

  @SbMessageListener
  public void onGuess(SbGuess event, UUID client) {
    if (!gameLogic.isPlayerTurn(client)) {
      logger.info(
          "A player made a guess when it's not their turn! client that made a guess: {}", client);
      return;
    }

    var player =
        Player.newBuilder()
            .setPlayerName(gameLogic.getCurrentPlayer().getName())
            .setScore(gameLogic.getCurrentPlayer().getScore())
            .build();
    var input = event.getGuessedWord();
    /* makes a turn, adds the score if a guess is correct */
    boolean guessCorrect = gameLogic.guess(input);

    gameSocketHandler.broadcastCbMessage(guessMessage(input, guessCorrect, player));
    if (guessCorrect) {
      /* .guess() updated a turn and a score */
      gameSocketHandler.broadcastCbMessage(newTurnMessage());
      broadcastPlayers();
    }
  }

  @SbMessageListener
  public void onGuessUpdate(SbGuessUpdate event, UUID client) {
    if (!gameLogic.isPlayerTurn(client)) {
      logger.info(
          "A player made a guess update when it's not their turn! client that made a guess update: {}",
          client);
      return;
    }

    String typed = event.getGuessedWord();
    if (typed.length() > 50) {
      return;
    }

    var player =
        gameLogic.getPlayers().stream()
            .filter(target -> target.getId().equals(client))
            .findFirst()
            .orElse(null);
    if (player == null) {
      logger.info("A player has made a guess that we cannot find in the game logic!");
      return;
    }
    var wrappedPlayer =
        Player.newBuilder().setPlayerName(player.getName()).setScore(player.getScore()).build();

    gameSocketHandler.broadcastCbMessage(guessUpdateMessage(typed, wrappedPlayer));
  }

  public void onTimerTick() {
    gameLogic.updateTickTime();
    long gamePassedMillis = gameLogic.getTickTime() - gameLogic.getGameStartMillis();
    long gameMillisLeft = gameLogic.getTotalGameTimeMillis() - gamePassedMillis;

    long turnPassedMillis = gameLogic.getTickTime() - gameLogic.getLastTurnStartMillis();
    long turnMillisLeft =
        Math.min(gameLogic.getTurnTimeMillis() - turnPassedMillis, gameMillisLeft);

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

    var cbMessage =
        CbMessage.newBuilder()
            .setTimerTick(
                CbTimerTick.newBuilder()
                    .setGameTimeLeftSeconds(gameSecondsLeft)
                    .setTurnTimeLeftSeconds(turnSecondsLeft))
            .build();

    gameSocketHandler.broadcastCbMessage(cbMessage);
  }

  public void onGameEnd() {
    /* The game timer has ended, we can use the players list from the game logic*/
    var players = gameLogic.getPlayersScoreSorted().stream().map(this::playerMessage).toList();

    gameSocketHandler.broadcastCbMessage(gameEndMessage(players));

    // Shutdown
    gameSocketHandler.closeAllSessions(new CloseStatus(CloseStatus.NORMAL.getCode(), "Game over"));
    if (agones != null) {
      agones.shutdown(); // Shutdown server
    } else {
      context.close(); // Shutdown
    }
  }

  public void broadcastPlayers() {
    if (gameServer.getState().equals(GameServer.State.PLAYING)) {
      /* If the game has started, use the player list from the game logic */
      var players = gameLogic.getPlayers().stream().map(this::playerMessage).toList();

      gameSocketHandler.broadcastCbMessage(updatePlayerListMessage(players));
    } else {
      /* Otherwise send a list of connected sessions */
      var players =
          getSessionsSorted().stream()
              .map(
                  clientSession ->
                      Player.newBuilder()
                          .setPlayerName(clientSession.getPlayerName())
                          .setScore(0)
                          .build())
              .toList();
      gameSocketHandler.broadcastCbMessage(updatePlayerListMessage(players));
    }
  }

  public List<ClientSession> getSessionsSorted() {
    var sessions = gameSocketHandler.getSessions();
    sessions.sort(Comparator.comparing(ClientSession::getPlayerName));
    return sessions;
  }

  public CbMessage gameStartMessage() {
    var players = gameLogic.getPlayers();

    var msgPlayers = players.stream().map(this::playerMessage).toList();

    return CbMessage.newBuilder()
        .setGameStart(
            CbGameStart.newBuilder()
                .setTotalGameTimeSeconds(gameLogic.getTotalGameTimeSeconds())
                .setTurnTimeSeconds(gameLogic.getTurnTimeSeconds())
                .addAllPlayers(msgPlayers))
        .build();
  }

  public CbMessage guessMessage(String guess, boolean correct, Player player) {
    return CbMessage.newBuilder()
        .setGuessResult(
            CbGuessResult.newBuilder().setPlayer(player).setGuess(guess).setCorrect(correct))
        .build();
  }

  public CbMessage guessUpdateMessage(String guess, Player player) {
    return CbMessage.newBuilder()
        .setGuessUpdate(CbGuessUpdate.newBuilder().setGuess(guess).setPlayer(player).build())
        .build();
  }

  public CbMessage newTurnMessage() {
    return CbMessage.newBuilder()
        .setNewTurn(
            CbNewTurn.newBuilder()
                .setAssignedString(gameLogic.getCurrentSubString())
                .setCurrentPlayer(
                    Player.newBuilder()
                        .setPlayerName(gameLogic.getCurrentPlayer().getName())
                        .setScore(gameLogic.getCurrentPlayer().getScore())))
        .build();
  }

  public CbMessage gameEndMessage(List<Player> players) {
    return CbMessage.newBuilder().setGameEnd(CbGameEnd.newBuilder().addAllPlayers(players)).build();
  }

  public Player playerMessage(dev.totallyspies.spydle.gameserver.game.Player player) {
    return dev.totallyspies.spydle.shared.proto.messages.Player.newBuilder()
        .setPlayerName(player.getName())
        .setScore(player.getScore())
        .build();
  }

  public CbMessage updatePlayerListMessage(List<Player> players) {
    return CbMessage.newBuilder()
        .setUpdatePlayerList(CbUpdatePlayerList.newBuilder().addAllPlayers(players))
        .build();
  }
}
