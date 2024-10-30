package dev.totallyspies.spydle.gameserver.redis;

import dev.totallyspies.spydle.gameserver.generated.model.GameServerModel;
import dev.totallyspies.spydle.gameserver.service.AgonesHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

@Service
public class RedisRepositoryService {

    private static final String SESSION_PREFIX = "session:";
    private static final String GAMESERVER_PREFIX = "gameserver:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AgonesHook agonesHook;

    public boolean hasClientSession(UUID clientId) {
        Object rawSession = redisTemplate.opsForValue().get(SESSION_PREFIX + clientId);
        if (!(rawSession instanceof ClientSession session)) return false;
        String gameServerName = agonesHook.getCurrentGameServer().getGameServerName();
        return gameServerName.equals(session.getGameServerName());
    }

    public void removeClientSession(UUID clientId) {
        redisTemplate.delete(SESSION_PREFIX + clientId.toString());
    }

    public void saveGameServer(GameServerModel gameserver) {
        redisTemplate.opsForValue().set(GAMESERVER_PREFIX + gameserver.getGameServerName(), gameserver);
    }

    public GameServerModel getGameServer() {
        String gameServerName = agonesHook.getCurrentGameServer().getGameServerName();
        return (GameServerModel) redisTemplate.opsForValue().get(GAMESERVER_PREFIX + gameServerName);
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