package dev.totallyspies.spydle.gameserver.game;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Player {
  private UUID id;
  private String name;
  private int score;
}
