package dev.totallyspies.spydle.gameserver.storage;

import static org.mockito.Mockito.*;

import dev.totallyspies.spydle.gameserver.socket.GameSocketHandler;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

public class GameShutdownHookTest {

  private final GameServer fakeGameServer =
      new GameServer("", 0, "", "", false, GameServer.State.WAITING);
  private final ClientSession fakeClientSession =
      new ClientSession(UUID.randomUUID(), fakeGameServer, "player", ClientSession.State.ASSIGNED);
  private GameShutdownHook gameShutdownHook;
  @Mock private GameServerStorage storage;
  @Mock private GameServer currentGameServer;
  @Mock private GameSocketHandler handler;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    gameShutdownHook = new GameShutdownHook(storage, currentGameServer, handler);
  }

  @Test
  public void testShutdownTask() {
    // Prepare test data
    ClientSession session1 = fakeClientSession.toBuilder().clientId(UUID.randomUUID()).build();
    ClientSession session2 = fakeClientSession.toBuilder().clientId(UUID.randomUUID()).build();
    List<ClientSession> sessions = Arrays.asList(session1, session2);

    when(handler.getSessions()).thenReturn(sessions);

    // Get the shutdown task and run it
    gameShutdownHook.onShutdown();

    // Verify that storage.deleteGameServer() is called
    verify(storage).deleteGameServer(currentGameServer);

    // Verify that storage.deleteClientSession() is called for each session
    verify(storage).deleteClientSession(session1);
    verify(storage).deleteClientSession(session2);
  }
}
