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
        // Check if client already has a session
        if (sessionRepository.sessionExists(clientId)) throw new IllegalStateException("Client is already in a game.");

        GameServer allocated = allocator.awaitAllocation(5000);

        // Save client session
        ClientSession session = new ClientSession(clientId, allocated, playerName);
        sessionRepository.saveSession(session);

        return allocated;
    }

    public void joinGame(UUID clientId, String playerName, GameServer room) {
        // Check if client already has a session
        if (sessionRepository.sessionExists(clientId)) {
            throw new IllegalStateException("Client is already in a game.");
        }
        // Save client session
        ClientSession session = new ClientSession(clientId, room, playerName);
        sessionRepository.saveSession(session);
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

}