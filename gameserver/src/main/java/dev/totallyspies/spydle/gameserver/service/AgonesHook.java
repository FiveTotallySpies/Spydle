package dev.totallyspies.spydle.gameserver.service;

import io.grpc.ManagedChannelBuilder;
import net.infumia.agones4j.Agones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class AgonesHook {

    private Agones agones;

    @Bean
    public Agones agones() {
        // TODO I have no clue any of this works
        // TODO add game lifecycle classes
        final ExecutorService gameServerWatcherExecutor =
                Executors.newSingleThreadExecutor();
        final ScheduledExecutorService healthCheckExecutor =
                Executors.newSingleThreadScheduledExecutor();
        agones = Agones.builder()
                .withAddress("localhost", 9357)
                .withChannel(ManagedChannelBuilder
                        .forAddress("localhost", 9357)
                        .usePlaintext()
                        .build())
                .withGameServerWatcherExecutor(gameServerWatcherExecutor)
                .withHealthCheck(
                        Duration.ofSeconds(1L), // Delay
                        Duration.ofSeconds(2L) // Period
                )
                .withHealthCheckExecutor(healthCheckExecutor)
                .build();
        if (agones.canHealthCheck()) {
            agones.startHealthChecking();
        } else {
            throw new IllegalStateException("Failed to begin agones health checking");
        }
        agones.stopHealthChecking();
        if (agones.canWatchGameServer()) {
            agones.addGameServerWatcher(gameServer ->
                    // TODO Use a real logger
                    // This will be called when the game server is updated.
                    System.out.println("Game server updated: " + gameServer));
        }
        agones.allocate();
        return agones;
    }

    @PreDestroy
    public void onExit() {
        // TODO this should be somewhere else
        agones.shutdown();
    }

}