package dev.totallyspies.spydle.frontend.use_cases.update_guess;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import org.junit.jupiter.api.*;
import org.mockito.*;

public class UpdateGuessInteractorTest {

  @Mock private ClientSocketHandler clientSocketHandler;

  @InjectMocks private UpdateGuessInteractor updateGuessInteractor;

  @Captor private ArgumentCaptor<SbMessage> sbMessageCaptor;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testExecute_HandlerOpen() {
    // Arrange
    when(clientSocketHandler.isOpen()).thenReturn(true);
    String guess = "partialguess";
    UpdateGuessInputData inputData = new UpdateGuessInputData(guess);

    // Act
    updateGuessInteractor.execute(inputData);

    // Assert
    verify(clientSocketHandler).sendSbMessage(sbMessageCaptor.capture());
    SbMessage sentMessage = sbMessageCaptor.getValue();
    assertEquals(guess, sentMessage.getGuessUpdate().getGuessedWord());
  }

  @Test
  public void testExecute_HandlerClosed() {
    // Arrange
    when(clientSocketHandler.isOpen()).thenReturn(false);
    String guess = "partialguess";
    UpdateGuessInputData inputData = new UpdateGuessInputData(guess);

    // Act
    updateGuessInteractor.execute(inputData);

    // Assert
    // Should not send any message
    verify(clientSocketHandler, never()).sendSbMessage(any());
  }
}
