package dev.totallyspies.spydle.frontend.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.totallyspies.spydle.frontend.client.message.CbMessageListener;
import dev.totallyspies.spydle.frontend.client.message.CbMessageListenerProcessor;
import dev.totallyspies.spydle.shared.SharedConstants;
import dev.totallyspies.spydle.shared.message.MessageHandler;
import dev.totallyspies.spydle.shared.proto.messages.CbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbStartGame;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;

public class ClientSocketHandlerTest {

  private ClientSocketHandler clientSocketHandler;

  @Mock private CbMessageListenerProcessor annotationProcessor;

  @Mock private ApplicationEventPublisher eventPublisher;

  @Mock private WebSocketClient client;

  @Mock private WebSocketSession webSocketSession;

  @Captor private ArgumentCaptor<ClientSocketHandler.CloseEvent> closeEventCaptor;

  @Mock
  private MessageHandler<CbMessage, CbMessage.PayloadCase, CbMessageListener> cbMessageHandler;

  @BeforeEach
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);

    clientSocketHandler = new ClientSocketHandler(annotationProcessor, eventPublisher);

    // Replace the client with our mock
    Field clientField = ClientSocketHandler.class.getDeclaredField("client");
    clientField.setAccessible(true);
    clientField.set(clientSocketHandler, client);
  }

  @Test
  public void testOpen_Success() throws Exception {
    String address = "localhost";
    int port = 8080;
    UUID clientId = UUID.randomUUID();
    String playerName = "TestPlayer";
    String endpoint = SharedConstants.GAME_SOCKET_ENDPOINT;

    // Mock the client.execute() method
    CompletableFuture<WebSocketSession> futureSession = new CompletableFuture<>();
    futureSession.complete(webSocketSession);

    when(client.execute(eq(clientSocketHandler), any(WebSocketHttpHeaders.class), any(URI.class)))
        .thenReturn(futureSession);

    clientSocketHandler.open(address, port, clientId, playerName);

    // Verify that client.execute() was called with the correct parameters
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<WebSocketHttpHeaders> headersCaptor =
        ArgumentCaptor.forClass(WebSocketHttpHeaders.class);

    verify(client).execute(eq(clientSocketHandler), headersCaptor.capture(), uriCaptor.capture());

    URI expectedUri = new URI("ws://" + address + ":" + port + endpoint);
    assertEquals(expectedUri, uriCaptor.getValue());

    // Verify headers
    WebSocketHttpHeaders headers = headersCaptor.getValue();
    assertEquals(clientId.toString(), headers.getFirst(SharedConstants.CLIENT_ID_HTTP_HEADER));
    assertEquals(playerName, headers.getFirst(SharedConstants.CLIENT_NAME_HTTP_HEADER));

    // Verify that the session is set
    assertEquals(webSocketSession, clientSocketHandler.getSession());

    // Verify that the clientId is set
    assertEquals(clientId, clientSocketHandler.getClientId());
  }

  @Test
  public void testOpen_AlreadyOpen() {
    String address = "localhost";
    int port = 8080;
    UUID clientId = UUID.randomUUID();
    String playerName = "TestPlayer";

    // Set the session to a non-null value
    when(webSocketSession.isOpen()).thenReturn(true);
    clientSocketHandler.setSession(webSocketSession, clientId);

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              clientSocketHandler.open(address, port, clientId, playerName);
            });

    assertEquals("Cannot open client socket when it is already open!", exception.getMessage());
  }

  @Test
  public void testOpen_ClientExecuteException() throws Exception {
    String address = "localhost";
    int port = 8080;
    UUID clientId = UUID.randomUUID();
    String playerName = "TestPlayer";

    // Mock the client.execute() method to throw an exception
    when(client.execute(eq(clientSocketHandler), any(WebSocketHttpHeaders.class), any(URI.class)))
        .thenThrow(new RuntimeException("Test exception"));

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> clientSocketHandler.open(address, port, clientId, playerName));

    assertTrue(exception.getMessage().contains("Failed to open client socket"));
    assertTrue(exception.getCause().getMessage().contains("Test exception"));
  }

  @Test
  public void testHandleBinaryMessage_Success() throws Exception {
    WebSocketSession session = mock(WebSocketSession.class);
    byte[] payload = new byte[] {1, 2, 3};

    ByteBuffer byteBuffer = ByteBuffer.wrap(payload);
    BinaryMessage message = new BinaryMessage(byteBuffer);

    // Mock the clientId
    UUID clientId = clientSocketHandler.getClientId();

    // Mock the annotationProcessor and handler
    when(annotationProcessor.getHandler()).thenReturn(cbMessageHandler);

    // Mock CbMessage.parseFrom
    CbMessage cbMessage = mock(CbMessage.class);
    try (MockedStatic<CbMessage> cbMessageMockedStatic = Mockito.mockStatic(CbMessage.class)) {
      cbMessageMockedStatic.when(() -> CbMessage.parseFrom(payload)).thenReturn(cbMessage);

      // Call the method
      clientSocketHandler.handleBinaryMessage(session, message);

      // Verify that execute was called
      verify(cbMessageHandler).execute(cbMessage, clientId);
    }
  }

  @Test
  public void testHandleBinaryMessage_ParseException() throws Exception {
    WebSocketSession session = mock(WebSocketSession.class);
    byte[] payload = new byte[] {1, 2, 3};

    ByteBuffer byteBuffer = ByteBuffer.wrap(payload);
    BinaryMessage message = new BinaryMessage(byteBuffer);

    // Mock CbMessage.parseFrom to throw an exception
    try (MockedStatic<CbMessage> cbMessageMockedStatic = Mockito.mockStatic(CbMessage.class)) {
      cbMessageMockedStatic
          .when(() -> CbMessage.parseFrom(payload))
          .thenThrow(new InvalidProtocolBufferException("Test exception"));

      // Call the method
      assertThrows(
          InvalidProtocolBufferException.class,
          () -> clientSocketHandler.handleBinaryMessage(session, message));
    }
  }

  @Test
  public void testAfterConnectionEstablished() {
    WebSocketSession session = mock(WebSocketSession.class);

    // Call the method
    clientSocketHandler.afterConnectionEstablished(session);

    // No exception should be thrown
  }

  @Test
  public void testAfterConnectionClosed() {
    WebSocketSession session = mock(WebSocketSession.class);
    CloseStatus status = CloseStatus.NORMAL;

    // Set clientId
    UUID clientId = UUID.randomUUID();

    // Set session
    clientSocketHandler.setSession(session, clientId);

    // Call the method
    clientSocketHandler.afterConnectionClosed(session, status);

    // Verify that session is set to null
    assertNull(clientSocketHandler.getSession());

    // Verify that clientId is set to null
    assertNull(clientSocketHandler.getClientId());

    // Verify that CloseEvent is published
    verify(eventPublisher).publishEvent(closeEventCaptor.capture());

    ClientSocketHandler.CloseEvent closeEvent = closeEventCaptor.getValue();
    assertEquals(clientId, closeEvent.getClientId());
    assertEquals(status, closeEvent.getStatus());
  }

  @Test
  public void testSendSbMessage_Success() throws Exception {
    // Mock session
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);
    clientSocketHandler.setSession(session, UUID.randomUUID());

    SbMessage sbMessage = mock(SbMessage.class);
    byte[] messageBytes = new byte[] {1, 2, 3};
    when(sbMessage.toByteArray()).thenReturn(messageBytes);

    clientSocketHandler.sendSbMessage(sbMessage);

    // Verify that sendMessage was called
    verify(session).sendMessage(any(BinaryMessage.class));
  }

  @Test
  public void testSendSbMessage_SessionClosed() {
    // Session is null
    clientSocketHandler.removeSession();

    SbMessage sbMessage = mock(SbMessage.class);

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              clientSocketHandler.sendSbMessage(sbMessage);
            });

    assertEquals(
        "Cannot send server-bound message when session is closed!", exception.getMessage());
  }

  @Test
  public void testSendSbMessage_IOException() throws Exception {
    // Mock session
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);
    clientSocketHandler.setSession(session, UUID.randomUUID());

    SbMessage message =
        SbMessage.newBuilder()
            .setStartGame(SbStartGame.newBuilder().setTotalGameTimeSeconds(60).build())
            .build();

    // Mock sendMessage to throw IOException
    doThrow(new IOException("Test exception")).when(session).sendMessage(any(BinaryMessage.class));

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> clientSocketHandler.sendSbMessage(message));

    System.out.println(exception.getMessage());

    assertTrue(exception.getMessage().contains("Failed to send server packet"));
    assertTrue(exception.getCause().getMessage().contains("Test exception"));
  }

  @Test
  public void testIsOpen_SessionOpen() {
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);
    clientSocketHandler.setSession(session, UUID.randomUUID());

    assertTrue(clientSocketHandler.isOpen());
  }

  @Test
  public void testIsOpen_SessionClosed() {
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(false);
    clientSocketHandler.setSession(session, UUID.randomUUID());

    assertFalse(clientSocketHandler.isOpen());
  }

  @Test
  public void testIsOpen_SessionNull() {
    clientSocketHandler.removeSession();

    assertFalse(clientSocketHandler.isOpen());
  }

  @Test
  public void testClose_Success() throws Exception {
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);
    clientSocketHandler.setSession(session, UUID.randomUUID());

    CloseStatus status = CloseStatus.NORMAL;

    clientSocketHandler.close(status);

    // Verify that session.close() was called
    verify(session).close(status);
  }

  @Test
  public void testClose_SessionNotOpen() {
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(false);
    clientSocketHandler.setSession(session, UUID.randomUUID());

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              clientSocketHandler.close(CloseStatus.NORMAL);
            });

    assertEquals("Cannot close non-open session!", exception.getMessage());
  }

  @Test
  public void testClose_SessionNull() {
    clientSocketHandler.removeSession();

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              clientSocketHandler.close(CloseStatus.NORMAL);
            });

    assertEquals("Cannot close non-open session!", exception.getMessage());
  }

  @Test
  public void testClose_IOException() throws Exception {
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);
    clientSocketHandler.setSession(session, UUID.randomUUID());

    CloseStatus status = CloseStatus.NORMAL;

    doThrow(new IOException("Test exception")).when(session).close(status);

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              clientSocketHandler.close(status);
            });

    assertTrue(exception.getMessage().contains("Failed to close socket handler"));
    assertTrue(exception.getCause().getMessage().contains("Test exception"));
  }

  @Test
  public void testOnShutdown_SessionOpen() throws Exception {
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);
    clientSocketHandler.setSession(session, UUID.randomUUID());

    // Spy on clientSocketHandler to mock close method
    ClientSocketHandler spyHandler = spy(clientSocketHandler);
    doNothing().when(spyHandler).close(any(CloseStatus.class));

    spyHandler.onShutdown(new ContextClosedEvent(new GenericApplicationContext()));

    // Verify that close was called
    verify(spyHandler).close(argThat(status -> status.getCode() == CloseStatus.NORMAL.getCode()));
  }

  @Test
  public void testOnShutdown_SessionNotOpen() {
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(false);
    clientSocketHandler.setSession(session, UUID.randomUUID());

    // Spy on clientSocketHandler to verify that close is not called
    ClientSocketHandler spyHandler = spy(clientSocketHandler);

    spyHandler.onShutdown(new ContextClosedEvent(new GenericApplicationContext()));

    // Verify that close was not called
    verify(spyHandler, never()).close(any(CloseStatus.class));
  }
}
