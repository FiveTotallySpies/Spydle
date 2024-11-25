package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import dev.totallyspies.spydle.shared.proto.messages.Player;
import jakarta.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Data
@Profile("!local")
public class GameRoomViewModel {

    private List<Player> playerList = new LinkedList<>(); // List of all players in order

    @Nullable
    private Player currentTurnPlayer; // Player whose turn it is
    @Nullable
    private Player localPlayer; // This player

    private String stringEntered = ""; // String in the middle

    private int timerSeconds = 0; // Time left

    private String currentSubstring = ""; // Current substring

    private String roomCode = "";

    public void reset() {
        this.playerList = new LinkedList<>();
        this.currentTurnPlayer = null;
        this.localPlayer = null;
        this.stringEntered = "";
        this.timerSeconds = 0;
        this.currentSubstring = "";
        this.roomCode = "";
    }

}
