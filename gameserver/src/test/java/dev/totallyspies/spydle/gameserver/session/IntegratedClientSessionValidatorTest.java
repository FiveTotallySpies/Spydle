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

public class IntegratedClientSessionValidatorTest {

  private final GameServer fakeGameServer =
      new GameServer("", 0, "", "", false, GameServer.State.WAITING);
  private final ClientSession fakeClientSession =
      new ClientSession(UUID.randomUUID(), fakeGameServer, "player", ClientSession.State.ASSIGNED);
  private IntegratedClientSessionValidator validator;
  @Mock private GameServerStorage storage;
  @Mock private GameServer currentGameServer;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    validator = new IntegratedClientSessionValidator(storage, currentGameServer);
  }

  @Test
  public void testValidateClientSession_Success() {
    UUID clientId = UUID.randomUUID();
    String name = "Player1";

    ClientSession session =
        fakeClientSession.toBuilder().clientId(clientId).playerName(name).build();

    when(storage.getClientSession(clientId)).thenReturn(session);
    when(currentGameServer.isSameGameServer(fakeGameServer)).thenReturn(true);

    boolean result = validator.validateClientSession(clientId, name);

    assertTrue(result);
  }

  @Test
  public void testValidateClientSession_SessionNotFound() {
    UUID clientId = UUID.randomUUID();
    String name = "Player1";

    when(storage.getClientSession(clientId)).thenReturn(null);

    boolean result = validator.validateClientSession(clientId, name);

    assertFalse(result);
  }

  @Test
  public void testValidateClientSession_SessionWrongType() {
    UUID clientId = UUID.randomUUID();
    String name = "Player1";

    when(storage.getClientSession(clientId)).thenReturn(null);

    boolean result = validator.validateClientSession(clientId, name);

    assertFalse(result);
  }

  @Test
  public void testValidateClientSession_DifferentGameServer() {
    UUID clientId = UUID.randomUUID();
    String name = "Player1";

    GameServer anotherGameServer = mock(GameServer.class);

    ClientSession session = fakeClientSession.toBuilder().clientId(clientId).build();

    when(storage.getClientSession(clientId)).thenReturn(session);
    when(currentGameServer.isSameGameServer(anotherGameServer)).thenReturn(false);

    boolean result = validator.validateClientSession(clientId, name);

    assertFalse(result);
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
