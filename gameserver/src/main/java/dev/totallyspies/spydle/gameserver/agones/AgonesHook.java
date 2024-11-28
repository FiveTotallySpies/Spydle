package dev.totallyspies.spydle.gameserver.agones;

import agones.dev.sdk.Sdk;
import dev.totallyspies.spydle.shared.RoomCodeUtils;
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

    public AgonesHook(Agones agones) throws ExecutionException, InterruptedException {
        Sdk.GameServer sdkGameServer = agones.getGameServerFuture().get(); // Blocking

        String gameServerName = sdkGameServer.getObjectMeta().getName();
        String roomCode = RoomCodeUtils.getFromName(gameServerName);

        // Store currentGameServer so we can cache in redis
        currentGameServer = GameServer.builder()
                .address(sdkGameServer.getStatus().getAddress())
                .port(sdkGameServer.getStatus().getPorts(0).getPort())
                .name(gameServerName)
                .roomCode(roomCode)
                .publicRoom(true) // TODO make false by default
                .state(GameServer.State.READY)
                .build();

        // Mark us as ready
        agones.ready();
        logger.info("Marked us as ready in Agones");
    }

}
