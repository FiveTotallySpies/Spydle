package dev.totallyspies.spydle.gameserver.agones;

import agones.dev.sdk.Sdk;
import dev.totallyspies.spydle.shared.model.GameServer;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.ExecutionException;
import net.infumia.agones4j.Agones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class AgonesHook {

    private final Logger logger = LoggerFactory.getLogger(AgonesHook.class);

    @Bean
    public GameServer currentGameServer(
            @Value("${agones.enabled}") boolean enabled,
            @Value("${agones.host-port}") int agonesPort,
            @Value("${server.port}") int containerPort
    ) throws ExecutionException, InterruptedException {
        if (!enabled) {
            logger.info("Agones Hook has been disabled in application properties, skipping Agones hook...");
            return GameServer.builder()
                    .address("localhost")
                    .port(containerPort)
                    .name("gameserver-local")
                    .roomId("12345") // TODO
                    .publicRoom(false) // TODO
                    .state(GameServer.State.WAITING)
                    .build();
        }

        // Construct agones SDK wrapper on GRPC
        final ExecutorService gameServerWatcherExecutor =
                Executors.newSingleThreadExecutor();
        final ScheduledExecutorService healthCheckExecutor =
                Executors.newSingleThreadScheduledExecutor();
        Agones agones = Agones.builder()
                .withAddress("localhost", agonesPort)
                .withChannel(ManagedChannelBuilder
                        .forAddress("localhost", agonesPort)
                        .usePlaintext()
                        .build())
                .withGameServerWatcherExecutor(gameServerWatcherExecutor)
                .withHealthCheck(
                        Duration.ofSeconds(1L), // Delay
                        Duration.ofSeconds(2L) // Period
                )
                .withHealthCheckExecutor(healthCheckExecutor)
                .build();
        logger.info("Instantiated Agones hook on localhost:{}", agonesPort);
        if (agones.canHealthCheck()) {
            agones.startHealthChecking();
            logger.info("Began Agones health checking");
        } else {
            throw new IllegalStateException("Failed to begin Agones health checking");
        }
        if (agones.canWatchGameServer()) {
            agones.addGameServerWatcher(gameServer -> {
                logger.info("Received state updated from Agones: {}", gameServer.getStatus().getState());
            });
        } else {
            logger.warn("Failed to add game server watcher: Not allowed");
        }
        Sdk.GameServer sdkGameServer = agones.getGameServerFuture().get(); // Blocking

        // Store currentGameServer so we can cache in redis
        GameServer currentGameServer = GameServer.builder()
                .address(sdkGameServer.getStatus().getAddress())
                .port(sdkGameServer.getStatus().getPorts(0).getPort())
                .name(sdkGameServer.getObjectMeta().getName())
                .roomId("12345") // TODO
                .publicRoom(false) // TODO
                .state(GameServer.State.WAITING)
                .build();

        // Mark us as ready
        agones.ready();

        return currentGameServer;
    }

}
