package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import dev.totallyspies.spydle.shared.proto.messages.Player;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class GameRoomViewModel {

    private List<Player> playerList = new LinkedList<>(); // List of all players in order

    private Player currentTurnPlayer; // Player whose turn it is
    private Player localPlayer; // This player

    private String stringEntered = "..."; // String in the middle

    private int timerSeconds = 0; // Time left

    private String currentSubstring; // Current substring

}
