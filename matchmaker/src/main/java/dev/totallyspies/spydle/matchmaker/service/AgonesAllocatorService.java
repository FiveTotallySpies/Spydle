package dev.totallyspies.spydle.matchmaker.service;

import allocation.Allocation;
import allocation.AllocationServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
@DependsOn("k8sClient")
public class AgonesAllocatorService {

    private final Logger logger = LoggerFactory.getLogger(AgonesAllocatorService.class);

    private AllocationServiceGrpc.AllocationServiceStub allocationService;

    private Allocation.AllocationRequest request;

    public AgonesAllocatorService(
            @Value("${agones.allocator.port}") int port,
            @Value("${agones.allocator.namespace}") String namespace,
            @Value("${agones.allocator.service-name}") String serviceName,
            @Value("${agones.gameserver.fleet}") String gameFleet,
            @Value("${agones.gameserver.namespace}") String gameNamespace
    ) throws ApiException {
        // Load clusterIP for agones allocator service
        CoreV1Api api = new CoreV1Api();
        V1Service service = api.readNamespacedService(serviceName, namespace).execute();
        String allocatorClusterIP = service.getSpec().getClusterIP();
        logger.info("For agones allocator service {} in namespace {}, found target {}:{} for game fleet {} in namespace {}",
                serviceName,
                namespace,
                allocatorClusterIP,
                port,
                gameFleet,
                gameNamespace);

        // Create GRPC stub
        allocationService = AllocationServiceGrpc.newStub(ManagedChannelBuilder
                .forAddress(allocatorClusterIP, port)
                .usePlaintext()
                .enableRetry()
                .maxInboundMessageSize(Integer.MAX_VALUE)
                .keepAliveTime(10, TimeUnit.SECONDS)
                .build());
        logger.info("Created allocation service GRPC stub");

        // Create base allocation request
        request = Allocation.AllocationRequest.newBuilder()
                .setNamespace(gameNamespace)
                .addGameServerSelectors(
                        Allocation.GameServerSelector.newBuilder()
                                .setGameServerState(Allocation.GameServerSelector.GameServerState.READY)
                                .putMatchLabels("agones.dev/fleet", gameFleet)
                                .build()
                )
                .setScheduling(Allocation.AllocationRequest.SchedulingStrategy.Distributed)
                .build();
    }

    public GameServerInfo awaitAllocation() {
        CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Allocation.AllocationResponse> firstResponse = new AtomicReference<>(null);
        allocationService.allocate(request, new StreamObserver<>() {
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
            public void onCompleted() {

            }
        });
        try {
            boolean success = latch.await(5, TimeUnit.SECONDS);
            if (!success) {
                throw new RuntimeException("Gameserver allocation timed out");
            }
            Allocation.AllocationResponse response = firstResponse.get();
            if (response == null) {
                throw new RuntimeException("Failed to get gameserver allocation");
            }
            GameServerInfo gameServer = GameServerInfo.builder()
                    .address(response.getAddress())
                    .gameServerName(response.getGameServerName())
                    .port(response.getPorts(0).getPort())
                    .build();
            logger.info("Found requested allocation: {}", gameServer);
            return gameServer;
        } catch (InterruptedException exception) {
            throw new RuntimeException("Requesting gameserver allocation was interrupted");
        }
    }

}
