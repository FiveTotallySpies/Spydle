package dev.totallyspies.spydle.gameserver.storage;

import dev.totallyspies.spydle.shared.model.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CurrentGameServerWriter {

    private final Logger logger = LoggerFactory.getLogger(CurrentGameServerWriter.class);

    @Autowired
    private GameServerStorage storage;

    @Autowired
    private GameServer currentGameServer;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        storage.storeGameServer(currentGameServer);
        logger.info("Wrote current game server to storage: {}", currentGameServer);
    }

}
