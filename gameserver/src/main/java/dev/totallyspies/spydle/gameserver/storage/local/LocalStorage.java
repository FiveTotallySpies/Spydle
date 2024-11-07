package dev.totallyspies.spydle.gameserver.storage.local;

import dev.totallyspies.spydle.gameserver.storage.GameServerStorage;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A local in-memory storage implementation for our gameserver to store data in.
 * This storage is exclusive to a singular gameserver instance.
 * This should only be used for local testing.
 */
public class LocalStorage implements GameServerStorage {

    private final Map<String, GameServer> gameServerStorage = new ConcurrentHashMap<>();
    private final Map<UUID, ClientSession> sessionStorage = new ConcurrentHashMap<>();

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
