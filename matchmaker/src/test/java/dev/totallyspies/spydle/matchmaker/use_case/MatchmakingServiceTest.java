package dev.totallyspies.spydle.matchmaker.use_case;

import dev.totallyspies.spydle.matchmaker.config.GameServerRepository;
import dev.totallyspies.spydle.matchmaker.config.SessionRepository;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MatchmakingServiceTest {

    private MatchmakingService matchmakingService;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private AgonesAllocatorService allocator;

    @Mock
    private GameServerRepository gameServerRepository;

    private final GameServer fakeGameServer = new GameServer("", 0, "", "", false, GameServer.State.WAITING);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        matchmakingService = new MatchmakingService(sessionRepository, allocator, gameServerRepository);
    }

    @Test
    public void testCreateGame_Success() {
        UUID clientId = UUID.randomUUID();
        String playerName = "Player1";

        when(sessionRepository.sessionExists(clientId)).thenReturn(false);
        when(allocator.awaitAllocation(anyInt())).thenReturn(fakeGameServer);

        GameServer result = matchmakingService.createGame(clientId, playerName);

        assertEquals(fakeGameServer, result);
        verify(sessionRepository).saveSession(any(ClientSession.class));
    }

    @Test
    public void testCreateGame_AlreadyInGame() {
        UUID clientId = UUID.randomUUID();
        String playerName = "Player1";

        when(sessionRepository.sessionExists(clientId)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> matchmakingService.createGame(clientId, playerName));

        verify(sessionRepository, never()).saveSession(any(ClientSession.class));
    }

    @Test
    public void testJoinGame_Success() {
        UUID clientId = UUID.randomUUID();
        String playerName = "Player1";

        when(sessionRepository.sessionExists(clientId)).thenReturn(false);

        matchmakingService.joinGame(clientId, playerName, fakeGameServer);

        verify(sessionRepository).saveSession(any(ClientSession.class));
    }

    @Test
    public void testJoinGame_AlreadyInGame() {
        UUID clientId = UUID.randomUUID();
        String playerName = "Player1";

        when(sessionRepository.sessionExists(clientId)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> matchmakingService.joinGame(clientId, playerName, fakeGameServer));

        verify(sessionRepository, never()).saveSession(any(ClientSession.class));
    }

    @Test
    public void testListGames() {
        GameServer gameServer1 = mock(GameServer.class);
        GameServer gameServer2 = mock(GameServer.class);

        when(gameServerRepository.getGameServers()).thenReturn(List.of(gameServer1, gameServer2));

        when(gameServer1.isPublicRoom()).thenReturn(true);
        when(gameServer1.getState()).thenReturn(GameServer.State.WAITING);
        when(gameServer1.getRoomCode()).thenReturn("ROOM1");

        when(gameServer2.isPublicRoom()).thenReturn(false);

        List<String> games = matchmakingService.listGames();

        assertEquals(1, games.size());
        assertEquals("ROOM1", games.get(0));
    }

}