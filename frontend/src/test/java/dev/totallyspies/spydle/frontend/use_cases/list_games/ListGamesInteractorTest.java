package dev.totallyspies.spydle.frontend.use_cases.list_games;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.totallyspies.spydle.frontend.client.rest.WebClientService;
import dev.totallyspies.spydle.matchmaker.generated.model.*;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

public class ListGamesInteractorTest {

  @Mock private WebClientService webClientService;

  @InjectMocks private ListGamesInteractor listGamesInteractor;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testExecute_Success() {
    // Arrange
    List<String> roomCodes = Arrays.asList("ABCD", "EFGH");
    ListGamesResponseModel responseModel = new ListGamesResponseModel();
    responseModel.setRoomCodes(roomCodes);

    when(webClientService.getEndpoint(eq("/list-games"), eq(ListGamesResponseModel.class)))
        .thenReturn(responseModel);

    // Act
    ListGamesOutputData outputData = listGamesInteractor.execute();

    // Assert
    assertEquals(roomCodes, outputData.getRoomCodes());

    // Verify interactions
    verify(webClientService).getEndpoint(eq("/list-games"), eq(ListGamesResponseModel.class));
  }

  @Test
  public void testExecute_ClientErrorResponse() {
    // Arrange
    ClientErrorResponse clientErrorResponse = new ClientErrorResponse();
    clientErrorResponse.setMessage("Client error occurred");

    when(webClientService.getEndpoint(eq("/list-games"), eq(ListGamesResponseModel.class)))
        .thenReturn(clientErrorResponse);

    // Act
    ListGamesOutputData outputData = listGamesInteractor.execute();

    // Assert
    assertTrue(outputData.getRoomCodes().isEmpty());
  }

  @Test
  public void testExecute_UnknownResponseType() {
    // Arrange
    String unexpectedResponse = "Unexpected response";

    when(webClientService.getEndpoint(eq("/list-games"), eq(ListGamesResponseModel.class)))
        .thenReturn(unexpectedResponse);

    // Act
    ListGamesOutputData outputData = listGamesInteractor.execute();

    // Assert
    assertTrue(outputData.getRoomCodes().isEmpty());
  }
}
