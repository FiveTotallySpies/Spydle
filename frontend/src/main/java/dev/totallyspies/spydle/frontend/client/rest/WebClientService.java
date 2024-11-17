package dev.totallyspies.spydle.frontend.client.rest;

import dev.totallyspies.spydle.matchmaker.generated.model.ClientErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WebClientService {

    @Autowired
    private WebClient webClient;

    public <T> T postEndpoint(String path, Object data, Class<T> responseModel) throws ClientErrorException {
        return handleResponse(webClient.post().uri(formatPath(path)).bodyValue(data).retrieve(), responseModel);

    }

    public <T> T getEndpoint(String path, Class<T> responseModel) {
        return handleResponse(webClient.get().uri(formatPath(path)).retrieve(), responseModel);
    }

    private <T> T handleResponse(WebClient.ResponseSpec responseSpec, Class<T> responseModel) throws ClientErrorException {
        return responseSpec
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse
                                .bodyToMono(ClientErrorResponse.class)
                                .flatMap(errorResponse ->
                                        Mono.error(new ClientErrorException(
                                                errorResponse,
                                                errorResponse.getMessage(),
                                                clientResponse.statusCode().value()))))
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new ServerErrorException(
                                        errorBody,
                                        clientResponse.statusCode().value()))))
                .bodyToMono(responseModel)
                .block();
    }

    private static String formatPath(String path) {
        if (path.startsWith("/")) return path;
        return "/" + path;
    }

}
