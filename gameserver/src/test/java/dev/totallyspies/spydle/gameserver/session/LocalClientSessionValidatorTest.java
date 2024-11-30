package dev.totallyspies.spydle.gameserver.session;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.totallyspies.spydle.gameserver.storage.GameServerStorage;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

public class LocalClientSessionValidatorTest {

  private LocalClientSessionValidator validator;

  @Mock private GameServerStorage storage;

  @Mock private GameServer currentGameServer;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    validator = new LocalClientSessionValidator(storage, currentGameServer);
  }

  @Test
  public void testValidateClientSession() {
    UUID clientId = UUID.randomUUID();
    String name = "Player1";

    boolean result = validator.validateClientSession(clientId, name);

    assertTrue(result);
    verify(storage)
        .storeClientSession(
            argThat(
                session ->
                    session.getClientId().equals(clientId)
                        && session.getGameServer().equals(currentGameServer)
                        && session.getPlayerName().equals(name)
                        && session.getState() == ClientSession.State.ASSIGNED));
  }

  @Test
  public void testParseClientId_Valid() {
    UUID clientId = UUID.randomUUID();

    UUID result = validator.parseClientId(clientId.toString());

    assertEquals(clientId, result);
  }

  @Test
  public void testParseClientId_Invalid() {
    UUID result = validator.parseClientId("invalid-uuid");

    assertNull(result);
  }

  @Test
  public void testParseClientId_Null() {
    UUID result = validator.parseClientId(null);

    assertNull(result);
  }
}
