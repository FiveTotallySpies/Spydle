package dev.totallyspies.spydle.frontend.use_cases.join_game;

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

public class JoinGameInteractorTest {

  @Mock private WebClientService webClientService;

  @InjectMocks private JoinGameInteractor joinGameInteractor;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    // Set the gameServerOverwrite value
    ReflectionTestUtils.setField(joinGameInteractor, "gameServerOverwrite", "");
  }

  @Test
  public void testExecute_Success_NoGameServerOverwrite() {
    // Arrange
    String playerName = "TestPlayer";
    String roomCode = "ABCD";
    JoinGameInputData inputData = new JoinGameInputData(playerName, roomCode);

    UUID clientId = UUID.randomUUID();
    ArgumentCaptor<JoinGameRequestModel> requestCaptor =
        ArgumentCaptor.forClass(JoinGameRequestModel.class);

    JoinGameResponseModel responseModel = new JoinGameResponseModel();
    responseModel.setClientId(clientId.toString());
    responseModel.setPlayerName(playerName);

    GameServer gameServerModel =
        new GameServer("127.0.0.1", 8080, "", "ABCDE", true, GameServer.State.WAITING);

    responseModel.setGameServer(gameServerModel);

    when(webClientService.postEndpoint(
            eq("/join-game"), any(JoinGameRequestModel.class), eq(JoinGameResponseModel.class)))
        .thenReturn(responseModel);

    // Act
    JoinGameOutputData outputData = joinGameInteractor.execute(inputData);

    // Assert
    assertTrue(outputData instanceof JoinGameOutputDataSuccess);
    JoinGameOutputDataSuccess successData = (JoinGameOutputDataSuccess) outputData;
    assertEquals(gameServerModel.getAddress(), successData.getGameHost());
    assertEquals(gameServerModel.getPort(), successData.getGamePort());
    assertEquals(clientId, successData.getClientId());
    assertEquals(playerName, successData.getPlayerName());
    assertEquals(gameServerModel.getRoomCode(), successData.getRoomCode());

    // Verify that webClientService was called correctly
    verify(webClientService)
        .postEndpoint(eq("/join-game"), requestCaptor.capture(), eq(JoinGameResponseModel.class));
    JoinGameRequestModel capturedRequest = requestCaptor.getValue();
    assertEquals(playerName, capturedRequest.getPlayerName());
    assertEquals(roomCode.toUpperCase(), capturedRequest.getRoomCode());
  }

  @Test
  public void testExecute_Success_WithGameServerOverwrite() {
    // Arrange
    String playerName = "TestPlayer";
    String roomCode = "ABCD";
    JoinGameInputData inputData = new JoinGameInputData(playerName, roomCode);

    // Set gameServerOverwrite
    String gameServerOverwrite = "overwritten-address";
    ReflectionTestUtils.setField(joinGameInteractor, "gameServerOverwrite", gameServerOverwrite);

    UUID clientId = UUID.randomUUID();

    JoinGameResponseModel responseModel = new JoinGameResponseModel();
    responseModel.setClientId(clientId.toString());
    responseModel.setPlayerName(playerName);

    GameServer gameServerModel =
        new GameServer("127.0.0.1", 8080, "", "ABCDE", true, GameServer.State.WAITING);

    responseModel.setGameServer(gameServerModel);

    when(webClientService.postEndpoint(
            eq("/join-game"), any(JoinGameRequestModel.class), eq(JoinGameResponseModel.class)))
        .thenReturn(responseModel);

    // Act
    JoinGameOutputData outputData = joinGameInteractor.execute(inputData);

    // Assert
    assertTrue(outputData instanceof JoinGameOutputDataSuccess);
    JoinGameOutputDataSuccess successData = (JoinGameOutputDataSuccess) outputData;
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
    String roomCode = "ABCD";
    JoinGameInputData inputData = new JoinGameInputData(playerName, roomCode);

    ClientErrorResponse clientErrorResponse = new ClientErrorResponse();
    clientErrorResponse.setMessage("Client error occurred");

    when(webClientService.postEndpoint(
            eq("/join-game"), any(JoinGameRequestModel.class), eq(JoinGameResponseModel.class)))
        .thenReturn(clientErrorResponse);

    // Act
    JoinGameOutputData outputData = joinGameInteractor.execute(inputData);

    // Assert
    assertTrue(outputData instanceof JoinGameOutputDataFail);
    JoinGameOutputDataFail failData = (JoinGameOutputDataFail) outputData;
    assertEquals(clientErrorResponse.getMessage(), failData.getMessage());
  }

  @Test
  public void testExecute_UnknownResponseType() {
    // Arrange
    String playerName = "TestPlayer";
    String roomCode = "ABCD";
    JoinGameInputData inputData = new JoinGameInputData(playerName, roomCode);

    String unexpectedResponse = "Unexpected response";

    when(webClientService.postEndpoint(
            eq("/join-game"), any(JoinGameRequestModel.class), eq(JoinGameResponseModel.class)))
        .thenReturn(unexpectedResponse);

    // Act
    JoinGameOutputData outputData = joinGameInteractor.execute(inputData);

    // Assert
    assertTrue(outputData instanceof JoinGameOutputDataFail);
    JoinGameOutputDataFail failData = (JoinGameOutputDataFail) outputData;
    assertEquals(unexpectedResponse, failData.getMessage());
  }
}
