package dev.totallyspies.spydle.frontend.use_cases.create_game;

import dev.totallyspies.spydle.shared.SharedConstants;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalCreateGameInteractor implements CreateGameInputBoundary {

  @Override
  public CreateGameOutputData execute(CreateGameInputData data) {
    UUID clientId = UUID.randomUUID();
    return new CreateGameOutputDataSuccess(
        "localhost", 7654, clientId, data.getPlayerName(), SharedConstants.LOCAL_SERVER_ROOM_CODE);
  }
}
