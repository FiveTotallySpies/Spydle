package dev.totallyspies.spydle.gameserver.storage;

import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.UUID;

@Service
public class StorageService {

    private final Logger logger = LoggerFactory.getLogger(StorageService.class);

    @Autowired
    private GameServerStorage storage;

    @Autowired
    private GameServer currentGameServer;

    public boolean hasClientSession(UUID clientId) {
        Object rawSession = storage.getClientSession(clientId);
        if (!(rawSession instanceof ClientSession session)) return false;
        String gameServerName = currentGameServer.getName();
        return gameServerName.equals(session.getGameServerName());
    }

    @Nullable
    public UUID parseClientId(Object clientIdObject) {
        if (clientIdObject == null) return null;
        try {
            return UUID.fromString(clientIdObject.toString());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

}