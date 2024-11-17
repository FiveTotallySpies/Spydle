package dev.totallyspies.spydle.matchmaker.config;

import dev.totallyspies.spydle.shared.SharedConstants;
import dev.totallyspies.spydle.shared.model.ClientSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.UUID;

@Repository
public class SessionRepository {

    private static final String PREFIX = SharedConstants.STORAGE_REDIS_SESSION_PREFIX;

    private final RedisTemplate<String, Object> redisTemplate;

    public SessionRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveSession(ClientSession session) {
        redisTemplate.opsForValue().set(PREFIX + session.getClientId(), session);
    }

    public ClientSession getSession(UUID clientId) {
        return (ClientSession) redisTemplate.opsForValue().get(PREFIX + clientId);
    }

    public void deleteSession(UUID clientId) {
        redisTemplate.delete(PREFIX + clientId);
    }

    public boolean sessionExists(UUID clientId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + clientId));
    }

    @Nullable
    public UUID parseClientId(Object clientIdObject) {
        if (clientIdObject == null) return null;
        try {
            return UUID.fromString(clientIdObject.toString());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

}