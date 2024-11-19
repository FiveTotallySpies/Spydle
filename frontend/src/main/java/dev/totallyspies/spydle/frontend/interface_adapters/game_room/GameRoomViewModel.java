package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@Data
public class GameRoomViewModel {
    private ArrayList<String> playerList;

    public void setPlayerList(ArrayList<String> playerList) {
        this.playerList = playerList;
    }
}
