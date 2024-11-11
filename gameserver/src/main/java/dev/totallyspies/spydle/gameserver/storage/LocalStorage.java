package dev.totallyspies.spydle.gameserver.storage;

import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * A local in-memory storage implementation for our gameserver to store data in.
 * This storage is exclusive to a singular gameserver instance.
 * This should only be used for local testing.
 */
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local")
public class LocalStorage implements GameServerStorage {

    private final Map<String, GameServer> gameServerStorage = new ConcurrentHashMap<>();
    private final Map<UUID, ClientSession> sessionStorage = new ConcurrentHashMap<>();

    public LocalStorage() {
        LoggerFactory.getLogger(LocalStorage.class).info("Found storage.type=local, loading LocalStorage");
    }

    @Override
    public void storeGameServer(GameServer gameServer) {
        gameServerStorage.put(gameServer.getName(), gameServer);
    }

    @Override
    public GameServer getGameServer(String name) {
        return gameServerStorage.get(name);
    }

    @Override
    public void deleteGameServer(String name) {
        gameServerStorage.remove(name);
    }

    @Override
    public void storeClientSession(ClientSession session) {
        sessionStorage.put(session.getClientId(), session);
    }

    @Override
    public ClientSession getClientSession(UUID clientId) {
        return sessionStorage.get(clientId);
    }

    @Override
    public void deleteClientSession(UUID clientId) {
        sessionStorage.remove(clientId);
    }
}
