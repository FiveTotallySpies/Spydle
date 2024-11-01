package dev.totallyspies.spydle.gameserver.redis;

import dev.totallyspies.spydle.gameserver.agones.AgonesHook;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.UUID;

@Service
public class RedisRepositoryService {

    private final Logger logger = LoggerFactory.getLogger(RedisRepositoryService.class);

    private static final String SESSION_PREFIX = "session:";
    private static final String GAMESERVER_PREFIX = "gameserver:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AgonesHook agonesHook;

    @EventListener(ApplicationReadyEvent.class)
    public void onAgonesReady() {
        GameServer currentGameServer = agonesHook.getCurrentGameServer();
        saveGameServer(currentGameServer);
        logger.info("Wrote current game server to redis: {}", currentGameServer);
    }

    public boolean hasClientSession(UUID clientId) {
        Object rawSession = redisTemplate.opsForValue().get(SESSION_PREFIX + clientId);
        if (!(rawSession instanceof ClientSession session)) return false;
        String gameServerName = agonesHook.getCurrentGameServer().getName();
        return gameServerName.equals(session.getGameServerName());
    }

    public void removeClientSession(UUID clientId) {
        redisTemplate.delete(SESSION_PREFIX + clientId.toString());
    }

    public void saveGameServer(GameServer gameserver) {
        redisTemplate.opsForValue().set(GAMESERVER_PREFIX + gameserver.getName(), gameserver);
    }

    public GameServer getGameServer() {
        String gameServerName = agonesHook.getCurrentGameServer().getName();
        return (GameServer) redisTemplate.opsForValue().get(GAMESERVER_PREFIX + gameServerName);
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