package dev.totallyspies.spydle.gameserver.storage;

import dev.totallyspies.spydle.shared.model.GameServer;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ShutdownHook implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private GameServerStorage storage;

    @Autowired
    private GameServer currentGameServer;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Code to run before bean destruction
            System.out.println("Graceful shutdown hook executed before bean destruction");
        }));
    }

    @PreDestroy
    public void onShutdown() {
        storage.deleteGameServer(currentGameServer.getName());
    }

}