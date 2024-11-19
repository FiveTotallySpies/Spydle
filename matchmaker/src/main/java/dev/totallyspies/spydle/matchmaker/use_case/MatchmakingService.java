package dev.totallyspies.spydle.matchmaker.use_case;

import dev.totallyspies.spydle.matchmaker.config.GameServerRepository;
import dev.totallyspies.spydle.matchmaker.config.SessionRepository;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Responsible for handling logic for all of our service endpoints
 */
@Service
public class MatchmakingService {

    private final SessionRepository sessionRepository;
    private final AgonesAllocatorService allocator;
    private final GameServerRepository gameServerRepository;

    public MatchmakingService(SessionRepository sessionRepository,
                              AgonesAllocatorService allocator,
                              GameServerRepository gameServerRepository) {
        this.sessionRepository = sessionRepository;
        this.allocator = allocator;
        this.gameServerRepository = gameServerRepository;
    }

    public GameServer createGame(UUID clientId, String playerName) {
        validateSession(clientId);

        // Await allocation for a new gameserver
        GameServer allocated = allocator.awaitAllocation(5000);

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