package dev.totallyspies.spydle.matchmaker.service;

import dev.totallyspies.spydle.matchmaker.redis.GameServerRepository;
import dev.totallyspies.spydle.matchmaker.redis.SessionRepository;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
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

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private GameServerRepository gameServerRepository;

    @Autowired
    private AgonesAllocatorService allocator;

    public GameServer createGame(UUID clientId) {
        // Check if client already has a session
        if (sessionRepository.sessionExists(clientId)) throw new IllegalStateException("Client is already in a game.");

        GameServer allocated = allocator.awaitAllocation();

        // Save client session
        ClientSession session = new ClientSession(clientId, allocated.getName());
        sessionRepository.saveSession(session);

        return allocated;
    }

    public GameServer joinGame(UUID clientId, String gameServerName) {
        // TODO should take in room ID not gameServerName

        // Check if client already has a session
        if (sessionRepository.sessionExists(clientId)) {
            // TODO return existing info
            throw new IllegalStateException("Client is already in a game.");
        }

        // Save client session
        ClientSession session = new ClientSession(clientId, gameServerName);
        sessionRepository.saveSession(session);

        GameServer model = gameServerRepository.getGameServer(gameServerName);
        assert model != null;
        return model;
    }

    public void leaveGame(UUID clientId) {
        // Delete client session
        sessionRepository.deleteSession(clientId);

        // TODO: Notify the GameServer (Optional)
        // TODO: Implement logic to notify the GameServer that the client is leaving
    }



}