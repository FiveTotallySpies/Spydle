package dev.totallyspies.spydle.gameserver.storage;

import dev.totallyspies.spydle.shared.SharedConstants;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Wrapper for an external redis storage database that is likely shared between this gameserver and other gameservers.
 */
@Service
@Primary
@ConditionalOnProperty(name = "storage.type", havingValue = "redis")
public class RedisStorage implements GameServerStorage {

    private static final  String GAME_SERVER_PREFIX = SharedConstants.STORAGE_REDIS_GAME_SERVER_PREFIX;
    private static final String SESSION_PREFIX = SharedConstants.STORAGE_REDIS_SESSION_PREFIX;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public RedisStorage() {
        LoggerFactory.getLogger(RedisStorage.class).info("Found storage.type=redis, loading RedisStorage");
    }

    @Override
    public void storeGameServer(GameServer gameServer) {
        redisTemplate.opsForValue().set(GAME_SERVER_PREFIX + gameServer.getRoomCode(), gameServer);
    }

    @Override
    public GameServer getGameServer(String roomCode) {
        Object value = redisTemplate.opsForValue().get(GAME_SERVER_PREFIX + roomCode);
        if (value == null) return null;
        if (!(value instanceof GameServer gameServer)) {
            throw new IllegalStateException("Redis storage for gameserver " + roomCode + " has invalid type " + value.getClass().getCanonicalName());
        }
        return gameServer;
    }

    @Override
    public void deleteGameServer(String roomCode) {
        redisTemplate.delete(GAME_SERVER_PREFIX + roomCode);
    }

    @Override
    public void storeClientSession(ClientSession session) {
        redisTemplate.opsForValue().get(SESSION_PREFIX + session.getClientId());
    }

    @Override
    public ClientSession getClientSession(UUID clientId) {
        Object value = redisTemplate.opsForValue().get(SESSION_PREFIX + clientId);
        if (value == null) return null;
        if (!(value instanceof ClientSession session)) {
            throw new IllegalStateException("Redis storage for client session " + clientId + " has invalid type " + value.getClass().getCanonicalName());
        }
        return session;
    }

    @Override
    public void deleteClientSession(UUID clientId) {
        redisTemplate.delete(SESSION_PREFIX + clientId);
    }

}