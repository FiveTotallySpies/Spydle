package dev.totallyspies.spydle.frontend.use_cases.update_guess;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.shared.proto.messages.SbGuessUpdate;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class UpdateGuessInteractor implements UpdateGuessInputBoundary {

  private final Logger logger = LoggerFactory.getLogger(UpdateGuessInteractor.class);

  private final ClientSocketHandler handler;

  public UpdateGuessInteractor(ClientSocketHandler handler) {
    this.handler = handler;
  }

  @Override
  public void execute(UpdateGuessInputData updateGuessInputData) {
    if (!handler.isOpen()) {
      logger.error("Cannot send update guess when client socket is not open!");
      return;
    }
    handler.sendSbMessage(
        SbMessage.newBuilder()
            .setGuessUpdate(
                SbGuessUpdate.newBuilder().setGuessedWord(updateGuessInputData.getGuess()))
            .build());
    logger.info("Sent guess update: {}", updateGuessInputData.getGuess());
  }
}
