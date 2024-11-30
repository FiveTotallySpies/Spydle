package dev.totallyspies.spydle.frontend.client.rest;

import dev.totallyspies.spydle.matchmaker.generated.model.ClientErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WebClientService {

  private final Logger logger = LoggerFactory.getLogger(WebClientService.class);

  private final WebClient webClient;

  public WebClientService(WebClient webClient) {
    this.webClient = webClient;
  }

  private static String formatPath(String path) {
    if (path.startsWith("/")) return path;
    return "/" + path;
  }

  public <T> Object postEndpoint(String path, Object data, Class<T> responseModel)
      throws ClientErrorException {
    Object response =
        handleResponse(
            webClient.post().uri(formatPath(path)).bodyValue(data).retrieve(), responseModel);
    logger.info("Made POST request to {} path with {} data, got response {}", path, data, response);
    return response;
  }

  public <T> Object getEndpoint(String path, Class<T> responseModel) {
    Object response =
        handleResponse(webClient.get().uri(formatPath(path)).retrieve(), responseModel);
    logger.info("Made GET request to {} path, got response {}", path, response);
    return response;
  }

  private <T> Object handleResponse(WebClient.ResponseSpec responseSpec, Class<T> responseModel)
      throws ClientErrorException {
    return responseSpec
        .onStatus(
            HttpStatusCode::is4xxClientError,
            clientResponse ->
                clientResponse
                    .bodyToMono(ClientErrorResponse.class)
                    .flatMap(
                        errorResponse ->
                            Mono.error(
                                new ClientErrorException(
                                    errorResponse,
                                    errorResponse.getMessage(),
                                    clientResponse.statusCode().value()))))
        .onStatus(
            HttpStatusCode::is5xxServerError,
            clientResponse ->
                clientResponse
                    .bodyToMono(String.class)
                    .flatMap(
                        errorBody ->
                            Mono.error(
                                new ServerErrorException(
                                    errorBody, clientResponse.statusCode().value()))))
        .bodyToMono(responseModel)
        .block();
  }
}
