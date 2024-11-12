package dev.totallyspies.spydle.gameserver.agones;

import agones.dev.sdk.Sdk;
import dev.totallyspies.spydle.shared.model.GameServer;
import io.grpc.ManagedChannelBuilder;

import lombok.Getter;
import net.infumia.agones4j.Agones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
@ConditionalOnProperty(name = "agones.enabled", havingValue = "true")
public class AgonesHook {

    private final Logger logger = LoggerFactory.getLogger(AgonesHook.class);

    @Getter
    private final GameServer currentGameServer;

    private final Agones agones;

    public AgonesHook(@Value("${agones.host-port}") int agonesPort) throws ExecutionException, InterruptedException {
        // Construct agones SDK wrapper on GRPC
        final ExecutorService gameServerWatcherExecutor =
                Executors.newSingleThreadExecutor();
        final ScheduledExecutorService healthCheckExecutor =
                Executors.newSingleThreadScheduledExecutor();
        agones = Agones.builder()
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

        String gameServerName = sdkGameServer.getObjectMeta().getName();
        String roomCode = gameServerName.substring(gameServerName.length() - 5).toUpperCase();

        // Store currentGameServer so we can cache in redis
        currentGameServer = GameServer.builder()
                .address(sdkGameServer.getStatus().getAddress())
                .port(sdkGameServer.getStatus().getPorts(0).getPort())
                .name(gameServerName)
                .roomCode(roomCode)
                .publicRoom(true) // TODO make false by default
                .state(GameServer.State.WAITING)
                .build();

        // Mark us as ready
        agones.ready();
    }

}
