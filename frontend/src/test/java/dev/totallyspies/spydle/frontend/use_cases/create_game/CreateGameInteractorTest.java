package dev.totallyspies.spydle.frontend.use_cases.create_game;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import dev.totallyspies.spydle.frontend.client.rest.WebClientService;
import dev.totallyspies.spydle.matchmaker.generated.model.*;
import dev.totallyspies.spydle.shared.model.GameServer;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

public class CreateGameInteractorTest {

  @Mock private WebClientService webClientService;

  @InjectMocks private CreateGameInteractor createGameInteractor;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    // Set the gameServerOverwrite value
    ReflectionTestUtils.setField(createGameInteractor, "gameServerOverwrite", "");
  }

  @Test
  public void testExecute_Success_NoGameServerOverwrite() {
    // Arrange
    String playerName = "TestPlayer";
    CreateGameInputData inputData = new CreateGameInputData(playerName);

    UUID clientId = UUID.randomUUID();
    ArgumentCaptor<CreateGameRequestModel> requestCaptor =
        ArgumentCaptor.forClass(CreateGameRequestModel.class);

    CreateGameResponseModel responseModel = new CreateGameResponseModel();
    responseModel.setClientId(clientId.toString());
    responseModel.setPlayerName(playerName);

    GameServer gameServerModel =
        new GameServer("127.0.0.1", 8080, "", "ABCDE", true, GameServer.State.WAITING);

    responseModel.setGameServer(gameServerModel);

    when(webClientService.postEndpoint(
            eq("/create-game"),
            any(CreateGameRequestModel.class),
            eq(CreateGameResponseModel.class)))
        .thenReturn(responseModel);

    // Act
    CreateGameOutputData outputData = createGameInteractor.execute(inputData);

    // Assert
    assertTrue(outputData instanceof CreateGameOutputDataSuccess);
    CreateGameOutputDataSuccess successData = (CreateGameOutputDataSuccess) outputData;
    assertEquals(gameServerModel.getAddress(), successData.getGameHost());
    assertEquals(gameServerModel.getPort(), successData.getGamePort());
    assertEquals(clientId, successData.getClientId());
    assertEquals(playerName, successData.getPlayerName());
    assertEquals(gameServerModel.getRoomCode(), successData.getRoomCode());

    // Verify that webClientService was called correctly
    verify(webClientService)
        .postEndpoint(
            eq("/create-game"), requestCaptor.capture(), eq(CreateGameResponseModel.class));
    CreateGameRequestModel capturedRequest = requestCaptor.getValue();
    assertEquals(playerName, capturedRequest.getPlayerName());
  }

  @Test
  public void testExecute_Success_WithGameServerOverwrite() {
    // Arrange
    String playerName = "TestPlayer";
    CreateGameInputData inputData = new CreateGameInputData(playerName);

    // Set gameServerOverwrite
    String gameServerOverwrite = "overwritten-address";
    ReflectionTestUtils.setField(createGameInteractor, "gameServerOverwrite", gameServerOverwrite);

    UUID clientId = UUID.randomUUID();

    CreateGameResponseModel responseModel = new CreateGameResponseModel();
    responseModel.setClientId(clientId.toString());
    responseModel.setPlayerName(playerName);

    GameServer gameServerModel =
        new GameServer("127.0.0.1", 8080, "", "ABCDE", true, GameServer.State.WAITING);

    responseModel.setGameServer(gameServerModel);

    when(webClientService.postEndpoint(
            eq("/create-game"),
            any(CreateGameRequestModel.class),
            eq(CreateGameResponseModel.class)))
        .thenReturn(responseModel);

    // Act
    CreateGameOutputData outputData = createGameInteractor.execute(inputData);

    // Assert
    assertTrue(outputData instanceof CreateGameOutputDataSuccess);
    CreateGameOutputDataSuccess successData = (CreateGameOutputDataSuccess) outputData;
    assertEquals(gameServerOverwrite, successData.getGameHost()); // Should use overwritten address
    assertEquals(gameServerModel.getPort(), successData.getGamePort());
    assertEquals(clientId, successData.getClientId());
    assertEquals(playerName, successData.getPlayerName());
    assertEquals(gameServerModel.getRoomCode(), successData.getRoomCode());
  }

  @Test
  public void testExecute_ClientErrorResponse() {
    // Arrange
    String playerName = "TestPlayer";
    CreateGameInputData inputData = new CreateGameInputData(playerName);

    ClientErrorResponse clientErrorResponse = new ClientErrorResponse();
    clientErrorResponse.setMessage("Client error occurred");

    when(webClientService.postEndpoint(
            eq("/create-game"),
            any(CreateGameRequestModel.class),
            eq(CreateGameResponseModel.class)))
        .thenReturn(clientErrorResponse);

    // Act
    CreateGameOutputData outputData = createGameInteractor.execute(inputData);

    // Assert
    assertTrue(outputData instanceof CreateGameOutputDataFail);
    CreateGameOutputDataFail failData = (CreateGameOutputDataFail) outputData;
    assertEquals(clientErrorResponse.getMessage(), failData.getMessage());
  }

  @Test
  public void testExecute_UnknownResponseType() {
    // Arrange
    String playerName = "TestPlayer";
    CreateGameInputData inputData = new CreateGameInputData(playerName);

    String unexpectedResponse = "Unexpected response";

    when(webClientService.postEndpoint(
            eq("/create-game"),
            any(CreateGameRequestModel.class),
            eq(CreateGameResponseModel.class)))
        .thenReturn(unexpectedResponse);

    // Act
    CreateGameOutputData outputData = createGameInteractor.execute(inputData);

    // Assert
    assertTrue(outputData instanceof CreateGameOutputDataFail);
    CreateGameOutputDataFail failData = (CreateGameOutputDataFail) outputData;
    assertEquals(unexpectedResponse, failData.getMessage());
  }
}
