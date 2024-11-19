package dev.totallyspies.spydle.frontend.use_cases.join_game;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JoinGameInputData {

    private String playerName;
    private String roomCode;

}
