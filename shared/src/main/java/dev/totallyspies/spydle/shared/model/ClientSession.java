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

    public static void validateJsonElement(JsonElement clientSession) {
        JsonValidator.validateJsonElement(clientSession, ClientSession.class);
    }

}