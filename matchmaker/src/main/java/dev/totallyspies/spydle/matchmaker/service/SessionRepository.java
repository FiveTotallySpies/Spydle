package dev.totallyspies.spydle.matchmaker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SessionRepository {

    private static final String PREFIX = "session:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void saveSession(ClientSession session) {
        redisTemplate.opsForValue().set(PREFIX + session.getClientId(), session);
    }

    public ClientSession getSession(String clientId) {
        return (ClientSession) redisTemplate.opsForValue().get(PREFIX + clientId);
    }

    public void deleteSession(String clientId) {
        redisTemplate.delete(PREFIX + clientId);
    }

    public boolean sessionExists(String clientId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + clientId));
    }
}