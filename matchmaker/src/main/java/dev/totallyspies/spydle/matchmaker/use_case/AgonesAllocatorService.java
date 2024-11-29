package dev.totallyspies.spydle.matchmaker.use_case;

import allocation.Allocation;
import allocation.AllocationServiceGrpc;
import dev.totallyspies.spydle.matchmaker.config.GameServerRepository;
import dev.totallyspies.spydle.shared.RoomCodeUtils;
import dev.totallyspies.spydle.shared.model.GameServer;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AgonesAllocatorService {

  private final Logger logger = LoggerFactory.getLogger(AgonesAllocatorService.class);

  private final AllocationServiceGrpc.AllocationServiceStub allocationService;
  private final Allocation.AllocationRequest request;
  private final GameServerRepository gameServerRepository;

  public AgonesAllocatorService(
      @Value("${agones.gameserver.fleet}") String gameFleet,
      @Value("${agones.gameserver.namespace}") String gameNamespace,
      GameServerRepository gameServerRepository,
      AllocationServiceGrpc.AllocationServiceStub allocationService) {
    this.gameServerRepository = gameServerRepository;
    this.allocationService = allocationService;

    // Create base allocation request
    this.request =
        Allocation.AllocationRequest.newBuilder()
            .setNamespace(gameNamespace)
            .addGameServerSelectors(
                Allocation.GameServerSelector.newBuilder()
                    .setGameServerState(Allocation.GameServerSelector.GameServerState.READY)
                    .putMatchLabels("agones.dev/fleet", gameFleet)
                    .build())
            .setScheduling(Allocation.AllocationRequest.SchedulingStrategy.Distributed)
            .build();
  }

  public GameServer awaitAllocation(int timeoutMillis) {
    CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<Allocation.AllocationResponse> firstResponse =
        new AtomicReference<>(null);
    allocationService.allocate(
        request,
        new StreamObserver<>() {
          @Override
          public void onNext(Allocation.AllocationResponse value) {
            firstResponse.compareAndSet(null, value);
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            latch.countDown();
            throw new RuntimeException(throwable);
          }

          @Override
          public void onCompleted() {}
        });
    try {
      boolean success = latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
      if (!success) throw new RuntimeException("Gameserver allocation timed out");
    } catch (InterruptedException exception) {
      throw new RuntimeException("Requesting gameserver allocation was interrupted");
    }

    Allocation.AllocationResponse response = firstResponse.get();
    if (response == null) {
      throw new RuntimeException("Failed to get gameserver allocation");
    }

    String gameServerName = response.getGameServerName();
    String roomCode = RoomCodeUtils.getFromName(gameServerName);

    if (!gameServerRepository.gameServerExists(roomCode)) {
      // TODO un-allocate/destroy instance?
      throw new RuntimeException("Game server does not exist in repository: " + roomCode);
    }
    GameServer gameServer = gameServerRepository.getGameServer(roomCode);

    // Validate that repository contains correct information on gameserver
    assert gameServer.getAddress().equals(response.getAddress());
    assert gameServer.getPort() == response.getPorts(0).getPort();

    logger.info("Found requested allocation: {}", gameServer);
    return gameServer;
  }
}
