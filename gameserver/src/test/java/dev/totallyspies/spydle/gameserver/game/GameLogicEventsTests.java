package dev.totallyspies.spydle.gameserver.game;

import dev.totallyspies.spydle.gameserver.socket.GameSocketHandler;
import dev.totallyspies.spydle.gameserver.storage.CurrentGameServerConfig;
import dev.totallyspies.spydle.shared.Clock;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import dev.totallyspies.spydle.shared.proto.messages.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class GameLogicEventsTests {

  private GameLogic gameLogic;
  private GameSocketHandler gameSocketHandler;
  private CurrentGameServerConfig gameServerConfig;
  private GameServer gameServer;
  private ConfigurableApplicationContext context;
  private GameLogicEvents gameLogicEvents;
  private UUID uuid1, uuid2, uuid3;
  private List<ClientSession> clientSessions;
  private SbStartGame gameStartEvent;

  @BeforeEach
  void setUp() {
    uuid1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    uuid2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    uuid3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

    gameLogic = spy(new GameLogic(0));
    gameSocketHandler = mock(GameSocketHandler.class);
    gameServerConfig = mock(CurrentGameServerConfig.class);
    gameServer = mock(GameServer.class);
    context = mock(ConfigurableApplicationContext.class);

    gameLogicEvents =
        spy(
            new GameLogicEvents(
                gameLogic,
                gameSocketHandler,
                gameServer,
                gameServerConfig,
                context,
                null // Agones is optional
                ));

    clientSessions = new ArrayList<>();
    clientSessions.add(
        new ClientSession(uuid1, gameServer, "player1", ClientSession.State.CONNECTED));
    clientSessions.add(
        new ClientSession(uuid2, gameServer, "player2", ClientSession.State.CONNECTED));
    clientSessions.add(
        new ClientSession(uuid3, gameServer, "player3", ClientSession.State.CONNECTED));

    when(gameSocketHandler.getSessions()).thenReturn(clientSessions);
    when(gameServer.getState()).thenReturn(GameServer.State.WAITING);

    gameStartEvent =
        SbStartGame.newBuilder().setTotalGameTimeSeconds(100).setTurnTimeSeconds(20).build();
  }

  @Test
  void testOnGameStart() {
    gameLogicEvents.onGameStart(gameStartEvent, uuid1);

    /* Game start message, new turn message, timer tick, 3 messages overall */
    verify(gameLogicEvents, atMost(1)).onTimerTick();
    verify(gameLogicEvents, times(1)).gameStartMessage();
    verify(gameLogicEvents, times(1)).newTurnMessage();
  }

  @Test
  void testGuessUpdate() {
    gameLogicEvents.onGameStart(gameStartEvent, uuid1);

    System.out.println("current player: " + gameLogic.getCurrentPlayer().getName()); // player2

    var guessUpdateEvent = SbGuessUpdate.newBuilder().setGuessedWord("word").build();

    gameLogicEvents.onGuessUpdate(guessUpdateEvent, uuid3); // wrong player
    verify(gameLogicEvents, never()).guessUpdateMessage(anyString(), any());

    gameLogicEvents.onGuessUpdate(guessUpdateEvent, uuid2); // right player
    verify(gameLogicEvents, times(1)).guessUpdateMessage(anyString(), any());
  }

  @Test
  void testGuess() {
    gameLogicEvents.onGameStart(gameStartEvent, uuid1);

    System.out.println(
        "testGuess(): current player: " + gameLogic.getCurrentPlayer().getName()); // player2

    var guessEvent = SbGuess.newBuilder().setGuessedWord("word").build();

    gameLogicEvents.onGuess(guessEvent, uuid3); // wrong player
    verify(gameLogicEvents, never()).guessMessage(anyString(), anyBoolean(), any());

    gameLogicEvents.onGuess(guessEvent, uuid2); // right player
    verify(gameLogicEvents, times(1)).guessMessage(anyString(), anyBoolean(), any());
  }

  @Test
  void testOnGameEnd() {
    gameLogicEvents.onGameStart(gameStartEvent, uuid1);
    gameLogicEvents.onGameEnd();
    verify(gameSocketHandler, times(1)).closeAllSessions(any());
  }

  @Test
  void testOnTimerTick() {
    /* Verifies that onGameEnd is called when time is up*/
    Clock clock = mock(Clock.class);
    try (MockedStatic<Clock> clockMock = mockStatic(Clock.class)) {
      clockMock.when(Clock::getInstance).thenReturn(clock);
      when(clock.currentTimeMillis()).thenReturn(1000L);
      gameLogicEvents.onGameStart(gameStartEvent, uuid1);

      when(clock.currentTimeMillis()).thenReturn(2000L);
      gameLogicEvents.onTimerTick();
      verify(gameLogicEvents, never()).onGameEnd();

      when(clock.currentTimeMillis()).thenReturn(20000000L); // 20000s passed
      gameLogicEvents.onTimerTick();
      verify(gameLogicEvents, times(1)).onGameEnd();
    }
  }

  @Test
  void testBroadcastPlayers() {
    /* In case the state is PLAYING,
       gameLogicEvents should use the gameLogic players list.
      Otherwise, it should get the list from current connected sessions.
    */
    when(gameServer.getState()).thenReturn(GameServer.State.WAITING);
    gameLogicEvents.broadcastPlayers();
    verify(gameLogic, never()).getPlayers();

    when(gameServer.getState()).thenReturn(GameServer.State.PLAYING);
    gameLogicEvents.broadcastPlayers();
    verify(gameLogic, times(1)).getPlayers();
  }
}
