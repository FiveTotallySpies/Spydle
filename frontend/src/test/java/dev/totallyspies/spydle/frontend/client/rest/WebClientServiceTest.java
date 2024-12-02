package dev.totallyspies.spydle.frontend.client.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.totallyspies.spydle.matchmaker.generated.model.ClientErrorResponse;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

public class WebClientServiceTest {

  private WebClientService webClientService;

  @Mock private WebClient webClient;

  @Mock private WebClient.RequestBodyUriSpec requestBodyUriSpec;

  @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock private WebClient.RequestBodySpec requestBodySpec;

  @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;

  @Mock private WebClient.ResponseSpec responseSpec;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    webClientService = new WebClientService(webClient);
  }

  @Test
  public void testPostEndpoint_Success() {
    String path = "/test";
    Object requestData = new Object();
    Class<String> responseType = String.class;
    String expectedResponse = "Success";

    // Mock the WebClient behavior
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(path)).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(requestData)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(responseType)).thenReturn(Mono.just(expectedResponse));

    // Call the method
    Object actualResponse = webClientService.postEndpoint(path, requestData, responseType);

    // Verify the response
    assertEquals(expectedResponse, actualResponse);

    // Verify interactions
    verify(webClient).post();
    verify(requestBodyUriSpec).uri(path);
    verify(requestBodySpec).bodyValue(requestData);
    verify(requestHeadersSpec).retrieve();
    verify(responseSpec, atLeast(1)).onStatus(any(), any());
    verify(responseSpec).bodyToMono(responseType);
  }

  @Test
  public void testGetEndpoint_Success() {
    String path = "/test";
    Class<String> responseType = String.class;
    String expectedResponse = "Success";

    // Mock the WebClient behavior
    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(path)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(responseType)).thenReturn(Mono.just(expectedResponse));

    // Call the method
    Object actualResponse = webClientService.getEndpoint(path, responseType);

    // Verify the response
    assertEquals(expectedResponse, actualResponse);

    // Verify interactions
    verify(webClient).get();
    verify(requestHeadersUriSpec).uri(path);
    verify(requestHeadersSpec).retrieve();
    verify(responseSpec, atLeast(1)).onStatus(any(), any());
    verify(responseSpec).bodyToMono(responseType);
  }

  @Test
  public void testPostEndpoint_ClientError() {
    String path = "/test";
    Object requestData = new Object();
    Class<String> responseType = String.class;

    ClientErrorResponse errorResponse = new ClientErrorResponse();
    errorResponse.setMessage("Client error occurred");

    // Create the exception that should be thrown
    ClientErrorException expectedException =
        new ClientErrorException(
            errorResponse, errorResponse.getMessage(), HttpStatus.BAD_REQUEST.value());

    // Mock the WebClient behavior
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(path)).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(requestData)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

    // Ensure onStatus() returns responseSpec to allow method chaining
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

    // Simulate an error when bodyToMono() is called
    when(responseSpec.bodyToMono(responseType)).thenReturn(Mono.error(expectedException));

    // Call the method and expect an exception
    ClientErrorException exception =
        assertThrows(
            ClientErrorException.class,
            () -> {
              webClientService.postEndpoint(path, requestData, responseType);
            });

    // Assertions
    assertEquals("Client error occurred", exception.getMessage());
    assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getCode());
    assertEquals(errorResponse, exception.getResponse());

    // Verify interactions
    verify(webClient).post();
    verify(requestBodyUriSpec).uri(path);
    verify(requestBodySpec).bodyValue(requestData);
    verify(requestHeadersSpec).retrieve();
    verify(responseSpec, atLeast(1)).onStatus(any(), any());
    verify(responseSpec).bodyToMono(responseType);
  }

  @Test
  public void testGetEndpoint_ServerError() {
    String path = "/test";
    Class<String> responseType = String.class;

    String errorBody = "Internal server error";
    ServerErrorException expectedException =
        new ServerErrorException(errorBody, HttpStatus.INTERNAL_SERVER_ERROR.value());

    // Mock the WebClient behavior
    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(path)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

    // Ensure onStatus() returns responseSpec to allow method chaining
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

    // Simulate an error when bodyToMono() is called
    when(responseSpec.bodyToMono(responseType)).thenReturn(Mono.error(expectedException));

    // Call the method and expect an exception
    ServerErrorException exception =
        assertThrows(
            ServerErrorException.class,
            () -> {
              webClientService.getEndpoint(path, responseType);
            });

    // Assertions
    assertEquals(errorBody, exception.getMessage());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getCode());

    // Verify interactions
    verify(webClient).get();
    verify(requestHeadersUriSpec).uri(path);
    verify(requestHeadersSpec).retrieve();
    verify(responseSpec, atLeast(1)).onStatus(any(), any());
    verify(responseSpec).bodyToMono(responseType);
  }

  @Test
  public void testFormatPath() throws Exception {
    // Using reflection to access private static method
    Method formatPathMethod = WebClientService.class.getDeclaredMethod("formatPath", String.class);
    formatPathMethod.setAccessible(true);

    String path1 = "/test";
    String path2 = "test";
    String expectedPath = "/test";

    String result1 = (String) formatPathMethod.invoke(null, path1);
    String result2 = (String) formatPathMethod.invoke(null, path2);

    assertEquals(expectedPath, result1);
    assertEquals(expectedPath, result2);
  }

  @Test
  public void testHandleResponse_Success() {
    Class<String> responseType = String.class;
    String expectedResponse = "Success";

    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(responseType)).thenReturn(Mono.just(expectedResponse));

    Object actualResponse = webClientService.handleResponse(responseSpec, responseType);

    assertEquals(expectedResponse, actualResponse);

    verify(responseSpec, atLeast(1)).onStatus(any(), any());
    verify(responseSpec).bodyToMono(responseType);
  }

  @Test
  public void testHandleResponse_ClientError() {
    Class<String> responseType = String.class;
    ClientErrorResponse errorResponse = new ClientErrorResponse();
    errorResponse.setMessage("Client error occurred");
    int statusCode = HttpStatus.BAD_REQUEST.value();

    // Create the expected exception
    ClientErrorException expectedException =
        new ClientErrorException(errorResponse, errorResponse.getMessage(), statusCode);

    // Mock the WebClient.ResponseSpec behavior
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(responseType)).thenReturn(Mono.error(expectedException));

    // Call the method and expect an exception
    ClientErrorException exception =
        assertThrows(
            ClientErrorException.class,
            () -> {
              webClientService.handleResponse(responseSpec, responseType);
            });

    // Assertions
    assertEquals("Client error occurred", exception.getMessage());
    assertEquals(statusCode, exception.getCode()); // Adjusted method name if necessary
    assertEquals(errorResponse, exception.getResponse()); // Adjusted method name if necessary

    // Verify interactions
    verify(responseSpec, atLeast(1)).onStatus(any(), any());
    verify(responseSpec).bodyToMono(responseType);
  }

  @Test
  public void testHandleResponse_ServerError() {
    Class<String> responseType = String.class;
    String errorBody = "Internal server error";
    int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();

    // Create the expected exception
    ServerErrorException expectedException = new ServerErrorException(errorBody, statusCode);

    // Mock the WebClient.ResponseSpec behavior
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(responseType)).thenReturn(Mono.error(expectedException));

    // Call the method and expect an exception
    ServerErrorException exception =
        assertThrows(
            ServerErrorException.class,
            () -> {
              webClientService.handleResponse(responseSpec, responseType);
            });

    // Assertions
    assertEquals(errorBody, exception.getMessage());
    assertEquals(statusCode, exception.getCode()); // Adjusted method name if necessary

    // Verify interactions
    verify(responseSpec, atLeast(1)).onStatus(any(), any());
    verify(responseSpec).bodyToMono(responseType);
  }
}
