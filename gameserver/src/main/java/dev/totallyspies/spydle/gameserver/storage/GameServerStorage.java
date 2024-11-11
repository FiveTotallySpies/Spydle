package dev.totallyspies.spydle.gameserver.storage;

import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;

import java.util.UUID;

/**
 * Represents some database or internal storage that we cache information to for this game server.
 * For local testing, this might be storage that is exclusive to this gameserver instance, but
 * it is typically shared among multiple gameservers.
 */
public interface GameServerStorage {

    void storeGameServer(GameServer gameServer);

    GameServer getGameServer(String name);

    void deleteGameServer(String name);

    void storeClientSession(ClientSession session);

    ClientSession getClientSession(UUID clientId);

    void deleteClientSession(UUID clientId);

}
