package dev.totallyspies.spydle.gameserver.socket;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.totallyspies.spydle.gameserver.agones.AgonesHook;
import dev.totallyspies.spydle.gameserver.session.ClientSessionValidator;
import dev.totallyspies.spydle.gameserver.session.SessionCloseEvent;
import dev.totallyspies.spydle.gameserver.session.SessionOpenEvent;
import dev.totallyspies.spydle.gameserver.storage.GameServerStorage;
import dev.totallyspies.spydle.shared.SharedConstants;
import dev.totallyspies.spydle.shared.message.MessageHandler;
import dev.totallyspies.spydle.shared.model.ClientSession;
import dev.totallyspies.spydle.shared.model.GameServer;
import dev.totallyspies.spydle.shared.proto.messages.CbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import net.infumia.agones4j.Agones;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GameSocketHandlerTest {

    private GameSocketHandler gameSocketHandler;

    @Mock
    private GameServerStorage storage;

    @Mock
    private ClientSessionValidator sessionValidator;

    @Mock
    private SbMessageListenerProcessor annotationProcessor;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private GameServer currentGameServer;

    @Mock
    private Agones agones;

    @Mock
    private MessageHandler<SbMessage, SbMessage.PayloadCase, SbMessageListener> sbMessageHandler;

    private final GameServer fakeGameServer = new GameServer("", 0, "", "", false, GameServer.State.WAITING);
    private final ClientSession fakeClientSession = new ClientSession(UUID.randomUUID(), fakeGameServer, "player", ClientSession.State.ASSIGNED);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        gameSocketHandler = new GameSocketHandler(
                storage,
                sessionValidator,
                annotationProcessor,
                publisher,
                currentGameServer,
                true,
                agones
        );

        // Set up the handler in the annotationProcessor
        when(annotationProcessor.getHandler()).thenReturn(sbMessageHandler);
    }

    @Test
    public void testHandleBinaryMessage_ValidMessage() throws Exception {
        UUID clientId = UUID.randomUUID();
        String clientName = "TestClient";

        WebSocketSession socketSession = mock(WebSocketSession.class);
        BinaryMessage binaryMessage = mock(BinaryMessage.class);

        // Mock headers
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(SharedConstants.CLIENT_ID_HTTP_HEADER, Collections.singletonList(clientId.toString()));
        headers.put(SharedConstants.CLIENT_NAME_HTTP_HEADER, Collections.singletonList(clientName));
        when(socketSession.getHandshakeHeaders()).thenReturn(new org.springframework.http.HttpHeaders(headers));

        // Mock sessionValidator
        when(sessionValidator.parseClientId(clientId.toString())).thenReturn(clientId);
        when(sessionValidator.validateClientSession(clientId, clientName)).thenReturn(true);

        // Mock message payload
        byte[] payload = new byte[]{1, 2, 3}; // Example bytes
        ByteBuffer payloadBuffer = ByteBuffer.wrap(payload);
        when(binaryMessage.getPayload()).thenReturn(payloadBuffer);

        // Mock SbMessage parsing
        SbMessage sbMessage = mock(SbMessage.class);
        // Use a wrapper or utility method to parse SbMessage if necessary

        // Since SbMessage.parseFrom is a static method, we can use the real method
        // or use a utility class to abstract it if necessary.

        // For this test, we'll assume parseFrom works as expected
        try (MockedStatic<SbMessage> sbMessageMockedStatic = Mockito.mockStatic(SbMessage.class)) {
            sbMessageMockedStatic.when(() -> SbMessage.parseFrom(payload)).thenReturn(sbMessage);

            // Call the method
            gameSocketHandler.handleBinaryMessage(socketSession, binaryMessage);

            // Verify that the handler's execute method was called
            verify(sbMessageHandler).execute(sbMessage, clientId);
        }
    }

    @Test
    public void testHandleBinaryMessage_MissingClientId() {
        WebSocketSession socketSession = mock(WebSocketSession.class);
        BinaryMessage binaryMessage = mock(BinaryMessage.class);

        // Mock headers without CLIENT_ID_HTTP_HEADER
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(SharedConstants.CLIENT_NAME_HTTP_HEADER, Collections.singletonList("TestClient"));
        when(socketSession.getHandshakeHeaders()).thenReturn(new org.springframework.http.HttpHeaders(headers));

        // Mock sessionValidator
        when(sessionValidator.parseClientId(null)).thenReturn(null);

        // Call the method
        gameSocketHandler.handleBinaryMessage(socketSession, binaryMessage);

        // Verify that sessionValidator.validateClientSession is not called
        verify(sessionValidator, never()).validateClientSession(any(), anyString());

        // Verify that no further actions are taken
        verifyNoInteractions(annotationProcessor);
    }

    @Test
    public void testHandleBinaryMessage_InvalidClientSession() throws Exception {
        UUID clientId = UUID.randomUUID();
        String clientName = "TestClient";

        WebSocketSession socketSession = mock(WebSocketSession.class);
        BinaryMessage binaryMessage = mock(BinaryMessage.class);

        // Mock headers
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(SharedConstants.CLIENT_ID_HTTP_HEADER, Collections.singletonList(clientId.toString()));
        headers.put(SharedConstants.CLIENT_NAME_HTTP_HEADER, Collections.singletonList(clientName));
        when(socketSession.getHandshakeHeaders()).thenReturn(new org.springframework.http.HttpHeaders(headers));

        // Mock sessionValidator
        when(sessionValidator.parseClientId(clientId.toString())).thenReturn(clientId);
        when(sessionValidator.validateClientSession(clientId, clientName)).thenReturn(false);

        // Call the method
        gameSocketHandler.handleBinaryMessage(socketSession, binaryMessage);

        // Verify that the session is closed
        verify(socketSession).close(argThat(status -> status.getCode() == CloseStatus.NOT_ACCEPTABLE.getCode()));

        // Verify that no further actions are taken
        verifyNoInteractions(annotationProcessor);
    }

    @Test
    public void testHandleBinaryMessage_ExceptionDuringParsing() throws Exception {
        UUID clientId = UUID.randomUUID();
        String clientName = "TestClient";

        WebSocketSession socketSession = mock(WebSocketSession.class);
        BinaryMessage binaryMessage = mock(BinaryMessage.class);

        // Mock headers
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(SharedConstants.CLIENT_ID_HTTP_HEADER, Collections.singletonList(clientId.toString()));
        headers.put(SharedConstants.CLIENT_NAME_HTTP_HEADER, Collections.singletonList(clientName));
        when(socketSession.getHandshakeHeaders()).thenReturn(new org.springframework.http.HttpHeaders(headers));

        // Mock sessionValidator
        when(sessionValidator.parseClientId(clientId.toString())).thenReturn(clientId);
        when(sessionValidator.validateClientSession(clientId, clientName)).thenReturn(true);

        // Mock message payload
        byte[] payload = new byte[]{1, 2, 3}; // Invalid bytes causing parse exception
        ByteBuffer payloadBuffer = ByteBuffer.wrap(payload);
        when(binaryMessage.getPayload()).thenReturn(payloadBuffer);

        // Mock SbMessage.parseFrom to throw exception
        try (MockedStatic<SbMessage> sbMessageMockedStatic = Mockito.mockStatic(SbMessage.class)) {
            sbMessageMockedStatic.when(() -> SbMessage.parseFrom(payload))
                    .thenThrow(new InvalidProtocolBufferException("Invalid message"));

            // Call the method
            gameSocketHandler.handleBinaryMessage(socketSession, binaryMessage);

            // Verify that an error was logged
            // We can use a logging framework or just assume that exception is handled
        }
    }

    @Test
    public void testAfterConnectionEstablished_Success() throws Exception {
        UUID clientId = UUID.randomUUID();
        String clientName = "TestClient";

        WebSocketSession socketSession = mock(WebSocketSession.class);

        // Mock headers
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(SharedConstants.CLIENT_ID_HTTP_HEADER, Collections.singletonList(clientId.toString()));
        headers.put(SharedConstants.CLIENT_NAME_HTTP_HEADER, Collections.singletonList(clientName));
        when(socketSession.getHandshakeHeaders()).thenReturn(new org.springframework.http.HttpHeaders(headers));

        // Mock sessionValidator
        when(sessionValidator.parseClientId(clientId.toString())).thenReturn(clientId);
        when(sessionValidator.validateClientSession(clientId, clientName)).thenReturn(true);

        // Mock storage
        ClientSession storedSession = fakeClientSession.toBuilder()
                .clientId(clientId)
                .playerName(clientName)
                .state(ClientSession.State.ASSIGNED)
                .build();
        when(storage.getClientSession(clientId)).thenReturn(storedSession);

        // Call the method
        gameSocketHandler.afterConnectionEstablished(socketSession);

        // Verify that the session is updated to CONNECTED and stored
        assertEquals(ClientSession.State.CONNECTED, storedSession.getState());
        verify(storage).storeClientSession(storedSession);

        // Verify that the session is added to sessions map
        assertTrue(gameSocketHandler.getSessions().contains(storedSession));

        // Simulate the socket session becoming open
        when(socketSession.isOpen()).thenReturn(true);

        // Since the scheduler in afterConnectionEstablished runs asynchronously,
        // we can wait for a short period to let it execute
        Thread.sleep(100); // Adjust as necessary

        // Verify that the SessionOpenEvent is published
        verify(publisher).publishEvent(any(SessionOpenEvent.class));
    }

    @Test
    public void testAfterConnectionEstablished_ClientNameAlreadyExists() throws Exception {
        UUID existingClientId = UUID.randomUUID();
        String clientName = "TestClient";

        // Existing session with the same name
        ClientSession existingSession = fakeClientSession.toBuilder()
                .clientId(existingClientId)
                .playerName(clientName)
                .build();

        WebSocketSession existingSocketSession = mock(WebSocketSession.class);

        // Add the existing session to the sessions map
        gameSocketHandler.getSessionsMap().put(existingSession, existingSocketSession);

        // Now attempt to connect with a new client with the same name
        UUID newClientId = UUID.randomUUID();
        WebSocketSession newSocketSession = mock(WebSocketSession.class);

        // Mock headers for the new session
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(SharedConstants.CLIENT_ID_HTTP_HEADER, Collections.singletonList(newClientId.toString()));
        headers.put(SharedConstants.CLIENT_NAME_HTTP_HEADER, Collections.singletonList(clientName)); // Same name
        when(newSocketSession.getHandshakeHeaders()).thenReturn(new org.springframework.http.HttpHeaders(headers));

        // Mock sessionValidator
        when(sessionValidator.parseClientId(newClientId.toString())).thenReturn(newClientId);
        when(sessionValidator.validateClientSession(newClientId, clientName)).thenReturn(true);

        // Call the method
        gameSocketHandler.afterConnectionEstablished(newSocketSession);

        // Verify that the session is closed
        verify(newSocketSession).close(argThat(status -> status.getCode() == CloseStatus.NOT_ACCEPTABLE.getCode()));

        // Verify that no further actions are taken
        verifyNoInteractions(storage);
    }


    @Test
    public void testAfterConnectionClosed_NormalClosure() throws Exception {
        UUID clientId = UUID.randomUUID();
        String clientName = "TestClient";

        WebSocketSession socketSession = mock(WebSocketSession.class);
        CloseStatus status = CloseStatus.NORMAL;

        // Mock headers
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(SharedConstants.CLIENT_ID_HTTP_HEADER, Collections.singletonList(clientId.toString()));
        headers.put(SharedConstants.CLIENT_NAME_HTTP_HEADER, Collections.singletonList(clientName));
        when(socketSession.getHandshakeHeaders()).thenReturn(new org.springframework.http.HttpHeaders(headers));

        // Mock sessionValidator
        when(sessionValidator.parseClientId(clientId.toString())).thenReturn(clientId);
        when(sessionValidator.validateClientSession(clientId, clientName)).thenReturn(true);

        // Mock stored session
        ClientSession storedSession = fakeClientSession.toBuilder()
                .clientId(clientId)
                .playerName(clientName)
                .build();
        gameSocketHandler.getSessionsMap().put(storedSession, socketSession);

        // Call the method
        gameSocketHandler.afterConnectionClosed(socketSession, status);

        // Verify that client session is deleted from storage
        verify(storage).deleteClientSession(clientId);

        // Verify that session is removed from sessions map
        assertFalse(gameSocketHandler.getSessions().contains(storedSession));

        // Verify that SessionCloseEvent is published
        verify(publisher).publishEvent(any(SessionCloseEvent.class));
    }

    @Test
    public void testAfterConnectionClosed_AllPlayersLeft_AgonesShutdown() throws Exception {
        // Mock currentGameServer state is not READY
        when(currentGameServer.getState()).thenReturn(GameServer.State.PLAYING);

        // Ensure sessions are empty
        assertTrue(gameSocketHandler.getSessions().isEmpty());

        // Create a mock WebSocketSession
        WebSocketSession socketSession = mock(WebSocketSession.class);

        // Mock getHandshakeHeaders() to return a non-null HttpHeaders
        HttpHeaders headers = new HttpHeaders();
        headers.add(SharedConstants.CLIENT_ID_HTTP_HEADER, UUID.randomUUID().toString());
        headers.add(SharedConstants.CLIENT_NAME_HTTP_HEADER, "TestClient");
        when(socketSession.getHandshakeHeaders()).thenReturn(headers);

        // Mock sessionValidator methods
        when(sessionValidator.parseClientId(anyString())).thenReturn(UUID.randomUUID());
        when(sessionValidator.validateClientSession(any(UUID.class), anyString())).thenReturn(true);

        // Call the method
        gameSocketHandler.afterConnectionClosed(socketSession, CloseStatus.NORMAL);

        // Verify that agonesHook.getAgones().shutdown() is called
        verify(agones).shutdown();
    }


    @Test
    public void testSendCbMessage_Success() throws Exception {
        UUID clientId = UUID.randomUUID();
        ClientSession clientSession = fakeClientSession.toBuilder().clientId(clientId).build();

        WebSocketSession socketSession = mock(WebSocketSession.class);
        gameSocketHandler.getSessionsMap().put(clientSession, socketSession);

        CbMessage message = CbMessage.newBuilder().build();

        gameSocketHandler.sendCbMessage(clientId, message);

        // Verify that the message is sent
        verify(socketSession).sendMessage(any(BinaryMessage.class));
    }

    @Test
    public void testSendCbMessage_InvalidClient() {
        UUID clientId = UUID.randomUUID();
        CbMessage message = CbMessage.newBuilder().build();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gameSocketHandler.sendCbMessage(clientId, message);
        });

        String expectedMessage = "Cannot send message to invalid client " + clientId;
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testCloseAllSessions() throws Exception {
        ClientSession clientSession1 = fakeClientSession.toBuilder().clientId(UUID.randomUUID()).build();
        ClientSession clientSession2 = fakeClientSession.toBuilder().clientId(UUID.randomUUID()).build();

        WebSocketSession socketSession1 = mock(WebSocketSession.class);
        WebSocketSession socketSession2 = mock(WebSocketSession.class);

        gameSocketHandler.getSessionsMap().put(clientSession1, socketSession1);
        gameSocketHandler.getSessionsMap().put(clientSession2, socketSession2);

        CloseStatus status = CloseStatus.NORMAL;

        gameSocketHandler.closeAllSessions(status);

        // Verify that both sessions are closed
        verify(socketSession1).close(status);
        verify(socketSession2).close(status);
    }
}
