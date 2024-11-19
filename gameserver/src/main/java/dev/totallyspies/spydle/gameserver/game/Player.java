package dev.totallyspies.spydle.gameserver.game;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Player {
    private UUID id;
    private String name;
    private int score;
}
