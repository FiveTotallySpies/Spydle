package dev.totallyspies.spydle.shared.model;

import com.google.gson.JsonElement;
import dev.totallyspies.spydle.shared.JsonValidator;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ClientSession implements Serializable {

    private UUID clientId;
    private GameServer gameServer;
    private String playerName;
    private State state;

    public static void validateJsonElement(JsonElement clientSession) {
        JsonValidator.validateJsonElement(clientSession, ClientSession.class);
    }

    public enum State {
        ASSIGNED, // Client has been assigned to a gameserver, but hasn't connected to it yet
        CONNECTED // Client has connected to this gameserver
    }

}