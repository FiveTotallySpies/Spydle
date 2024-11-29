package dev.totallyspies.spydle.shared.model;

import com.google.gson.JsonElement;
import dev.totallyspies.spydle.shared.JsonValidator;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ClientSession implements Serializable {

  private UUID clientId;
  private GameServer gameServer;
  private String playerName;
  private State state;

  public static void validateJsonElement(JsonElement clientSession) {
    JsonValidator.validateJsonElement(clientSession, ClientSession.class);
  }

  // TODO Investigate later why this is needed
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ClientSession session)) return false;
    if (session.getClientId() == null || !session.getClientId().equals(clientId)) return false;
    if (session.getGameServer() == null || !session.getGameServer().equals(gameServer))
      return false;
    if (session.getPlayerName() == null || !session.getPlayerName().equals(playerName))
      return false;
      return session.getState() != null && session.getState().equals(state);
  }

  public enum State {
    ASSIGNED, // Client has been assigned to a gameserver, but hasn't connected to it yet
    CONNECTED // Client has connected to this gameserver
  }
}
