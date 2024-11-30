package dev.totallyspies.spydle.gameserver.session;

import dev.totallyspies.spydle.gameserver.storage.GameServerStorage;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnProperty(prefix = "agones", name = "enabled", havingValue = "true")
public class IntegratedClientSessionValidator implements ClientSessionValidator {

  private final Logger logger = LoggerFactory.getLogger(IntegratedClientSessionValidator.class);

  private final GameServerStorage storage;
  private final GameServer currentGameServer;

  public IntegratedClientSessionValidator(GameServerStorage storage, GameServer currentGameServer) {
    this.storage = storage;
    this.currentGameServer = currentGameServer;
    logger.info("Agones enabled, loaded k8s-integrated client session validator");
  }

  @Override
  public boolean validateClientSession(UUID clientId, String name) {
    Object rawSession = storage.getClientSession(clientId);
    if (!(rawSession instanceof ClientSession session)) return false;
    return currentGameServer.isSameGameServer(session.getGameServer());
  }
}
