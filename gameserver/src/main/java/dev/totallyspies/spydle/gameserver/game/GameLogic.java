package dev.totallyspies.spydle.gameserver.game;

import dev.totallyspies.spydle.shared.proto.messages.*;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class GameLogic {
    private final Map<UUID, Player> joinedPlayers; // doesn't change after
    private final AtomicBoolean gameInProgress;
    private final AtomicInteger turn;
    private final AtomicReference<String> currentSubString;

    private static final int TIMER_INTERVAL_MILLIS = 1000;
    private static final AtomicInteger TOTAL_GAME_TIME_SECONDS = new AtomicInteger(30);

    public GameLogic() {
        this.joinedPlayers = new ConcurrentHashMap<UUID, Player>();
        this.gameInProgress = new AtomicBoolean(false);
        this.currentSubString = new AtomicReference<>();
        this.turn = new AtomicInteger(0);
    }

    public void onPlayerJoin(SbJoinGame event, UUID client) {
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

    public CbMessage onGameStart(UUID client)
    {
        // TODO: check whether client can start the game
        this.gameInProgress.set(true);

        return CbMessage
                .newBuilder()
                .setGameStart(
                        CbGameStart.newBuilder()
                        .setTotalGameTimeSeconds(100) // TODO: change this in the future
                        .addAllPlayers(joinedPlayers.values())
                ).build();
    }

    public CbMessage newTurn() {
        this.turn.incrementAndGet();
        String c = Character.toString((char) (turn.get() % 26 + 'A'));
        this.currentSubString.set(c.repeat(3));

        return CbMessage
                .newBuilder()
                .setNewTurn(
                        CbNewTurn.newBuilder()
                        .setAssignedString(this.currentSubString.get())
                        .setCurrentPlayerName("kai")
                )
                .build();
    }

    public String getCurrentSubString() {
        return this.currentSubString.get();
    }
}