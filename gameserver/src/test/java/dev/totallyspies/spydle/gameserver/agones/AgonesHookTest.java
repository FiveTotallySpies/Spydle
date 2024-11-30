package dev.totallyspies.spydle.gameserver.agones;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import agones.dev.sdk.Sdk;
import dev.totallyspies.spydle.shared.model.GameServer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import net.infumia.agones4j.Agones;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

public class AgonesHookTest {

  @Mock private Agones agones;

  @Mock private CompletableFuture<Sdk.GameServer> gameServerFuture;

  @Mock private Sdk.GameServer sdkGameServer;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testConstructor_Success() throws ExecutionException, InterruptedException {
    // Arrange
    String gameServerName = "game-server-12345";
    String expectedRoomCode = "12345";
    int expectedPort = 7777;
    String expectedAddress = "127.0.0.1";

    // Mock the Agones object to return a Future
    when(agones.getGameServerFuture()).thenReturn(gameServerFuture);
    when(gameServerFuture.get()).thenReturn(sdkGameServer);

    // Mock the Sdk.GameServer object
    Sdk.GameServer.ObjectMeta objectMeta =
        Sdk.GameServer.ObjectMeta.newBuilder().setName(gameServerName).build();
    when(sdkGameServer.getObjectMeta()).thenReturn(objectMeta);

    Sdk.GameServer.Status.Port port =
        Sdk.GameServer.Status.Port.newBuilder().setPort(expectedPort).build();
    Sdk.GameServer.Status status =
        Sdk.GameServer.Status.newBuilder().setAddress(expectedAddress).addPorts(port).build();
    when(sdkGameServer.getStatus()).thenReturn(status);

    // Mock RoomCodeUtils.getFromName() if necessary
    // Assuming it returns the substring after the last hyphen
    // Since it's a simple static utility method, we can use the real implementation or assume the
    // behavior

    // Act
    AgonesHook agonesHook = new AgonesHook(agones);

    // Assert
    GameServer currentGameServer = agonesHook.getCurrentGameServer();
    assertNotNull(currentGameServer);
    assertEquals(expectedAddress, currentGameServer.getAddress());
    assertEquals(expectedPort, currentGameServer.getPort());
    assertEquals(gameServerName, currentGameServer.getName());
    assertEquals(expectedRoomCode, currentGameServer.getRoomCode());
    assertTrue(currentGameServer.isPublicRoom());
    assertEquals(GameServer.State.READY, currentGameServer.getState());

    // Verify that agones.ready() was called
    verify(agones).ready();
  }

  @Test
  public void testConstructor_ExecutionException() throws ExecutionException, InterruptedException {
    // Arrange
    when(agones.getGameServerFuture()).thenReturn(gameServerFuture);
    when(gameServerFuture.get()).thenThrow(new ExecutionException(new Exception("Test exception")));

    // Act & Assert
    ExecutionException exception =
        assertThrows(
            ExecutionException.class,
            () -> {
              new AgonesHook(agones);
            });
    assertEquals("java.lang.Exception: Test exception", exception.getMessage());
  }

  @Test
  public void testConstructor_InterruptedException()
      throws ExecutionException, InterruptedException {
    // Arrange
    when(agones.getGameServerFuture()).thenReturn(gameServerFuture);
    when(gameServerFuture.get()).thenThrow(new InterruptedException("Test exception"));

    // Act & Assert
    InterruptedException exception =
        assertThrows(
            InterruptedException.class,
            () -> {
              new AgonesHook(agones);
            });
    assertEquals("Test exception", exception.getMessage());
  }
}
