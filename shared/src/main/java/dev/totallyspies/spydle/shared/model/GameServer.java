package dev.totallyspies.spydle.shared.model;

import com.google.gson.JsonElement;
import dev.totallyspies.spydle.shared.JsonValidator;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
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

  // Unlike .equals, does not check state/publicRoom!
  public boolean isSameGameServer(GameServer other) {
    if (other.address == null || !other.address.equals(address)) return false;
    if (other.port != port) return false;
    if (other.name == null || !other.name.equals(name)) return false;
      return other.roomCode != null && other.roomCode.equals(roomCode);
  }

  public enum State {
    READY, // No one has joined our gameserver yet, we are waiting for the first player
    WAITING, // Some players have joined, but the game hasn't started. New players can join.
    PLAYING // Players are playing the game, no one can join
  }
}
