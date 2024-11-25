package dev.totallyspies.spydle.frontend.use_cases.join_game;

import dev.totallyspies.spydle.shared.SharedConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("local")
public class LocalJoinGameInteractor implements JoinGameInputBoundary {

    @Override
    public JoinGameOutputData execute(JoinGameInputData data) {
        UUID clientId = UUID.randomUUID();
        return new JoinGameOutputDataSuccess(
                "localhost",
                7654,
                clientId,
                data.getPlayerName(),
                SharedConstants.LOCAL_SERVER_ROOM_CODE
        );
    }

}
