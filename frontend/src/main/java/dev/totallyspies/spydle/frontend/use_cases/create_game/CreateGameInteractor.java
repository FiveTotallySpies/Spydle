package dev.totallyspies.spydle.frontend.use_cases.create_game;

import dev.totallyspies.spydle.frontend.client.rest.WebClientService;
import dev.totallyspies.spydle.matchmaker.generated.model.ClientErrorResponse;
import dev.totallyspies.spydle.matchmaker.generated.model.CreateGameRequestModel;
import dev.totallyspies.spydle.matchmaker.generated.model.CreateGameResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CreateGameInteractor implements CreateGameInputBoundary {

    @Autowired
    private WebClientService webClient;

    @Override
    public CreateGameOutputData execute(CreateGameInputData data) {
        UUID clientId = UUID.randomUUID();
        CreateGameRequestModel request = new CreateGameRequestModel()
                .clientId(clientId.toString())
                .playerName(data.getPlayerName());
        Object response = webClient.postEndpoint("/create-game", request, CreateGameResponseModel.class);
        if (response instanceof CreateGameResponseModel responseModel) {
            return new CreateGameOutputDataSuccess(
                    responseModel.getGameServer().getAddress(), // TODO fix
                    responseModel.getGameServer().getPort(),
                    UUID.fromString(responseModel.getClientId()),
                    responseModel.getPlayerName()
            );
        } else if (response instanceof ClientErrorResponse clientErrorModel) {
            return new CreateGameOutputDataFail(clientErrorModel.getMessage());
        }
        return new CreateGameOutputDataFail(response.toString());
    }

}
