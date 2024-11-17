package dev.totallyspies.spydle.matchmaker.redis;

import dev.totallyspies.spydle.shared.SharedConstants;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GameServerRepositoryTest {

    private GameServerRepository gameServerRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private Set<String> keys;

    private final GameServer fakeGameServer = new GameServer("", 0, "", "", false, GameServer.State.WAITING);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        gameServerRepository = new GameServerRepository(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testGetGameServer() {
        String roomCode = "ABC123";
        GameServer gameServer = fakeGameServer.toBuilder().roomCode(roomCode).build();
        String key = SharedConstants.STORAGE_REDIS_GAME_SERVER_PREFIX + roomCode;

        when(valueOperations.get(key)).thenReturn(gameServer);

        GameServer result = gameServerRepository.getGameServer(roomCode);

        verify(valueOperations).get(key);
        assertEquals(gameServer, result);
    }

    @Test
    public void testGameServerExists_True() {
        String roomCode = "ABC123";
        String key = SharedConstants.STORAGE_REDIS_GAME_SERVER_PREFIX + roomCode;

        when(redisTemplate.hasKey(key)).thenReturn(true);

        boolean exists = gameServerRepository.gameServerExists(roomCode);

        verify(redisTemplate).hasKey(key);
        assertTrue(exists);
    }

    @Test
    public void testGameServerExists_False() {
        String roomCode = "ABCDE";
        String key = SharedConstants.STORAGE_REDIS_GAME_SERVER_PREFIX + roomCode;

        when(redisTemplate.hasKey(key)).thenReturn(false);

        boolean exists = gameServerRepository.gameServerExists(roomCode);

        verify(redisTemplate).hasKey(key);
        assertFalse(exists);
    }

    @Test
    public void testGetGameServers_NoKeys() {
        when(redisTemplate.keys(anyString())).thenReturn(null);

        List<GameServer> result = gameServerRepository.getGameServers();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetGameServers_WithKeys() {
        Set<String> keySet = Set.of("key1", "key2");
        GameServer gameServer2 = fakeGameServer.toBuilder().roomCode("ABCDE").build();

        when(redisTemplate.keys(anyString())).thenReturn(keySet);
        when(valueOperations.get("key1")).thenReturn(fakeGameServer);
        when(valueOperations.get("key2")).thenReturn(gameServer2);

        List<GameServer> result = gameServerRepository.getGameServers();

        assertEquals(2, result.size());
        assertTrue(result.contains(fakeGameServer));
        assertTrue(result.contains(gameServer2));
    }

}
