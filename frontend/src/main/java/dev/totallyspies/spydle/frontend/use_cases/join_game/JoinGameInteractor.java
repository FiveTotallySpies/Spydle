package dev.totallyspies.spydle.frontend.use_cases.join_game;

import dev.totallyspies.spydle.frontend.client.rest.WebClientService;
import dev.totallyspies.spydle.matchmaker.generated.model.ClientErrorResponse;
import dev.totallyspies.spydle.matchmaker.generated.model.JoinGameRequestModel;
import dev.totallyspies.spydle.matchmaker.generated.model.JoinGameResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JoinGameInteractor implements JoinGameInputBoundary {

    @Value("${server.backend.gameserver-overwrite}")
    private String gameServerOverwrite;

    @Autowired
    private WebClientService webClient;

    @Override
    public JoinGameOutputData execute(JoinGameInputData data) {
        UUID clientId = UUID.randomUUID();
        JoinGameRequestModel request = new JoinGameRequestModel()
                .clientId(clientId.toString())
                .playerName(data.getPlayerName())
                .roomCode(data.getRoomCode());
        Object response = webClient.postEndpoint("/create-game", request, JoinGameResponseModel.class);
        if (response instanceof JoinGameResponseModel responseModel) {
            String ip = gameServerOverwrite == null || gameServerOverwrite.isBlank()
                    ? responseModel.getGameServer().getAddress()
                    : gameServerOverwrite;
            return new JoinGameOutputDataSuccess(
                    ip,
                    responseModel.getGameServer().getPort(),
                    UUID.fromString(responseModel.getClientId()),
                    responseModel.getPlayerName()
            );
        } else if (response instanceof ClientErrorResponse clientErrorModel) {
            return new JoinGameOutputDataFail(clientErrorModel.getMessage());
        }
        return new JoinGameOutputDataFail(response.toString());
    }

}
