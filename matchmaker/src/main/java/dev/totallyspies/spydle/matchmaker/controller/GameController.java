package dev.totallyspies.spydle.matchmaker.controller;

import dev.totallyspies.spydle.matchmaker.generated.model.*;
import dev.totallyspies.spydle.matchmaker.service.MatchmakingService;
import dev.totallyspies.spydle.matchmaker.redis.SessionRepository;
import dev.totallyspies.spydle.shared.model.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    private SessionRepository sessionRepository;

    @PostMapping("/create-game")
    public ResponseEntity<?> createGame(@RequestBody CreateGameRequestModel request) {
        UUID clientId = sessionRepository.parseClientId(request.getClientId());
        logger.info("Received request: /create-game, clientId: {}", request.getClientId());
        if (clientId == null) {
            logger.info("Request for /create-game has bad clientId: {}", request.getClientId());
            return ResponseEntity.status(400).body("Bad clientId: should be UUID");
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
        logger.info("Received request: /join-game, clientId: {}, gameServerName: {}", request.getClientId(), request.getGameServerName());
        if (clientId == null) {
            logger.info("Request for /join-game has bad clientId: {}", request.getClientId());
            return ResponseEntity.status(400).body("Bad clientId: should be UUID");
        }
        try {
            GameServer gameServer = matchmakingService.joinGame(clientId, request.getGameServerName());
            logger.info("Successfully handled /join-game request: {}", gameServer);
            return ResponseEntity.ok(new JoinGameResponseModel().gameServer(gameServer));
        } catch (Exception exception) {
            logger.error("Failed to handle /join-game", exception);
            return ResponseEntity.status(500).body(exception.getMessage());
        }
    }

    @PostMapping("/leave-game")
    public ResponseEntity<?> leaveGame(@RequestBody LeaveGameRequestModel request) {
        UUID clientId = sessionRepository.parseClientId(request.getClientId());
        logger.info("Received request: /join-game, clientId: {}", request.getClientId());
        if (clientId == null) {
            logger.info("Request for /leave-game has bad clientId: {}", request.getClientId());
            return ResponseEntity.status(400).body("Bad clientId: should be UUID");
        }
        try {
            matchmakingService.leaveGame(clientId);
            logger.info("Successfully handled /leave-game request");
            return ResponseEntity.ok(new LeaveGameResponseModel().success(true));
        } catch (Exception exception) {
            logger.error("Failed to handle /leave-game", exception);
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

}