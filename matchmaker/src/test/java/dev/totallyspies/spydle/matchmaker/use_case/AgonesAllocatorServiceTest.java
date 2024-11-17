package dev.totallyspies.spydle.matchmaker.use_case;

import allocation.Allocation;
import allocation.AllocationServiceGrpc;
import dev.totallyspies.spydle.matchmaker.config.GameServerRepository;
import dev.totallyspies.spydle.shared.model.GameServer;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AgonesAllocatorServiceTest {

    @Mock
    private GameServerRepository gameServerRepository;

    @Mock
    private AllocationServiceGrpc.AllocationServiceStub allocationServiceStub;

    private AgonesAllocatorService agonesAllocatorService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Instantiate AgonesAllocatorService with mock dependencies
        String gameFleet = "test-fleet";
        String gameNamespace = "test-game-namespace";

        agonesAllocatorService = new AgonesAllocatorService(
                gameFleet,
                gameNamespace,
                gameServerRepository,
                allocationServiceStub
        );
    }

    @Test
    public void testAwaitAllocation_Success() {
        Allocation.AllocationResponse response = Allocation.AllocationResponse.newBuilder()
                .setGameServerName("game-server-12345")
                .setAddress("127.0.0.1")
                .addPorts(Allocation.AllocationResponse.GameServerStatusPort.newBuilder()
                        .setPort(7777)
                        .build())
                .build();

        doAnswer(invocation -> {
            StreamObserver<Allocation.AllocationResponse> observer = invocation.getArgument(1);
            observer.onNext(response);
            observer.onCompleted();
            return null;
        }).when(allocationServiceStub).allocate(any(), any());

        GameServer gameServer = new GameServer("127.0.0.1", 7777, "", "", true, GameServer.State.WAITING);

        when(gameServerRepository.gameServerExists(anyString())).thenReturn(true);
        when(gameServerRepository.getGameServer(anyString())).thenReturn(gameServer);

        GameServer result = agonesAllocatorService.awaitAllocation(1);

        assertEquals(gameServer, result);
    }

    @Test
    public void testAwaitAllocation_GameServerNotExist() {
        Allocation.AllocationResponse response = Allocation.AllocationResponse.newBuilder()
                .setGameServerName("game-server-12345")
                .build();

        doAnswer(invocation -> {
            StreamObserver<Allocation.AllocationResponse> observer = invocation.getArgument(1);
            observer.onNext(response);
            observer.onCompleted();
            return null;
        }).when(allocationServiceStub).allocate(any(), any());

        when(gameServerRepository.gameServerExists(anyString())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> agonesAllocatorService.awaitAllocation(1));

        assertTrue(exception.getMessage().contains("Game server does not exist in repository"));
    }

    @Test
    public void testAwaitAllocation_Timeout() {
        doAnswer(invocation -> null).when(allocationServiceStub).allocate(any(), any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> agonesAllocatorService.awaitAllocation(1));

        assertTrue(exception.getMessage().contains("Gameserver allocation timed out"));
    }

}