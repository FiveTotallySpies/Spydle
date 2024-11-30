package dev.totallyspies.spydle.frontend.use_cases.create_game;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class CreateGameOutputDataFail implements CreateGameOutputData {

  private String message;
}
