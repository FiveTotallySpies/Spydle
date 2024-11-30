package dev.totallyspies.spydle.frontend.use_cases.create_game;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class CreateGameOutputDataSuccess implements CreateGameOutputData {

  private String gameHost;
  private int gamePort;
  private UUID clientId;
  private String playerName;
  private String roomCode;
}
