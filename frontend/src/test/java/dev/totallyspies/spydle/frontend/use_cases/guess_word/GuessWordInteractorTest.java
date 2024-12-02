package dev.totallyspies.spydle.frontend.use_cases.guess_word;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import org.junit.jupiter.api.*;
import org.mockito.*;

public class GuessWordInteractorTest {

  @Mock private ClientSocketHandler clientSocketHandler;

  @InjectMocks private GuessWordInteractor guessWordInteractor;

  @Captor private ArgumentCaptor<SbMessage> sbMessageCaptor;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testExecute_HandlerOpen() {
    // Arrange
    when(clientSocketHandler.isOpen()).thenReturn(true);
    String word = "testword";
    GuessWordInputData inputData = new GuessWordInputData(word);

    // Act
    guessWordInteractor.execute(inputData);

    // Assert
    verify(clientSocketHandler).sendSbMessage(sbMessageCaptor.capture());
    SbMessage sentMessage = sbMessageCaptor.getValue();
    assertEquals(word, sentMessage.getGuess().getGuessedWord());
  }

  @Test
  public void testExecute_HandlerClosed() {
    // Arrange
    when(clientSocketHandler.isOpen()).thenReturn(false);
    String word = "testword";
    GuessWordInputData inputData = new GuessWordInputData(word);

    // Act
    guessWordInteractor.execute(inputData);

    // Assert
    // Should not send any message
    verify(clientSocketHandler, never()).sendSbMessage(any());
  }
}
