package dev.totallyspies.spydle.matchmaker.use_case;

import dev.totallyspies.spydle.matchmaker.config.GameServerRepository;
import dev.totallyspies.spydle.matchmaker.config.SessionRepository;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
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
    private final ClientSession fakeClientSession = new ClientSession(UUID.randomUUID(), fakeGameServer, "player", ClientSession.State.ASSIGNED);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        matchmakingService = new MatchmakingService(sessionRepository, allocator, gameServerRepository);
    }

    @Test
    public void testCreateGame_Success() {
        UUID clientId = UUID.randomUUID();
        String playerName = "Player1";

        // Mock behavior
        when(sessionRepository.sessionExists(clientId)).thenReturn(false);
        when(allocator.awaitAllocation(anyInt())).thenReturn(fakeGameServer);

        // Call method
        GameServer result = matchmakingService.createGame(clientId, playerName, 1);

        // Verify
        assertEquals(fakeGameServer, result);
        verify(sessionRepository).saveSession(argThat(session ->
                session.getClientId().equals(clientId)
                        && session.getGameServer().equals(fakeGameServer)
                        && session.getPlayerName().equals(playerName)
                        && session.getState() == ClientSession.State.ASSIGNED
        ));
    }

    @Test
    public void testCreateGame_ClientAlreadyConnected() {
        UUID clientId = UUID.randomUUID();
        String playerName = "Player1";

        // Mock existing session in CONNECTED state
        when(sessionRepository.sessionExists(clientId)).thenReturn(true);
        ClientSession existingSession = fakeClientSession.toBuilder().state(ClientSession.State.CONNECTED).build();
        when(sessionRepository.getSession(clientId)).thenReturn(existingSession);

        // Call method and expect exception
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            matchmakingService.createGame(clientId, playerName, 1);
        });

        assertEquals("Client is already connected to a game.", exception.getMessage());
        verify(sessionRepository, never()).saveSession(any());
        verify(sessionRepository, never()).deleteSession(clientId);
    }

    @Test
    public void testCreateGame_ClientAssignedSession() {
        UUID clientId = UUID.randomUUID();
        String playerName = "Player1";

        // Mock existing session in ASSIGNED state
        when(sessionRepository.sessionExists(clientId)).thenReturn(true);
        when(sessionRepository.getSession(clientId)).thenReturn(fakeClientSession);
        when(allocator.awaitAllocation(anyInt())).thenReturn(fakeGameServer);

        // Call method
        GameServer result = matchmakingService.createGame(clientId, playerName, 1);

        // Verify
        assertEquals(fakeGameServer, result);
        verify(sessionRepository).deleteSession(clientId);
        verify(sessionRepository).saveSession(argThat(session ->
                session.getClientId().equals(clientId)
                        && session.getGameServer().equals(fakeGameServer)
                        && session.getPlayerName().equals(playerName)
                        && session.getState() == ClientSession.State.ASSIGNED
        ));
    }

    @Test
    public void testJoinGame_Success() {
        UUID clientId = UUID.randomUUID();
        String playerName = "Player1";

        // Mock behavior
        when(sessionRepository.sessionExists(clientId)).thenReturn(false);

        // Call method
        matchmakingService.joinGame(clientId, playerName, fakeGameServer);

        // Verify
        verify(sessionRepository).saveSession(argThat(session ->
                session.getClientId().equals(clientId)
                        && session.getGameServer().equals(fakeGameServer)
                        && session.getPlayerName().equals(playerName)
                        && session.getState() == ClientSession.State.ASSIGNED
        ));
    }

    @Test
    public void testJoinGame_ClientAlreadyConnected() {
        UUID clientId = UUID.randomUUID();
        String playerName = "Player1";

        // Mock existing session in CONNECTED state
        when(sessionRepository.sessionExists(clientId)).thenReturn(true);
        ClientSession existingSession = fakeClientSession.toBuilder().state(ClientSession.State.CONNECTED).build();
        when(sessionRepository.getSession(clientId)).thenReturn(existingSession);

        // Call method and expect exception
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            matchmakingService.joinGame(clientId, playerName, fakeGameServer);
        });

        assertEquals("Client is already connected to a game.", exception.getMessage());
        verify(sessionRepository, never()).saveSession(any());
        verify(sessionRepository, never()).deleteSession(clientId);
    }

    @Test
    public void testJoinGame_ClientAssignedSession() {
        UUID clientId = UUID.randomUUID();
        String playerName = "Player1";

        // Mock existing session in ASSIGNED state
        when(sessionRepository.sessionExists(clientId)).thenReturn(true);
        when(sessionRepository.getSession(clientId)).thenReturn(fakeClientSession);

        // Call method
        matchmakingService.joinGame(clientId, playerName, fakeGameServer);

        // Verify
        verify(sessionRepository).deleteSession(clientId);
        verify(sessionRepository).saveSession(argThat(session ->
                session.getClientId().equals(clientId)
                        && session.getGameServer().equals(fakeGameServer)
                        && session.getPlayerName().equals(playerName)
                        && session.getState() == ClientSession.State.ASSIGNED
        ));
    }

    @Test
    public void testListGames() {
        GameServer gameServer1 = mock(GameServer.class);
        GameServer gameServer2 = mock(GameServer.class);
        GameServer gameServer3 = mock(GameServer.class);

        // Mock list of game servers
        when(gameServerRepository.getGameServers()).thenReturn(List.of(gameServer1, gameServer2, gameServer3));

        // Configure gameServer1 to match criteria
        when(gameServer1.isPublicRoom()).thenReturn(true);
        when(gameServer1.getState()).thenReturn(GameServer.State.WAITING);
        when(gameServer1.getRoomCode()).thenReturn("ROOM1");

        // Configure gameServer2 to not match criteria
        when(gameServer2.isPublicRoom()).thenReturn(false);

        // Configure gameServer3 to not match criteria
        when(gameServer3.isPublicRoom()).thenReturn(true);
        when(gameServer3.getState()).thenReturn(GameServer.State.PLAYING);

        // Call method
        List<String> roomCodes = matchmakingService.listGames();

        // Verify
        assertEquals(1, roomCodes.size());
        assertEquals("ROOM1", roomCodes.get(0));
    }
}