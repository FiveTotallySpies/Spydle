package dev.totallyspies.spydle.matchmaker.service;

import dev.totallyspies.spydle.matchmaker.redis.SessionRepository;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
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
    private AgonesAllocatorService allocator;

    public GameServer createGame(UUID clientId) {
        // Check if client already has a session
        if (sessionRepository.sessionExists(clientId)) throw new IllegalStateException("Client is already in a game.");

        GameServer allocated = allocator.awaitAllocation();

        // Save client session
        ClientSession session = new ClientSession(clientId, allocated);
        sessionRepository.saveSession(session);

        return allocated;
    }

    public void joinGame(UUID clientId, GameServer room) {
        // Check if client already has a session
        if (sessionRepository.sessionExists(clientId)) {
            throw new IllegalStateException("Client is already in a game.");
        }
        // Save client session
        ClientSession session = new ClientSession(clientId, room);
        sessionRepository.saveSession(session);
    }

}