package dev.totallyspies.spydle.gameserver.game;

import dev.totallyspies.spydle.shared.proto.messages.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GameLogic {
    private final Map<UUID, Player> joinedPlayers;
    private final AtomicBoolean gameInProgress;
    private final Timer timer;

    private static final int TIMER_INTERVAL_MILLIS = 1000;
    private static final AtomicInteger TOTAL_GAME_TIME_SECONDS = new AtomicInteger(30);

    public GameLogic() {
        this.joinedPlayers = new ConcurrentHashMap<UUID, Player>();
        this.gameInProgress = new AtomicBoolean(false);
        this.timer = new Timer();
    }

    public void playerLeft(UUID clientId) {
        this.joinedPlayers.remove(clientId);
    }

    public void onPlayerNameSelect(SbSelectName event, UUID client) {
        /* Player name can't be changed when the game is in progress. */
        if (this.gameInProgress.get()) {
            return;
        }

        String playerName = event.getPlayerName(); // TODO: what if playerName is empty

        var player = Player.newBuilder().setPlayerName(playerName).setScore(0).build();
        this.joinedPlayers.put(client, player);
    }

    public boolean isGameInProgress() {
        return this.gameInProgress.get();
    }

    public CbGameStart onGameStart(UUID client) {
        // TODO: check whether client can start the game
        // TODO: check for the number of players before the start
        this.gameInProgress.set(true);

        return CbGameStart
                .newBuilder()
                .setTotalGameTimeSeconds(100) // TODO: change this in the future
                .addAllPlayers(joinedPlayers.values())
                .build();
    }
}