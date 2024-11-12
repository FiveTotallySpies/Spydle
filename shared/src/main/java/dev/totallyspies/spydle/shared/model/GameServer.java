package dev.totallyspies.spydle.shared.model;

import com.google.gson.JsonElement;
import dev.totallyspies.spydle.shared.JsonValidator;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class GameServer implements Serializable {

    private String address;
    private int port;
    private String name;
    private String roomCode;
    private boolean publicRoom;
    private State state;

    public static void validateJsonElement(JsonElement gameServer) {
        JsonValidator.validateJsonElement(gameServer, GameServer.class);
    }

    public enum State {
        WAITING,
        PLAYING,
        FINISHING;
    }

}
