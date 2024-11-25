package dev.totallyspies.spydle.frontend.use_cases.create_game;

import dev.totallyspies.spydle.frontend.client.rest.WebClientService;
import dev.totallyspies.spydle.matchmaker.generated.model.ClientErrorResponse;
import dev.totallyspies.spydle.matchmaker.generated.model.CreateGameRequestModel;
import dev.totallyspies.spydle.matchmaker.generated.model.CreateGameResponseModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("!local")
public class CreateGameInteractor implements CreateGameInputBoundary {

    private final String gameServerOverwrite;
    private final WebClientService webClient;

    public CreateGameInteractor(@Value("${server.backend.gameserver-overwrite}") String gameServerOverwrite, WebClientService webClient) {
        this.gameServerOverwrite = gameServerOverwrite;
        this.webClient = webClient;
    }

    @Override
    public CreateGameOutputData execute(CreateGameInputData data) {
        UUID clientId = UUID.randomUUID();
        CreateGameRequestModel request = new CreateGameRequestModel()
                .clientId(clientId.toString())
                .playerName(data.getPlayerName());
        Object response = webClient.postEndpoint("/create-game", request, CreateGameResponseModel.class);
        if (response instanceof CreateGameResponseModel responseModel) {
            String ip = gameServerOverwrite == null || gameServerOverwrite.isBlank()
                    ? responseModel.getGameServer().getAddress()
                    : gameServerOverwrite;
            return new CreateGameOutputDataSuccess(
                    ip,
                    responseModel.getGameServer().getPort(),
                    UUID.fromString(responseModel.getClientId()),
                    responseModel.getPlayerName(),
                    responseModel.getGameServer().getRoomCode()
            );
        } else if (response instanceof ClientErrorResponse clientErrorModel) {
            return new CreateGameOutputDataFail(clientErrorModel.getMessage());
        }
        return new CreateGameOutputDataFail(response.toString());
    }

}
