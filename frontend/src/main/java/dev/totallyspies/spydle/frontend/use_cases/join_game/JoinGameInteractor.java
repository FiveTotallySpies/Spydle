package dev.totallyspies.spydle.frontend.use_cases.join_game;

import dev.totallyspies.spydle.frontend.client.rest.WebClientService;
import dev.totallyspies.spydle.matchmaker.generated.model.ClientErrorResponse;
import dev.totallyspies.spydle.matchmaker.generated.model.JoinGameRequestModel;
import dev.totallyspies.spydle.matchmaker.generated.model.JoinGameResponseModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("!test & !local")
public class JoinGameInteractor implements JoinGameInputBoundary {

    private final String gameServerOverwrite;
    private final WebClientService webClient;

    public JoinGameInteractor(@Value("${server.backend.gameserver-overwrite}") String gameServerOverwrite, WebClientService webClient) {
        this.gameServerOverwrite = gameServerOverwrite;
        this.webClient = webClient;
    }

    @Override
    public JoinGameOutputData execute(JoinGameInputData data) {
        UUID clientId = UUID.randomUUID();
        JoinGameRequestModel request = new JoinGameRequestModel()
                .clientId(clientId.toString())
                .playerName(data.getPlayerName())
                .roomCode(data.getRoomCode().toUpperCase());
        Object response = webClient.postEndpoint("/join-game", request, JoinGameResponseModel.class);
        if (response instanceof JoinGameResponseModel responseModel) {
            String ip = gameServerOverwrite == null || gameServerOverwrite.isBlank()
                    ? responseModel.getGameServer().getAddress()
                    : gameServerOverwrite;
            return new JoinGameOutputDataSuccess(
                    ip,
                    responseModel.getGameServer().getPort(),
                    UUID.fromString(responseModel.getClientId()),
                    responseModel.getPlayerName(),
                    responseModel.getGameServer().getRoomCode()
            );
        } else if (response instanceof ClientErrorResponse clientErrorModel) {
            return new JoinGameOutputDataFail(clientErrorModel.getMessage());
        }
        return new JoinGameOutputDataFail(response.toString());
    }

}
