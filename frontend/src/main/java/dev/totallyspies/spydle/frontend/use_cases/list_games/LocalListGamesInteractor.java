package dev.totallyspies.spydle.frontend.use_cases.list_games;

import dev.totallyspies.spydle.shared.SharedConstants;
import java.util.LinkedList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalListGamesInteractor implements ListGamesInputBoundary {

  private final List<String> games = new LinkedList<>();

  public LocalListGamesInteractor() {
    games.add(SharedConstants.LOCAL_SERVER_ROOM_CODE);
  }

  @Override
  public ListGamesOutputData execute() {
    return new ListGamesOutputData(games);
  }
}
