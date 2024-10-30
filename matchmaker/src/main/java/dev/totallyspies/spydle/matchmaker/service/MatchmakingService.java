package dev.totallyspies.spydle.matchmaker.service;

import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleRequestModel;
import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleResponseModel;
import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleResponseModelResponse;
import dev.totallyspies.spydle.matchmaker.generated.model.GameServerModel;
import dev.totallyspies.spydle.matchmaker.generated.model.GameServerStateModel;
import dev.totallyspies.spydle.matchmaker.redis.ClientSession;
import dev.totallyspies.spydle.matchmaker.redis.GameServerRepository;
import dev.totallyspies.spydle.matchmaker.redis.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Responsible for handling logic for all of our service endpoints
 */
@Service
public class MatchmakingService {

    private final Logger logger = LoggerFactory.getLogger(MatchmakingService.class);

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private GameServerRepository gameServerRepository;

    @Autowired
    private AgonesAllocatorService allocator;

    public GameServerModel createGame(UUID clientId) {
        // Check if client already has a session
        if (sessionRepository.sessionExists(clientId)) throw new IllegalStateException("Client is already in a game.");

        GameServerModel allocated = allocator.awaitAllocation();

        // Save client session
        ClientSession session = new ClientSession(clientId, allocated.getGameServerName());
        sessionRepository.saveSession(session);

        return allocated;
    }

    public GameServerModel joinGame(UUID clientId, String gameServerName) {
        // TODO should take in room ID not gameServerName

        // Check if client already has a session
        if (sessionRepository.sessionExists(clientId)) {
            // TODO return existing info
            throw new IllegalStateException("Client is already in a game.");
        }

        // Save client session
        ClientSession session = new ClientSession(clientId, gameServerName);
        sessionRepository.saveSession(session);

        GameServerModel model = gameServerRepository.getGameServer(gameServerName);
        assert model != null;
        return model;
    }

    public void leaveGame(UUID clientId) {
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