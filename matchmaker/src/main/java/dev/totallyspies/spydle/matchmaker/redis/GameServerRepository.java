package dev.totallyspies.spydle.matchmaker.redis;

import dev.totallyspies.spydle.shared.model.GameServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GameServerRepository {

    private static final String PREFIX = "gameserver:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public GameServer getGameServer(String gameServerName) {
        return (GameServer) redisTemplate.opsForValue().get(PREFIX + gameServerName);
    }

    public boolean gameServerExists(String gameServerName) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + gameServerName));
    }

}