package dev.totallyspies.spydle.gameserver.game;

import dev.totallyspies.spydle.gameserver.storage.CurrentGameServerConfig;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GameLogicTests {
    private GameLogic gameLogic;

    private Collection<ClientSession> clientSessions;
    private final GameServer fakeGameServer = new GameServer("", 0, "", "", false, GameServer.State.WAITING);
    private UUID uuid1, uuid2, uuid3;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        gameLogic = spy(new GameLogic(0));

        var validWords = new TreeSet<String>();
        validWords.add("aaa"); validWords.add("bb"); validWords.add("bbb"); validWords.add("ccc");

        var substrings = new ArrayList<String>();
        substrings.add("a"); substrings.add("b"); substrings.add("c");

        try {
            when(gameLogic.parseWords()).thenReturn(validWords);
            when(gameLogic.parseSubstrings()).thenReturn(substrings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        clientSessions = new ArrayList<>();
        uuid1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        uuid2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        uuid3 = UUID.fromString("33333333-3333-3333-3333-333333333333");
        clientSessions.add(
                new ClientSession(uuid1, fakeGameServer, "player1", ClientSession.State.CONNECTED)
        );
        clientSessions.add(
                new ClientSession(uuid2, fakeGameServer, "player2", ClientSession.State.CONNECTED)
        );
        clientSessions.add(
                new ClientSession(uuid3, fakeGameServer, "player3", ClientSession.State.CONNECTED)
        );
    }

    @Test
    public void testGameTurnTime() {
        when(gameLogic.isValidTotalGameTime(100)).thenReturn(true);
        when(gameLogic.isValidTotalGameTime(20)).thenReturn(true);
        gameLogic.gameStart(this.clientSessions, 100, 20);
        assertEquals(100, gameLogic.getTotalGameTimeSeconds());
        assertEquals(100*1000L, gameLogic.getTotalGameTimeMillis());
        assertEquals(20, gameLogic.getTurnTimeSeconds());
        assertEquals(20*1000L, gameLogic.getTurnTimeMillis());
    }

    @Test
    public void testTurns() {
        gameLogic.gameStart(this.clientSessions, -1, -1);

        assertFalse(gameLogic.isPlayerTurn(uuid1));
        assertTrue(gameLogic.isPlayerTurn(uuid2));
        assertFalse(gameLogic.isPlayerTurn(uuid3));

        gameLogic.newTurn();

        assertFalse(gameLogic.isPlayerTurn(uuid1));
        assertFalse(gameLogic.isPlayerTurn(uuid2));
        assertTrue(gameLogic.isPlayerTurn(uuid3));

        gameLogic.newTurn();

        assertTrue(gameLogic.isPlayerTurn(uuid1));
        assertFalse(gameLogic.isPlayerTurn(uuid2));
        assertFalse(gameLogic.isPlayerTurn(uuid3));

        gameLogic.newTurn();

        assertFalse(gameLogic.isPlayerTurn(uuid1));
        assertTrue(gameLogic.isPlayerTurn(uuid2));
        assertFalse(gameLogic.isPlayerTurn(uuid3));
    }

    @Test
    public void testGuess() {
        gameLogic.gameStart(this.clientSessions, -1, -1);
        assertTrue(gameLogic.isPlayerTurn(uuid2));
        System.out.println("testGuess() substring:" + gameLogic.getCurrentSubString()); // current substring: a

        String veryLongGuess = new String(new char[100]).replace("\0", "q");
        assertFalse(gameLogic.guess(veryLongGuess)); // not a word, has more than 50 characters
        assertFalse(gameLogic.guess("aaaaa")); // not a word, wrong guess
        assertFalse(gameLogic.guess("qqq")); // doesn't contain the substring, wrong guess
        assertTrue(gameLogic.guess("aaa")); // right guess
    }

    @Test
    public void testPlayers() {
        gameLogic.gameStart(this.clientSessions, -1, -1);
        var players = gameLogic.getPlayers();
        assertEquals(3, players.size());
        assertTrue(players.contains(new Player(uuid1, "player1", 0)));
        assertTrue(players.contains(new Player(uuid2, "player2", 0)));
        assertTrue(players.contains(new Player(uuid3, "player3", 0)));
    }

    @Test
    public void testScore() {
        gameLogic.gameStart(this.clientSessions, -1, -1);
        gameLogic.newTurn();
        assertTrue(gameLogic.isPlayerTurn(uuid3));
        System.out.println("testScore() substring:" + gameLogic.getCurrentSubString()); // current substring: b
        gameLogic.guess("bbb"); // correct guess, 3 points

        assertTrue(gameLogic.isPlayerTurn(uuid1));
        System.out.println("testScore() substring:" + gameLogic.getCurrentSubString()); // current substring: b
        gameLogic.guess("bb"); // correct guess, 2 points

        assertTrue(gameLogic.isPlayerTurn(uuid2));
        System.out.println("testScore() substring:" + gameLogic.getCurrentSubString()); // current substring: c
        gameLogic.guess("qqqq"); // wrong guess, 0 points

        var playersByScore = gameLogic.getPlayersScoreSorted();
        assertEquals(3, playersByScore.size());
        assertEquals(new Player(uuid3, "player3", 3), playersByScore.get(0));
        assertEquals(new Player(uuid1, "player1", 2), playersByScore.get(1));
        assertEquals(new Player(uuid2, "player2", 0), playersByScore.get(2));
    }

    @Test
    public void testTime() {
        Clock clock = mock(Clock.class);
        try (MockedStatic<Clock> clockMock = mockStatic(Clock.class)) {
            clockMock.when(Clock::getInstance).thenReturn(clock);
            when(clock.currentTimeMillis()).thenReturn(1000L);
            gameLogic.updateTickTime();

            /* gameStart should update gameStartMillis, time is now 1000L */
            gameLogic.gameStart(this.clientSessions, -1, -1);

            when(clock.currentTimeMillis()).thenReturn(2000L);
            gameLogic.updateTickTime();
            assertEquals(1000L, gameLogic.getGameStartMillis());

            /* time is now 3000L, testing a new turn time */
            when(clock.currentTimeMillis()).thenReturn(3000L);
            gameLogic.updateTickTime();
            gameLogic.newTurn();
            assertEquals(3000L, gameLogic.getLastTurnStartMillis());

            when(clock.currentTimeMillis()).thenReturn(4000L);
            gameLogic.updateTickTime();
            assertEquals(4000L, gameLogic.getTickTime());
        }
    }
}
