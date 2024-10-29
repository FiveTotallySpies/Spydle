package dev.totallyspies.spydle.matchmaker.service;

import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleRequestModel;
import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleResponseModel;
import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleResponseModelResponse;
import io.kubernetes.client.openapi.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Responsible for handling logic for all of our service endpoints
 */
@Service
public class MatchmakingService {

    private final Logger logger = LoggerFactory.getLogger(MatchmakingService.class);

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private AgonesAllocatorService allocator;

    public GameServerInfo createGame(String clientId) throws ApiException {
        // Check if client already has a session
        if (sessionRepository.sessionExists(clientId)) throw new IllegalStateException("Client is already in a game.");

        GameServerInfo allocated = allocator.awaitAllocation();

        // Save client session
        ClientSession session = new ClientSession(clientId, allocated.getGameServerName());
        sessionRepository.saveSession(session);

        return allocated;
    }

    public GameServerInfo joinGame(String clientId, String gameServerName) {
        // Check if client already has a session
        if (sessionRepository.sessionExists(clientId)) {
            // TODO return existing info
            throw new IllegalStateException("Client is already in a game.");
        }

        // TODO: Validate that the gameServerName is valid and has capacity

        // Save client session
        ClientSession session = new ClientSession(clientId, gameServerName);
        sessionRepository.saveSession(session);

        // TODO: Get NodePort address for gameservers and grab port from redis

        // Retrieve GameServer info (This assumes you have a way to get the address and port)
        // For demonstration purposes, we'll mock this data
        return GameServerInfo.builder()
                .address("game-server-address")
                .port(12345)
                .gameServerName(gameServerName)
                .build();
    }

    public void leaveGame(String clientId) {
        // Delete client session
        sessionRepository.deleteSession(clientId);

        // TODO: Notify the GameServer (Optional)
        // TODO: Implement logic to notify the GameServer that the client is leaving
    }

    public AutoscaleResponseModel autoscale(AutoscaleRequestModel request) {
        int allocatedReplicas = request.getRequest().getStatus().getAllocatedReplicas();

        // TODO Load custom scaling logic from config
        int desiredIdleReplicas = Math.max(4, (int) (allocatedReplicas * 0.5));
        int desiredReplicas = allocatedReplicas + desiredIdleReplicas;
        logger.debug("Calculated desired replicas for autoscale target: {}", desiredReplicas);

        AutoscaleResponseModelResponse response = new AutoscaleResponseModelResponse()
                .replicas(desiredReplicas)
                .uid(request.getRequest().getUid())
                .scale(true);
        return new AutoscaleResponseModel().response(response);
    }

}