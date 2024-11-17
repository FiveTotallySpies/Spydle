package dev.totallyspies.spydle.matchmaker.config;

import dev.totallyspies.spydle.shared.SharedConstants;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Repository
public class GameServerRepository {

    private static final String PREFIX = SharedConstants.STORAGE_REDIS_GAME_SERVER_PREFIX;

    private RedisTemplate<String, Object> redisTemplate;

    public GameServerRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public GameServer getGameServer(String roomCode) {
        return (GameServer) redisTemplate.opsForValue().get(PREFIX + roomCode);
    }

    public boolean gameServerExists(String roomCode) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + roomCode));
    }

    public List<GameServer> getGameServers() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        if (keys == null) {
            return new LinkedList<>();
        }
        return keys.stream().map(key -> (GameServer) redisTemplate.opsForValue().get(key)).toList();
    }

}