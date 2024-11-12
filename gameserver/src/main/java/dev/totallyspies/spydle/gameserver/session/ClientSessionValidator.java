package dev.totallyspies.spydle.gameserver.session;

import javax.annotation.Nullable;
import java.util.UUID;

public interface ClientSessionValidator {

    boolean validateClientSession(UUID clientId);

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