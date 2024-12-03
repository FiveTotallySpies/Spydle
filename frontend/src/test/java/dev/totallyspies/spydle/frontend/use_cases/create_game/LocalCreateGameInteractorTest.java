package dev.totallyspies.spydle.frontend.use_cases.create_game;

import static org.junit.jupiter.api.Assertions.*;

import dev.totallyspies.spydle.shared.SharedConstants;
import org.junit.jupiter.api.Test;

public class LocalCreateGameInteractorTest {

  private LocalCreateGameInteractor localCreateGameInteractor = new LocalCreateGameInteractor();

  @Test
  public void testExecute() {
    // Arrange
    String playerName = "TestPlayer";
    CreateGameInputData inputData = new CreateGameInputData(playerName);

    // Act
    CreateGameOutputData outputData = localCreateGameInteractor.execute(inputData);

    // Assert
    assertInstanceOf(CreateGameOutputDataSuccess.class, outputData);
    CreateGameOutputDataSuccess successData = (CreateGameOutputDataSuccess) outputData;
    assertEquals("localhost", successData.getGameHost());
    assertEquals(7654, successData.getGamePort());
    assertEquals(playerName, successData.getPlayerName());
    assertEquals(SharedConstants.LOCAL_SERVER_ROOM_CODE, successData.getRoomCode());
    assertNotNull(successData.getClientId());
  }
}
