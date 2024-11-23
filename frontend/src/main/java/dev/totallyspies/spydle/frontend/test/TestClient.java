package dev.totallyspies.spydle.frontend.test;

import dev.totallyspies.spydle.frontend.client.ClientSocketConfig;
import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.shared.proto.messages.SbGuess;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbStartGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(TestClient.class);

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
        logger.debug("player1 socket open");
        waitMs(300);
        this.player2.open(ip, port);

        logger.debug("player sockets open");
        waitMs(300);
        testStartNewGame(player1, 5);
        waitMs(300);
        testGuess(player2, "AAAAAA"); // right guess
        waitMs(300);
        testGuess(player1, "AAAAAA"); // wrong guess
        waitMs(300);
        testGuess(player1, "BBBBBBBBBB");

        /*
        Expected logs (shortened):
        */
        /*
        Established connection to websocket
        player1 socket open
        Fired message with PayloadCase UPDATE_PLAYER_LIST with client 1....1, message: update_player_list {  players {    player_name: "player1"  }}
        Established connection to websocket
        player sockets open
        Fired message with PayloadCase UPDATE_PLAYER_LIST with client 1....1, message: update_player_list {  players {    player_name: "player1"  }  players {    player_name: "player2"  }}
        Fired message with PayloadCase UPDATE_PLAYER_LIST with client 2....2, message: update_player_list {  players {    player_name: "player1"  }  players {    player_name: "player2"  }}
        Sending server message start_game{total_game_time_seconds:5}
        Fired message with PayloadCase GAME_START with client 1....1, message: game_start {  players {    player_name: "player1"  }  players {    player_name: "player2"  }  total_game_time_seconds: 5}
        Fired message with PayloadCase GAME_START with client 2....2, message: game_start {  players {    player_name: "player1"  }  players {    player_name: "player2"  }  total_game_time_seconds: 5}
        Fired message with PayloadCase NEW_TURN with client 1....1, message: new_turn {  assigned_string: "AAA"  current_player_name: "player2"}
        Fired message with PayloadCase NEW_TURN with client 2....2, message: new_turn {  assigned_string: "AAA"  current_player_name: "player2"}
        Sending server message guess{guessed_word:"AAAAAA"}
        Fired message with PayloadCase GUESS_RESULT with client 2....2, message: guess_result {  player_name: "player2"  guess: "AAAAAA"  correct: true}
        Fired message with PayloadCase GUESS_RESULT with client 1....1, message: guess_result {  player_name: "player2"  guess: "AAAAAA"  correct: true}
        Fired message with PayloadCase NEW_TURN with client 1....1, message: new_turn {  assigned_string: "BBB"  current_player_name: "player1"}
        Fired message with PayloadCase NEW_TURN with client 2....2, message: new_turn {  assigned_string: "BBB"  current_player_name: "player1"}
        Fired message with PayloadCase UPDATE_PLAYER_LIST with client 1....1, message: update_player_list {  players {    player_name: "player1"  }  players {    player_name: "player2"    score: 6  }}
        Fired message with PayloadCase UPDATE_PLAYER_LIST with client 2....2, message: update_player_list {  players {    player_name: "player1"  }  players {    player_name: "player2"    score: 6  }}
        Sending server message guess{guessed_word:"AAAAAA"}
        Fired message with PayloadCase GUESS_RESULT with client 1....1, message: guess_result {  player_name: "player1"  guess: "AAAAAA"}
        Fired message with PayloadCase GUESS_RESULT with client 2....2, message: guess_result {  player_name: "player1"  guess: "AAAAAA"}
        Sending server message guess{guessed_word:"BBBBBBBBBB"}
        Fired message with PayloadCase GUESS_RESULT with client 2....2, message: guess_result {  player_name: "player1"  guess: "BBBBBBBBBB"  correct: true}
        Fired message with PayloadCase GUESS_RESULT with client 1....1, message: guess_result {  player_name: "player1"  guess: "BBBBBBBBBB"  correct: true}
        Fired message with PayloadCase NEW_TURN with client 2....2, message: new_turn {  assigned_string: "CCC"  current_player_name: "player2"}
        Fired message with PayloadCase NEW_TURN with client 1....1, message: new_turn {  assigned_string: "CCC"  current_player_name: "player2"}
        Fired message with PayloadCase UPDATE_PLAYER_LIST with client 1....1, message: update_player_list {  players {    player_name: "player1"    score: 10  }  players {    player_name: "player2"    score: 6  }}
        Fired message with PayloadCase UPDATE_PLAYER_LIST with client 2....2, message: update_player_list {  players {    player_name: "player1"    score: 10  }  players {    player_name: "player2"    score: 6  }}
        Fired message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  time_left_seconds: 4}
        Fired message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  time_left_seconds: 4}
        Fired message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  time_left_seconds: 3}
        Fired message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  time_left_seconds: 3}
        Fired message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  time_left_seconds: 2}
        Fired message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  time_left_seconds: 2}
        Fired message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  time_left_seconds: 1}
        Fired message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  time_left_seconds: 1}
        Fired message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {}
        Fired message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {}
        Fired message with PayloadCase GAME_END with client 2....2, message: game_end {  players {    player_name: "player1"    score: 10  }  players {    player_name: "player2"    score: 6  }}
        Fired message with PayloadCase GAME_END with client 1....1, message: game_end {  players {    player_name: "player1"    score: 10  }  players {    player_name: "player2"    score: 6  }}
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