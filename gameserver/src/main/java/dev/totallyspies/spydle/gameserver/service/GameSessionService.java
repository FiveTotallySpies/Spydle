package dev.totallyspies.spydle.gameserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

@Service
public class GameSessionService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AgonesHook agonesHook;

    public boolean hasClientSession(UUID clientId) {
        // TODO serialize similar to matchmaker
        String sessionKey = "session:" + clientId.toString();
        Map<Object, Object> session = redisTemplate.opsForHash().entries(sessionKey);
        if (session.isEmpty()) return false;
        String assignedGameServer = (String) session.get("gameServerName");
        return agonesHook.getGameServerName().equals(assignedGameServer);
    }

    public void removeClientSession(UUID clientId) {
        redisTemplate.delete("session:" + clientId.toString());
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