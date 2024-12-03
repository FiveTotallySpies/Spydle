package dev.totallyspies.spydle.frontend.use_cases.join_game;

import static org.junit.jupiter.api.Assertions.*;

import dev.totallyspies.spydle.shared.SharedConstants;
import org.junit.jupiter.api.Test;

public class LocalJoinGameInteractorTest {

  private LocalJoinGameInteractor localJoinGameInteractor = new LocalJoinGameInteractor();

  @Test
  public void testExecute() {
    // Arrange
    String playerName = "TestPlayer";
    String roomCode = "ABCD";
    JoinGameInputData inputData = new JoinGameInputData(playerName, roomCode);

    // Act
    JoinGameOutputData outputData = localJoinGameInteractor.execute(inputData);

    // Assert
    assertInstanceOf(JoinGameOutputDataSuccess.class, outputData);
    JoinGameOutputDataSuccess successData = (JoinGameOutputDataSuccess) outputData;
    assertEquals("localhost", successData.getGameHost());
    assertEquals(7654, successData.getGamePort());
    assertEquals(playerName, successData.getPlayerName());
    assertEquals(SharedConstants.LOCAL_SERVER_ROOM_CODE, successData.getRoomCode());
    assertNotNull(successData.getClientId());
  }
}
