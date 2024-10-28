package dev.totallyspies.spydle.matchmaker.service;

import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleRequestModel;
import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleResponseModel;
import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleResponseModelResponse;
import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleResponseModelResponseScale;
import io.kubernetes.client.openapi.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

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

        AutoscaleResponseModelResponseScale scale = new AutoscaleResponseModelResponseScale().replicas(desiredReplicas);
        AutoscaleResponseModelResponse response = new AutoscaleResponseModelResponse().scale(scale);
        return new AutoscaleResponseModel().response(response);
    }

    private static String prettyPrint(Map<String, Object> yaml) {
        StringBuilder builder = new StringBuilder("{");
        for (String key : yaml.keySet()) {
            Object val = yaml.get(key);
            builder.append(key).append(":");
            if (val instanceof Map) {
                builder.append(prettyPrint((Map<String, Object>) val));
            } else {
                builder.append(val);
            }
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.append("}").toString();
    }
}