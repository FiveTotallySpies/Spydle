package dev.totallyspies.spydle.frontend.use_cases.join_game;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class JoinGameOutputDataFail implements JoinGameOutputData {

  private String message;
}
