package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import dev.totallyspies.spydle.shared.proto.messages.Player;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
@Profile("!test")
public class GameRoomViewModel {

    private List<Player> playerList = new LinkedList<>(); // List of all players in order

    @Nullable
    private Player currentTurnPlayer; // Player whose turn it is
    @Nullable
    private Player localPlayer; // This player

    private String stringEntered = ""; // String in the middle

    private int gameTimerSeconds = 0; // Time left

    private int turnTimerSeconds = 0;

    private String currentSubstring = ""; // Current substring

    private String roomCode = "";

    // The set of strings that appear above players as they type their guesses
    // Key is player name
    private Map<String, Guess> currentGuesses = new ConcurrentHashMap<>();

    public void reset() {

        this.playerList = new LinkedList<>();
        this.currentTurnPlayer = null;
        this.localPlayer = null;
        this.stringEntered = "";
        this.gameTimerSeconds = 0;
        this.turnTimerSeconds = 0;
        this.currentSubstring = "";
        this.roomCode = "";
        this.currentGuesses.clear();
    }

    @Data
    @AllArgsConstructor
    public static class Guess {

        private String currentWord;
        private Verdict verdict;

        public enum Verdict {
            CORRECT,
            INCORRECT,
            IN_PROGRESS
        }

    }

}
