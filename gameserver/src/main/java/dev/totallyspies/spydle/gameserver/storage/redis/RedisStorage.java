package dev.totallyspies.spydle.gameserver.storage.redis;

import dev.totallyspies.spydle.gameserver.storage.GameServerStorage;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;

/**
 * Wrapper for an external redis storage database that is likely shared between this gameserver and other gameservers.
 */
public class RedisStorage implements GameServerStorage {

    private static final  String GAME_SERVER_PREFIX = "gameserver:";
    private static final String SESSION_PREFIX = "session:";

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisStorage(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void storeGameServer(GameServer gameServer) {
        redisTemplate.opsForValue().set(GAME_SERVER_PREFIX + gameServer.getName(), gameServer);
    }

    @Override
    public GameServer getGameServer(String name) {
        Object value = redisTemplate.opsForValue().get(GAME_SERVER_PREFIX + name);
        if (value == null) return null;
        if (!(value instanceof GameServer gameServer)) {
            throw new IllegalStateException("Redis storage for gameserver " + name + " has invalid type " + value.getClass().getCanonicalName());
        }
        return gameServer;
    }

    @Override
    public void deleteGameServer(String name) {
        redisTemplate.delete(GAME_SERVER_PREFIX + name);
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