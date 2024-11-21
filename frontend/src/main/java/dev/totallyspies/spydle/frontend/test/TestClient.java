package dev.totallyspies.spydle.frontend.test;

import dev.totallyspies.spydle.frontend.client.ClientSocketConfig;
import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.shared.proto.messages.SbGuess;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbStartGame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;

import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Semaphore;

@Component
@Profile("local")
public class TestClient {
    
    @Autowired
    private ClientSocketConfig config;

    private TestPlayer player1;
    private TestPlayer player2;

    private String ip;
    private int port;

    public void initPlayers() {
        this.player1 = new TestPlayer("player1",
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                config.createClient());

        this.player2 = new TestPlayer("player2",
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                config.createClient());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        setIpPort();

        initPlayers();

        this.player1.open(ip, port);
        this.player2.open(ip, port);

        testStartNewGame(player1, 5);
        testGuess(player2, "BBBBBB"); // right guess
        waitMs(500);
        testGuess(player1, "BBBBBB"); // wrong guess
        waitMs(500);
        testGuess(player1, "CCCCCCCCCC");

        /*
        Expected logs (shortened):
        */
        /*
        Sending server message start_game{total_game_time_seconds:5}
        Fired message with PayloadCase GAME_START to 1, message: game_start {  players {    player_name: "player1"  }  players {    player_name: "player2"  }  total_game_time_seconds: 5}
        Fired message with PayloadCase GAME_START to 2, message: game_start {  players {    player_name: "player1"  }  players {    player_name: "player2"  }  total_game_time_seconds: 5}
        Sending server message guess{guessed_word:"BBBBBB"} // Correct guess, + 6 points for player2
        Fired message with PayloadCase NEW_TURN to 2, message: new_turn {  assigned_string: "BBB"  current_player_name: "player2"}
        Fired message with PayloadCase NEW_TURN to 1, message: new_turn {  assigned_string: "BBB"  current_player_name: "player2"}
        Fired message with PayloadCase GUESS_RESULT to 1, message: guess_result {  guess: "BBBBBB"  correct: true}
        Fired message with PayloadCase GUESS_RESULT to 2, message: guess_result {  guess: "BBBBBB"  correct: true}
        Fired message with PayloadCase NEW_TURN to 1, message: new_turn {  assigned_string: "CCC"  current_player_name: "player1"}
        Fired message with PayloadCase NEW_TURN to 2, message: new_turn {  assigned_string: "CCC"  current_player_name: "player1"}
        Sending server message guess{guessed_word:"BBBBBB"}
        Fired message with PayloadCase GUESS_RESULT to 1, message: guess_result {  guess: "BBBBBB"}  //the value of 'correct' is false, not showing up
        Fired message with PayloadCase GUESS_RESULT to 2, message: guess_result {  guess: "BBBBBB"}
        Fired message with PayloadCase TIMER_TICK to 2, message: timer_tick {  time_left_seconds: 4}
        Fired message with PayloadCase TIMER_TICK to 1, message: timer_tick {  time_left_seconds: 4}
        Sending server message guess{guessed_word:"CCCCCCCCCC"} // Correct guess, +10 players to player1
        Fired message with PayloadCase GUESS_RESULT to 1, message: guess_result {  guess: "CCCCCCCCCC"  correct: true} // player1 gets 10 points
        Fired message with PayloadCase GUESS_RESULT to 2, message: guess_result {  guess: "CCCCCCCCCC"  correct: true}
        Fired message with PayloadCase NEW_TURN to 1, message: new_turn {  assigned_string: "DDD"  current_player_name: "player2"}
        Fired message with PayloadCase NEW_TURN to 2, message: new_turn {  assigned_string: "DDD"  current_player_name: "player2"}
        Fired message with PayloadCase TIMER_TICK to 1, message: timer_tick {  time_left_seconds: 3}
        Fired message with PayloadCase TIMER_TICK to 2, message: timer_tick {  time_left_seconds: 3}
        Fired message with PayloadCase TIMER_TICK to 1, message: timer_tick {  time_left_seconds: 2}
        Fired message with PayloadCase TIMER_TICK to 2, message: timer_tick {  time_left_seconds: 2}
        Fired message with PayloadCase TIMER_TICK to 1, message: timer_tick {  time_left_seconds: 1}
        Fired message with PayloadCase TIMER_TICK to 2, message: timer_tick {  time_left_seconds: 1}
        Fired message with PayloadCase GAME_END to 1, message: game_end {  winner {    player_name: "player1"    score: 10  }}
        Fired message with PayloadCase GAME_END to 2, message: game_end {  winner {    player_name: "player1"    score: 10  }}
        */

        waitForever();
        // Close the websocket
        //handler.close();
    }

    public void testStartNewGame(TestPlayer player, int timeSeconds) {
        var message = SbMessage.newBuilder().setStartGame(
                SbStartGame.newBuilder().setTotalGameTimeSeconds(timeSeconds)
        ).build();

        player.send(message);
    }

    public void testGuess(TestPlayer player, String guess) {
        var message = SbMessage.newBuilder().setGuess(SbGuess.newBuilder().setGuessedWord(guess)).build();
        player.send(message);
    }

    // Custom event fired after socket close
    @EventListener(ClientSocketHandler.CloseEvent.class)
    public void onSocketClose(ClientSocketHandler.CloseEvent event) {
        UUID clientId = event.getClientId();
        CloseStatus reason = event.getStatus();
        // Close window, do other logic?
    }

    private void setIpPort() {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter IP:");
        this.ip = in.next();
        System.out.println("Enter PORT:");
        this.port = in.nextInt();
    }

    private void waitMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void waitForever() {
        try {
            new Semaphore(0).acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}