package dev.totallyspies.spydle.frontend.use_cases.list_games;

import static org.junit.jupiter.api.Assertions.*;

import dev.totallyspies.spydle.shared.SharedConstants;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class LocalListGamesInteractorTest {

  private LocalListGamesInteractor localListGamesInteractor = new LocalListGamesInteractor();

  @Test
  public void testExecute() {
    // Act
    ListGamesOutputData outputData = localListGamesInteractor.execute();

    // Assert
    assertEquals(Arrays.asList(SharedConstants.LOCAL_SERVER_ROOM_CODE), outputData.getRoomCodes());
  }
}
