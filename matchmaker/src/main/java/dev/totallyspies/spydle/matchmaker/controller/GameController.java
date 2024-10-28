package dev.totallyspies.spydle.matchmaker.controller;

import dev.totallyspies.spydle.matchmaker.service.GameServerInfo;
import dev.totallyspies.spydle.matchmaker.service.MatchmakingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/create-game")
    public ResponseEntity<?> createGame(@RequestParam String clientId) {
        logger.info("Received request: /create-game, clientId: {}", clientId);
        try {
            GameServerInfo gameServerInfo = matchmakingService.createGame(clientId);
            logger.info("Successfully handled /create-game request: {}", gameServerInfo);
            return ResponseEntity.ok(gameServerInfo);
        } catch (Exception exception) {
            logger.error("Failed to handle /create-game", exception);
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @PostMapping("/join-game")
    public ResponseEntity<?> joinGame(@RequestParam String clientId, @RequestParam String gameServerName) {
        // TODO how do clients know about gameserver names?
        logger.info("Received request: /join-game, clientId: {}, gameServerName: {}", clientId, gameServerName);
        try {
            GameServerInfo gameServerInfo = matchmakingService.joinGame(clientId, gameServerName);
            logger.info("Successfully handled /join-game request: {}", gameServerInfo);
            return ResponseEntity.ok(gameServerInfo);
        } catch (Exception exception) {
            logger.error("Failed to handle /join-game", exception);
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @PostMapping("/leave-game")
    public ResponseEntity<?> leaveGame(@RequestParam String clientId) {
        logger.info("Received request: /join-game, clientId: {}", clientId);
        try {
            matchmakingService.leaveGame(clientId);
            logger.info("Successfully handled /leave-game request");
            return ResponseEntity.ok("Left the game successfully.");
        } catch (Exception exception) {
            logger.error("Failed to handle /leave-game", exception);
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }
}