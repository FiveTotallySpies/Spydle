package dev.totallyspies.spydle.gameserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SessionService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public boolean isClientAssignedToGameServer(String clientId, String gameServerName) {
        String sessionKey = "session:" + clientId;
        Map<Object, Object> session = redisTemplate.opsForHash().entries(sessionKey);
        if (session.isEmpty()) return false;
        String assignedGameServer = (String) session.get("gameServerName");
        return gameServerName.equals(assignedGameServer);
    }

    public void removeClientSession(String clientId) {
        redisTemplate.delete("session:" + clientId);
    }

}