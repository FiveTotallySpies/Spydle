package dev.totallyspies.spydle.gameserver.storage;

import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LocalStorageTest {

    private LocalStorage localStorage;

    private final GameServer fakeGameServer = new GameServer("", 0, "", "", false, GameServer.State.WAITING);
    private final ClientSession fakeClientSession = new ClientSession(UUID.randomUUID(), fakeGameServer, "player", ClientSession.State.ASSIGNED);

    @BeforeEach
    public void setUp() {
        localStorage = new LocalStorage();
    }

    @Test
    public void testStoreAndGetGameServer() {
        String roomCode = "ROOM1";
        GameServer gameServer = fakeGameServer.toBuilder().roomCode(roomCode).build();

        localStorage.storeGameServer(gameServer);

        GameServer result = localStorage.getGameServer(roomCode);

        assertEquals(gameServer, result);
    }

    @Test
    public void testGetGameServer_NotFound() {
        GameServer result = localStorage.getGameServer("NON_EXISTENT");

        assertNull(result);
    }

    @Test
    public void testDeleteGameServer() {
        String roomCode = "ROOM1";
        GameServer gameServer = fakeGameServer.toBuilder().roomCode(roomCode).build();

        localStorage.storeGameServer(gameServer);
        localStorage.deleteGameServer(roomCode);

        GameServer result = localStorage.getGameServer(roomCode);

        assertNull(result);
    }

    @Test
    public void testStoreAndGetClientSession() {
        UUID clientId = UUID.randomUUID();
        ClientSession session = fakeClientSession.toBuilder().clientId(clientId).build();

        localStorage.storeClientSession(session);

        ClientSession result = localStorage.getClientSession(clientId);

        assertEquals(session, result);
    }

    @Test
    public void testGetClientSession_NotFound() {
        ClientSession result = localStorage.getClientSession(UUID.randomUUID());

        assertNull(result);
    }

    @Test
    public void testDeleteClientSession() {
        UUID clientId = UUID.randomUUID();
        ClientSession session = fakeClientSession.toBuilder().clientId(clientId).build();

        localStorage.storeClientSession(session);
        localStorage.deleteClientSession(clientId);

        ClientSession result = localStorage.getClientSession(clientId);

        assertNull(result);
    }
}
