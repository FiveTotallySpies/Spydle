package dev.totallyspies.spydle.gameserver.session;

import java.util.UUID;
import javax.annotation.Nullable;

public interface ClientSessionValidator {

  boolean validateClientSession(UUID clientId, String name);

  @Nullable
  default UUID parseClientId(Object clientIdObject) {
    if (clientIdObject == null) return null;
    try {
      return UUID.fromString(clientIdObject.toString());
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }
}
