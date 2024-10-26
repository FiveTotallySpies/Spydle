package dev.totallyspies.spydle.matchmaker.controller;

import dev.totallyspies.spydle.matchmaker.service.GameServerInfo;
import dev.totallyspies.spydle.matchmaker.service.MatchmakingService;
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

    @Autowired
    private MatchmakingService matchmakingService;

    @PostMapping("/create-game")
    public ResponseEntity<?> createGame(@RequestParam String clientId) {
        try {
            GameServerInfo gameServerInfo = matchmakingService.createGame(clientId);
            return ResponseEntity.ok(gameServerInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/join-game")
    public ResponseEntity<?> joinGame(@RequestParam String clientId, @RequestParam String gameServerName) {
        // TODO how do clients know about gameserver names?
        try {
            GameServerInfo gameServerInfo = matchmakingService.joinGame(clientId, gameServerName);
            return ResponseEntity.ok(gameServerInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/leave-game")
    public ResponseEntity<?> leaveGame(@RequestParam String clientId) {
        try {
            matchmakingService.leaveGame(clientId);
            return ResponseEntity.ok("Left the game successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}