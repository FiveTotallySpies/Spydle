package dev.totallyspies.spydle.matchmaker.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import dev.totallyspies.spydle.matchmaker.config.GameServerRepository;
import dev.totallyspies.spydle.matchmaker.config.SessionRepository;
import dev.totallyspies.spydle.matchmaker.generated.model.CreateGameRequestModel;
import dev.totallyspies.spydle.matchmaker.generated.model.CreateGameResponseModel;
import dev.totallyspies.spydle.matchmaker.generated.model.JoinGameRequestModel;
import dev.totallyspies.spydle.matchmaker.generated.model.JoinGameResponseModel;
import dev.totallyspies.spydle.matchmaker.generated.model.ListGamesResponseModel;
import dev.totallyspies.spydle.matchmaker.use_case.MatchmakingService;
import dev.totallyspies.spydle.shared.model.GameServer;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

public class MatchmakingControllerTest {

  private final GameServer fakeGameServer =
      new GameServer("", 0, "", "", false, GameServer.State.WAITING);
  private MatchmakingController matchmakingController;
  @Mock private MatchmakingService matchmakingService;
  @Mock private GameServerRepository gameServerRepository;
  @Mock private SessionRepository sessionRepository;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    matchmakingController =
        new MatchmakingController(matchmakingService, gameServerRepository, sessionRepository);
  }

  @Test
  public void testCreateGame_Success() {
    CreateGameRequestModel request = new CreateGameRequestModel();
    UUID clientId = UUID.randomUUID();
    request.setClientId(clientId.toString());
    request.setPlayerName("Player1");

    when(sessionRepository.parseClientId(anyString())).thenReturn(clientId);
    when(matchmakingService.createGame(eq(clientId), eq("Player1"), anyInt()))
        .thenReturn(fakeGameServer);

    ResponseEntity<?> response = matchmakingController.createGame(request);

    assertEquals(200, response.getStatusCode().value());
    CreateGameResponseModel responseBody = (CreateGameResponseModel) response.getBody();
    assertNotNull(responseBody);
    assertEquals(fakeGameServer, responseBody.getGameServer());
  }

  @Test
  public void testCreateGame_BadClientId() {
    CreateGameRequestModel request = new CreateGameRequestModel();
    request.setClientId("invalid-uuid");
    request.setPlayerName("Player1");

    when(sessionRepository.parseClientId(anyString())).thenReturn(null);

    ResponseEntity<?> response = matchmakingController.createGame(request);

    assertEquals(400, response.getStatusCode().value());
  }

  @Test
  public void testJoinGame_Success() {
    JoinGameRequestModel request = new JoinGameRequestModel();
    UUID clientId = UUID.randomUUID();
    request.setClientId(clientId.toString());
    request.setPlayerName("Player1");
    request.setRoomCode("ROOM1");

    when(sessionRepository.parseClientId(anyString())).thenReturn(clientId);
    when(gameServerRepository.gameServerExists("ROOM1")).thenReturn(true);
    when(gameServerRepository.getGameServer("ROOM1")).thenReturn(fakeGameServer);
    when(sessionRepository.sessionExists(clientId)).thenReturn(false);

    ResponseEntity<?> response = matchmakingController.joinGame(request);

    assertEquals(200, response.getStatusCode().value());
    JoinGameResponseModel responseBody = (JoinGameResponseModel) response.getBody();
    assertNotNull(responseBody);
    assertEquals(fakeGameServer, responseBody.getGameServer());
  }

  @Test
  public void testJoinGame_GameNotFound() {
    JoinGameRequestModel request = new JoinGameRequestModel();
    UUID clientId = UUID.randomUUID();
    request.setClientId(clientId.toString());
    request.setPlayerName("Player1");
    request.setRoomCode("ROOM1");

    when(sessionRepository.parseClientId(anyString())).thenReturn(clientId);
    when(gameServerRepository.gameServerExists("ROOM1")).thenReturn(false);

    ResponseEntity<?> response = matchmakingController.joinGame(request);

    assertEquals(404, response.getStatusCode().value());
  }

  @Test
  public void testListGames() {
    List<String> games = List.of("ROOM1", "ROOM2");

    when(matchmakingService.listGames()).thenReturn(games);

    ResponseEntity<?> response = matchmakingController.listGames();

    assertEquals(200, response.getStatusCode().value());
    ListGamesResponseModel responseBody = (ListGamesResponseModel) response.getBody();
    assertNotNull(responseBody);
    assertEquals(games, responseBody.getRoomCodes());
  }
}
