package dev.totallyspies.spydle.gameserver.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.totallyspies.spydle.gameserver.agones.AgonesHook;
import dev.totallyspies.spydle.shared.SharedConstants;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationContext;

public class CurrentGameServerConfigTest {

  private final GameServer fakeGameServer =
      new GameServer("", 0, "", "", false, GameServer.State.WAITING);
  private CurrentGameServerConfig configuration;
  @Mock private ApplicationContext context;
  @Mock private GameServerStorage storage;
  @Mock private AgonesHook agonesHook;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    configuration = new CurrentGameServerConfig(context, storage);
  }

  @Test
  public void testCurrentAgonesGameServer() {

    when(agonesHook.getCurrentGameServer()).thenReturn(fakeGameServer);

    GameServer result = configuration.currentAgonesGameServer(agonesHook);

    assertEquals(fakeGameServer, result);
    verify(storage).storeGameServer(fakeGameServer);
  }

  @Test
  public void testCurrentLocalGameServer() {
    int containerPort = 8080;

    GameServer result = configuration.currentLocalGameServer(containerPort);

    assertEquals("localhost", result.getAddress());
    assertEquals(containerPort, result.getPort());
    assertEquals("gameserver-local", result.getName());
    assertEquals(SharedConstants.LOCAL_SERVER_ROOM_CODE, result.getRoomCode());
    assertFalse(result.isPublicRoom());
    assertEquals(GameServer.State.READY, result.getState());

    verify(storage).storeGameServer(result);
  }

  @Test
  public void testUpdateInStorage() {
    when(context.getBean(GameServer.class)).thenReturn(fakeGameServer);

    configuration.updateInStorage();

    verify(storage).storeGameServer(fakeGameServer);
  }
}
