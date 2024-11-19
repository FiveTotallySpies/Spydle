package dev.totallyspies.spydle.gameserver.message.session;

import dev.totallyspies.spydle.gameserver.storage.GameServerStorage;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Primary
@ConditionalOnProperty(prefix = "agones", name = "enabled", havingValue = "true")
public class IntegratedClientSessionValidator implements ClientSessionValidator {

    private final Logger logger = LoggerFactory.getLogger(IntegratedClientSessionValidator.class);

    @Autowired
    private GameServerStorage storage;

    @Autowired
    private GameServer currentGameServer;

    public IntegratedClientSessionValidator() {
        logger.info("Agones enabled, loaded k8s-integrated client session validator");
    }

    @Override
    public boolean validateClientSession(UUID clientId, String name) {
        Object rawSession = storage.getClientSession(clientId);
        if (!(rawSession instanceof ClientSession session)) return false;
        return currentGameServer.equals(session.getGameServer());
    }

}
