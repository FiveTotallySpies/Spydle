package dev.totallyspies.spydle.matchmaker.service;

import dev.totallyspies.spydle.matchmaker.redis.GameServerRepository;
import dev.totallyspies.spydle.matchmaker.redis.SessionRepository;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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

    @Autowired
    private GameServerRepository gameServerRepository;

    public GameServer createGame(UUID clientId, String playerName) {
        validateSession(clientId);

        // Await allocation for a new gameserver
        GameServer allocated = allocator.awaitAllocation();

        // Save client session
        ClientSession session = new ClientSession(clientId, allocated, playerName, ClientSession.State.ASSIGNED);
        sessionRepository.saveSession(session);

        return allocated;
    }

    public void joinGame(UUID clientId, String playerName, GameServer room) {
        validateSession(clientId);
        // Save client session
        sessionRepository.saveSession(new ClientSession(clientId, room, playerName, ClientSession.State.ASSIGNED));
    }

    public List<String> listGames() {
        return gameServerRepository.getGameServers()
                .stream()
                .filter(gameServer ->
                        gameServer.isPublicRoom()
                        && gameServer.getState() == GameServer.State.WAITING)
                .map(GameServer::getRoomCode)
                .toList();
    }

    private void validateSession(UUID clientId) {
        // Check if client already has a session
        if (sessionRepository.sessionExists(clientId)) {
            ClientSession session = sessionRepository.getSession(clientId);
            if (session.getState() == ClientSession.State.CONNECTED) {
                throw new IllegalStateException("Client is already connected to a game.");
            }
            // Otherwise, reset session since they are only ASSIGNED
            sessionRepository.deleteSession(clientId);
        }
    }

}