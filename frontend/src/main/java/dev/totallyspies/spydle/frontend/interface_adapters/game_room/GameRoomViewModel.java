package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import dev.totallyspies.spydle.shared.proto.messages.Player;
import jakarta.annotation.Nullable;
import lombok.Data;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

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
    private String stringCurrentPlayer = ""; // string entered by the player whose turn it currently is

    private int gameTimerSeconds = 0; // Time left
    private int turnTimerSeconds = 0;

    private String currentSubstring = ""; // Current substring

    private String roomCode = "";

    public void reset() {
        this.playerList = new LinkedList<>();
        this.currentTurnPlayer = null;
        this.localPlayer = null;
        this.stringEntered = "";
        this.gameTimerSeconds = 0;
        this.turnTimerSeconds = 0;
        this.currentSubstring = "";
        this.roomCode = "";
    }

}
