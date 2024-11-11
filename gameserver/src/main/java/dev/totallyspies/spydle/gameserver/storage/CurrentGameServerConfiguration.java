package dev.totallyspies.spydle.gameserver.storage;

import dev.totallyspies.spydle.gameserver.agones.AgonesHook;
import dev.totallyspies.spydle.shared.model.GameServer;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
    private GameServerStorage storage;

    private GameServer currentGameServer;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "agones.enabled", havingValue = "true")
    public GameServer currentAgonesGameServer(AgonesHook agonesHook) {
<<<<<<< Updated upstream
        logger.info("Agones has been enabled in application properties, loading agones current game server info...");
        return agonesHook.getCurrentGameServer();
=======
        logger.info("Agones enabled, loading agones current game server info...");
        currentGameServer = agonesHook.getCurrentGameServer();
        writeCurrentGameServer();
        return currentGameServer;
>>>>>>> Stashed changes
    }

    @Bean
    @ConditionalOnProperty(name = "agones.enabled", havingValue = "false")
    public GameServer currentLocalGameServer(@Value("${server.port}") int containerPort) {
<<<<<<< Updated upstream
        logger.info("Agones has been disabled in application properties, loading local current game server info...");
        return GameServer.builder()
=======
        logger.info("Agones disabled, loading local current game server info...");
        currentGameServer = GameServer.builder()
>>>>>>> Stashed changes
                .address("localhost")
                .port(containerPort)
                .name("gameserver-local")
                .roomId("12345") // TODO
                .publicRoom(false) // TODO
                .state(GameServer.State.WAITING)
                .build();
        writeCurrentGameServer();
        return currentGameServer;
    }

    private void writeCurrentGameServer() {
        storage.storeGameServer(currentGameServer);
        logger.info("Wrote current game server to storage: {}", currentGameServer);
    }

    @PreDestroy
    public void onShutdown() {
        storage.deleteGameServer(currentGameServer.getName());
    }

}
