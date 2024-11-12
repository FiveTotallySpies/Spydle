package dev.totallyspies.spydle.gameserver.storage;

import dev.totallyspies.spydle.gameserver.message.GameSocketHandler;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * This is a special shutdown hook that executes before all the beans are destroyed.
 * This is different from @PreDestroy because that one is while beans are being destroyed.
 * This one just clears values from storage.
 */
@Component
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "redis")
public class GameShutdownHook implements ApplicationListener<ApplicationReadyEvent> {

    private final Logger logger = LoggerFactory.getLogger(GameShutdownHook.class);

    @Autowired
    private GameServerStorage storage;

    @Autowired
    private GameServer currentGameServer;

    @Autowired
    private GameSocketHandler handler;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Executing custom shutdown hook, clearing redis data");
            storage.deleteGameServer(currentGameServer);
            for (UUID clientId : handler.getSessions()) {
                storage.deleteClientSession(clientId);
            }
        }));
    }

}