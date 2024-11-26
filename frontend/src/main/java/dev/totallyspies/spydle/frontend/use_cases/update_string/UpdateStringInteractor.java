package dev.totallyspies.spydle.frontend.use_cases.update_string;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.frontend.use_cases.guess_word.GuessWordInteractor;
import dev.totallyspies.spydle.shared.proto.messages.SbGuess;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdateStringInteractor {
    private final ClientSocketHandler handler;
    private final Logger logger = LoggerFactory.getLogger(GuessWordInteractor.class);

    public UpdateStringInteractor(ClientSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void execute(){
        if (!handler.isOpen()) {
            logger.error("Cannot send guess word when client socket is not open!");
            return;
        }
        handler.sendSbMessage(SbMessage.newBuilder().setGuess(SbGuess.newBuilder().setGuessedWord(data.getWord())).build());
        logger.info("Sent guess {}", data.getWord());
    }
}
