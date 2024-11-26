package dev.totallyspies.spydle.frontend.use_cases.update_string;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdateStringInteractor implements UpdateStringInputBoundary{
    private final ClientSocketHandler handler;
    private final Logger logger = LoggerFactory.getLogger(UpdateStringInteractor.class);

    public UpdateStringInteractor(ClientSocketHandler handler) {
        this.handler = handler;
    }

    public void execute(UpdateStringInputData updateStringInputData){
        if (!handler.isOpen()) {
            logger.error("Cannot send guess word when client socket is not open!");
            return;
        }
        //KAI TODO
//        handler.sendSbMessage(SbMessage.newBuilder().setGuess(SbGuess.newBuilder().setGuessedWord(updateStringInputData.getGuess())).build());
        logger.info("Sent guess {}", updateStringInputData.getUpdateString());
    }
}