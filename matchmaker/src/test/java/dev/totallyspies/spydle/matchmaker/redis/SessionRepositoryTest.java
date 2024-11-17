package dev.totallyspies.spydle.matchmaker.redis;

import dev.totallyspies.spydle.shared.SharedConstants;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SessionRepositoryTest {

    private SessionRepository sessionRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private final GameServer fakeGameServer = new GameServer("", 0, "", "", false, GameServer.State.WAITING);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        sessionRepository = new SessionRepository(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testSaveSession() {
        ClientSession session = new ClientSession(UUID.randomUUID(), fakeGameServer, "");
        session.setClientId(UUID.randomUUID());
        String key = SharedConstants.STORAGE_REDIS_SESSION_PREFIX + session.getClientId();

        sessionRepository.saveSession(session);

        verify(valueOperations).set(key, session);
    }

    @Test
    public void testGetSession() {
        UUID clientId = UUID.randomUUID();
        ClientSession session = new ClientSession(clientId, fakeGameServer, "");
        String key = SharedConstants.STORAGE_REDIS_SESSION_PREFIX + clientId;

        when(valueOperations.get(key)).thenReturn(session);

        ClientSession result = sessionRepository.getSession(clientId);

        verify(valueOperations).get(key);
        assertEquals(session, result);
    }

    @Test
    public void testDeleteSession() {
        UUID clientId = UUID.randomUUID();
        String key = SharedConstants.STORAGE_REDIS_SESSION_PREFIX + clientId;

        sessionRepository.deleteSession(clientId);

        verify(redisTemplate).delete(key);
    }

    @Test
    public void testSessionExists_True() {
        UUID clientId = UUID.randomUUID();
        String key = SharedConstants.STORAGE_REDIS_SESSION_PREFIX + clientId;

        when(redisTemplate.hasKey(key)).thenReturn(true);

        boolean exists = sessionRepository.sessionExists(clientId);

        verify(redisTemplate).hasKey(key);
        assertTrue(exists);
    }

    @Test
    public void testSessionExists_False() {
        UUID clientId = UUID.randomUUID();
        String key = SharedConstants.STORAGE_REDIS_SESSION_PREFIX + clientId;

        when(redisTemplate.hasKey(key)).thenReturn(false);

        boolean exists = sessionRepository.sessionExists(clientId);

        verify(redisTemplate).hasKey(key);
        assertFalse(exists);
    }

    @Test
    public void testParseClientId_Valid() {
        UUID clientId = UUID.randomUUID();

        UUID result = sessionRepository.parseClientId(clientId.toString());

        assertEquals(clientId, result);
    }

    @Test
    public void testParseClientId_Invalid() {
        UUID result = sessionRepository.parseClientId("invalid-uuid");

        assertNull(result);
    }

    @Test
    public void testParseClientId_Null() {
        UUID result = sessionRepository.parseClientId(null);

        assertNull(result);
    }

}