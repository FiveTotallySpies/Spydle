package dev.totallyspies.spydle.matchmaker.controller;

import dev.totallyspies.spydle.matchmaker.generated.model.ClientErrorResponse;
import dev.totallyspies.spydle.matchmaker.generated.model.CreateGameRequestModel;
import dev.totallyspies.spydle.matchmaker.generated.model.CreateGameResponseModel;
import dev.totallyspies.spydle.matchmaker.generated.model.JoinGameRequestModel;
import dev.totallyspies.spydle.matchmaker.generated.model.JoinGameResponseModel;
import dev.totallyspies.spydle.matchmaker.generated.model.ListGamesResponseModel;
import dev.totallyspies.spydle.matchmaker.redis.GameServerRepository;
import dev.totallyspies.spydle.matchmaker.redis.SessionRepository;
import dev.totallyspies.spydle.matchmaker.service.MatchmakingService;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Responsible for delegating requests to client-facing endpoints.
 * create-game: Allocate a game server to the user and assign them a session for it
 * join-game: Assign a session for a user to an already allocated existing game server
 * leave-game: Delete a user session from a game server and notify it of client departure
 */
@RestController
public class GameController {

    private final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private MatchmakingService matchmakingService;

    @Autowired
    private GameServerRepository gameServerRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @PostMapping("/create-game")
    public ResponseEntity<?> createGame(@RequestBody CreateGameRequestModel request) {
        UUID clientId = sessionRepository.parseClientId(request.getClientId());
        logger.info("Received request: /create-game, clientId: {}", request.getClientId());
        if (clientId == null) {
            logger.info("Request for /create-game has bad clientId: {}", request.getClientId());
            return ResponseEntity.status(400).body(new ClientErrorResponse().message("Bad clientId: should be UUID"));
        }
        try {
            GameServer gameServer = matchmakingService.createGame(clientId);
            logger.info("Successfully handled /create-game request: {}", gameServer);
            return ResponseEntity.ok(new CreateGameResponseModel().gameServer(gameServer));
        } catch (Exception exception) {
            logger.error("Failed to handle /create-game", exception);
            return ResponseEntity.status(500).body(exception.getMessage());
        }
    }

    @PostMapping("/join-game")
    public ResponseEntity<?> joinGame(@RequestBody JoinGameRequestModel request) {
        UUID clientId = sessionRepository.parseClientId(request.getClientId());
        logger.info("Received request: /join-game, clientId: {}, gameServerName: {}", request.getClientId(), request.getRoomCode());
        if (clientId == null) {
            logger.info("Request for /join-game has bad clientId: {}", request.getClientId());
            return ResponseEntity.status(400).body(new ClientErrorResponse().message("Bad clientId: should be UUID"));
        }
        try {
            if (!gameServerRepository.gameServerExists(request.getRoomCode())) {
                logger.info("Request for /join-game requests room that doesn't exist: {}", request.getRoomCode());
                return ResponseEntity.status(404).body(
                        new ClientErrorResponse().message("Could not find game server: " + request.getRoomCode()));
            }
            if (sessionRepository.sessionExists(clientId)) {
                logger.info("Request for /join-game is try to join room {} when they are already in {}",
                        request.getRoomCode(),
                        sessionRepository.getSession(clientId).getGameServer().getRoomCode());
                return ResponseEntity.status(403).body(
                        new ClientErrorResponse().message("Cannot join room: you are already in one"));
            }
            GameServer gameServer = gameServerRepository.getGameServer(request.getRoomCode());
            matchmakingService.joinGame(clientId, gameServer);
            logger.info("Successfully handled /join-game request: {}", gameServer);
            return ResponseEntity.ok(new JoinGameResponseModel().gameServer(gameServer));
        } catch (Exception exception) {
            logger.error("Failed to handle /join-game", exception);
            return ResponseEntity.status(500).body(exception.getMessage());
        }
    }

    @GetMapping("/list-games")
    public ResponseEntity<?> listGames() {
        logger.info("Received request: /list-games");
        try {
            List<String> games = matchmakingService.listGames();
            logger.info("Successfully handled /list-games request: {}", games);
            return ResponseEntity.ok(new ListGamesResponseModel().roomCodes(games));
        } catch (Exception exception) {
            logger.error("Failed to handle /list-games", exception);
            return ResponseEntity.status(500).body(exception.getMessage());
        }
    }

}