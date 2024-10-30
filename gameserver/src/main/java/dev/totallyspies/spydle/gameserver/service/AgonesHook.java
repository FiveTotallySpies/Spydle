package dev.totallyspies.spydle.gameserver.service;

import agones.dev.sdk.Sdk;
import dev.totallyspies.spydle.gameserver.generated.model.GameServerModel;
import dev.totallyspies.spydle.gameserver.generated.model.GameServerStateModel;
import dev.totallyspies.spydle.gameserver.redis.RedisRepositoryService;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import net.infumia.agones4j.Agones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class AgonesHook {

    private final Logger logger = LoggerFactory.getLogger(AgonesHook.class);

    private Agones agones;

    @Value("${agones.host}")
    private String agonesHost;
    @Value("${agones.port}")
    private int agonesPort;

    @Getter
    private String gameServerName;

    @Autowired
    private RedisRepositoryService redisRepositoryService;

    @Bean
    public Agones agones() throws ExecutionException, InterruptedException {
        final ExecutorService gameServerWatcherExecutor =
                Executors.newSingleThreadExecutor();
        final ScheduledExecutorService healthCheckExecutor =
                Executors.newSingleThreadScheduledExecutor();
        agones = Agones.builder()
                .withAddress(agonesHost, agonesPort)
                .withChannel(ManagedChannelBuilder
                        .forAddress(agonesHost, agonesPort)
                        .usePlaintext()
                        .build())
                .withGameServerWatcherExecutor(gameServerWatcherExecutor)
                .withHealthCheck(
                        Duration.ofSeconds(1L), // Delay
                        Duration.ofSeconds(2L) // Period
                )
                .withHealthCheckExecutor(healthCheckExecutor)
                .build();
        logger.info("Instantiated Agones connection on {}:{}", agonesHost, agonesPort);
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

        // Cache our info in redis
        GameServerModel gameServerModel = new GameServerModel()
                .address(sdkGameServer.getStatus().getAddress())
                .port(sdkGameServer.getStatus().getPorts(0).getPort())
                .gameServerName(sdkGameServer.getObjectMeta().getName())
                .publicRoom(false) // TODO
                .state(GameServerStateModel.WAITING);
        redisRepositoryService.saveGameServer(gameServerModel);

        gameServerName = agones.getGameServerFuture().get().getObjectMeta().getName();
        agones.ready();
        return agones;
    }

}
