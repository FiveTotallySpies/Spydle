package dev.totallyspies.spydle.frontend.use_cases.join_game;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class JoinGameOutputDataSuccess implements JoinGameOutputData {

  private String gameHost;
  private int gamePort;
  private UUID clientId;
  private String playerName;
  private String roomCode;
}
