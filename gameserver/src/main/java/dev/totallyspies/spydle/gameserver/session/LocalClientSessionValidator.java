package dev.totallyspies.spydle.gameserver.session;

import dev.totallyspies.spydle.gameserver.storage.GameServerStorage;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "agones", name = "enabled", havingValue = "false")
public class LocalClientSessionValidator implements ClientSessionValidator {

    private final Logger logger = LoggerFactory.getLogger(LocalClientSessionValidator.class);

    @Autowired
    private GameServerStorage storage;

    @Autowired
    private GameServer currentGameServer;

    public LocalClientSessionValidator() {
        logger.info("Agones disabled, loading local (automatic acceptance) client session validator");
    }

    @Override
    public boolean validateClientSession(UUID clientId) {
        storage.storeClientSession(new ClientSession(clientId, currentGameServer.getName()));
        return true;
    }

}
