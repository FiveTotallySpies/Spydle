package dev.totallyspies.spydle.frontend.test;

import dev.totallyspies.spydle.frontend.client.ClientSocketConfig;
import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.frontend.client.message.CbMessageListener;
import dev.totallyspies.spydle.shared.proto.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.CloseStatus;

import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Semaphore;

//@Component
public class TestClient {
    @Autowired
    private ClientSocketConfig config1;

    @Autowired
    private ClientSocketConfig config2;

    private TestPlayer player1;
    private TestPlayer player2;

    private String ip;
    private int port;

    public void initPlayers() {
        this.player1 = new TestPlayer("player1",
                UUID.fromString("04f01a02-11b7-4c64-ab25-2f02c6cab409"),
                config1.createClient());

        this.player2 = new TestPlayer("player2",
                UUID.fromString("f754601e-47f3-4b15-b9fd-3517a232dd31"),
                config2.createClient());
    }

    // Executed on application startup, but could be instead on the press of a button?
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        setIpPort();

        initPlayers();

        this.player1.open(ip, port);
        this.player2.open(ip, port);

        testStartNewGame(player1);
        testGuess(player2, "BBBBBB");
        testGuess(player1, "CCCCCC");

        waitForever();
        // Close the websocket
        //handler.close();
    }

    public void testStartNewGame(TestPlayer player) {
        var message = SbMessage.newBuilder().setStartGame(SbStartGame.newBuilder()).build();

        player.send(message);
    }

    public void testGuess(TestPlayer player, String guess) {
        var message = SbMessage.newBuilder().setGuess(SbGuess.newBuilder().setGuessedWord(guess)).build();
        player.send(message);
    }

    @CbMessageListener
    public void gameStartListener(CbGameStart message, UUID clientId) {
        TestPlayer player = determinePlayerByUUID(clientId);
        System.out.println("Got a message CbGameStart for " + player.getName() + " message: " + message);
    }

    @CbMessageListener
    public void newTurnListener(CbNewTurn message, UUID clientId) {
        TestPlayer player = determinePlayerByUUID(clientId);
        System.out.println("Got a message CbNewTurn for " + player.getName() + " message: " + message);
    }

    // Custom event fired after socket close
    @EventListener(ClientSocketHandler.CloseEvent.class)
    public void onSocketClose(ClientSocketHandler.CloseEvent event) {
        UUID clientId = event.getClientId();
        CloseStatus reason = event.getStatus();
        // Close window, do other logic?
    }

    private TestPlayer determinePlayerByUUID(UUID id) {
        if (id.equals(player1.getUuid())) {
            return player1;
        }

        if (id.equals(player2.getUuid())) {
            return player2;
        }

        throw new RuntimeException("Couldn't determine player by uuid: " + id.toString());
    }

    private void setIpPort() {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter IP:");
        this.ip = in.next();
        System.out.println("Enter PORT:");
        this.port = in.nextInt();
    }

    private void waitForever() {
        try {
            new Semaphore(0).acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}