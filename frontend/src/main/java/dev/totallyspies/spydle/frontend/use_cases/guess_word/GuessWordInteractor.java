package dev.totallyspies.spydle.frontend.use_cases.guess_word;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.shared.proto.messages.SbGuess;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class GuessWordInteractor implements GuessWordInputBoundary {

    private final Logger logger = LoggerFactory.getLogger(GuessWordInteractor.class);

    private final ClientSocketHandler handler;

    public GuessWordInteractor(ClientSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void execute(GuessWordInputData data) {
        if (!handler.isOpen()) {
            logger.error("Cannot send guess word when client socket is not open!");
            return;
        }
        handler.sendSbMessage(SbMessage.newBuilder().setGuess(SbGuess.newBuilder().setGuessedWord(data.getWord())).build());
        logger.info("Sent guess {}", data.getWord());
    }

}
