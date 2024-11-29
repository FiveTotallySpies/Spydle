package dev.totallyspies.spydle.gameserver.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.totallyspies.spydle.shared.SharedConstants;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

public class RedisStorageTest {

  private final GameServer fakeGameServer =
      new GameServer("", 0, "", "", false, GameServer.State.WAITING);
  private final ClientSession fakeClientSession =
      new ClientSession(UUID.randomUUID(), fakeGameServer, "player", ClientSession.State.ASSIGNED);
  private RedisStorage redisStorage;
  @Mock private RedisTemplate<String, Object> redisTemplate;
  @Mock private ValueOperations<String, Object> valueOperations;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    redisStorage = new RedisStorage(redisTemplate);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
  }

  @Test
  public void testStoreGameServer() {
    GameServer gameServer = fakeGameServer.toBuilder().roomCode("ROOM1").build();

    String key = SharedConstants.STORAGE_REDIS_GAME_SERVER_PREFIX + gameServer.getRoomCode();

    redisStorage.storeGameServer(gameServer);

    verify(valueOperations).set(key, gameServer);
  }

  @Test
  public void testGetGameServer_Found() {
    String roomCode = "ROOM1";
    GameServer gameServer = fakeGameServer.toBuilder().roomCode(roomCode).build();
    String key = SharedConstants.STORAGE_REDIS_GAME_SERVER_PREFIX + roomCode;

    when(valueOperations.get(key)).thenReturn(gameServer);

    GameServer result = redisStorage.getGameServer(roomCode);

    assertEquals(gameServer, result);
  }

  @Test
  public void testGetGameServer_NotFound() {
    String roomCode = "ROOM1";
    String key = SharedConstants.STORAGE_REDIS_GAME_SERVER_PREFIX + roomCode;

    when(valueOperations.get(key)).thenReturn(null);

    GameServer result = redisStorage.getGameServer(roomCode);

    assertNull(result);
  }

  @Test
  public void testGetGameServer_InvalidType() {
    String roomCode = "ROOM1";
    String key = SharedConstants.STORAGE_REDIS_GAME_SERVER_PREFIX + roomCode;

    when(valueOperations.get(key)).thenReturn("InvalidType");

    Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              redisStorage.getGameServer(roomCode);
            });

    String expectedMessage =
        "Redis storage for gameserver " + roomCode + " has invalid type java.lang.String";
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testDeleteGameServer() {
    String roomCode = "ROOM1";
    String key = SharedConstants.STORAGE_REDIS_GAME_SERVER_PREFIX + roomCode;

    redisStorage.deleteGameServer(roomCode);

    verify(redisTemplate).delete(key);
  }

  @Test
  public void testStoreClientSession() {
    UUID clientId = UUID.randomUUID();
    ClientSession session = fakeClientSession.toBuilder().clientId(clientId).build();

    String key = SharedConstants.STORAGE_REDIS_SESSION_PREFIX + clientId;

    redisStorage.storeClientSession(session);

    verify(valueOperations).set(key, session);
  }

  @Test
  public void testGetClientSession_Found() {
    UUID clientId = UUID.randomUUID();
    ClientSession session = fakeClientSession.toBuilder().clientId(clientId).build();
    String key = SharedConstants.STORAGE_REDIS_SESSION_PREFIX + clientId;

    when(valueOperations.get(key)).thenReturn(session);

    ClientSession result = redisStorage.getClientSession(clientId);

    assertEquals(session, result);
  }

  @Test
  public void testGetClientSession_NotFound() {
    UUID clientId = UUID.randomUUID();
    String key = SharedConstants.STORAGE_REDIS_SESSION_PREFIX + clientId;

    when(valueOperations.get(key)).thenReturn(null);

    ClientSession result = redisStorage.getClientSession(clientId);

    assertNull(result);
  }

  @Test
  public void testGetClientSession_InvalidType() {
    UUID clientId = UUID.randomUUID();
    String key = SharedConstants.STORAGE_REDIS_SESSION_PREFIX + clientId;

    when(valueOperations.get(key)).thenReturn("InvalidType");

    Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              redisStorage.getClientSession(clientId);
            });

    String expectedMessage =
        "Redis storage for client session " + clientId + " has invalid type java.lang.String";
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testDeleteClientSession() {
    UUID clientId = UUID.randomUUID();
    String key = SharedConstants.STORAGE_REDIS_SESSION_PREFIX + clientId;

    redisStorage.deleteClientSession(clientId);

    verify(redisTemplate).delete(key);
  }
}
