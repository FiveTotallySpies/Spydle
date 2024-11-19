package dev.totallyspies.spydle.frontend.use_cases.create_game;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public final class CreateGameOutputDataSuccess implements CreateGameOutputData {

    private String gameHost;
    private int gamePort;
    private UUID clientId;
    private String playerName;

}
