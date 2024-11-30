package dev.totallyspies.spydle.frontend.use_cases.list_games;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ListGamesOutputData {

  private List<String> roomCodes;
}
