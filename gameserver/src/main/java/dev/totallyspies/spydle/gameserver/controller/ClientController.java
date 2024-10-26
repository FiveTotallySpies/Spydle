package dev.totallyspies.spydle.gameserver.controller;

import dev.totallyspies.spydle.gameserver.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private SessionService sessionService;

    @PostMapping("/leave")
    public String clientLeave(@RequestParam String clientId) {
        // Terminate the Netty connection and remove from Redis
        sessionService.removeClientSession(clientId);
        // TODO: Implement logic to close the Netty channel associated with the client
        return "Client has left the game.";
    }
}