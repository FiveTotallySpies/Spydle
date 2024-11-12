package dev.totallyspies.spydle.gameserver.storage;

import dev.totallyspies.spydle.gameserver.agones.AgonesHook;
import dev.totallyspies.spydle.shared.RoomCodeUtils;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration to decide which bean should declare how we get information about our current gameserver.
 * If agones is enabled, we use the Agones Hook to retrieve this information from Agones Controller Service.
 * If agones is disabled, we use fake local details.
 */
@Configuration
public class CurrentGameServerConfiguration {

    private final Logger logger = LoggerFactory.getLogger(CurrentGameServerConfiguration.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private GameServerStorage storage;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "agones.enabled", havingValue = "true")
    public GameServer currentAgonesGameServer(AgonesHook agonesHook) {
        logger.info("Agones enabled, loading agones current game server info...");
        return writeCurrentGameServer(agonesHook.getCurrentGameServer());
    }

    @Bean
    @ConditionalOnProperty(name = "agones.enabled", havingValue = "false")
    public GameServer currentLocalGameServer(@Value("${server.port}") int containerPort) {
        logger.info("Agones disabled, loading local current game server info...");
        String roomCode = RoomCodeUtils.generateRandomCode();
        String gameServerName = "gameserver-local-" + roomCode;
        return writeCurrentGameServer(GameServer.builder()
                .address("localhost")
                .port(containerPort)
                .name(gameServerName)
                .roomCode(roomCode)
                .publicRoom(false)
                .state(GameServer.State.READY)
                .build());
    }

    public void updateInStorage() {
        GameServer currentGameServer = context.getBean(GameServer.class);
        writeCurrentGameServer(currentGameServer);
    }

    private GameServer writeCurrentGameServer(GameServer currentGameServer) {
        storage.storeGameServer(currentGameServer);
        logger.info("Wrote current game server to storage: {}", currentGameServer);
        return currentGameServer;
    }

}
