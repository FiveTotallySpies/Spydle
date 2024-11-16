package dev.totallyspies.spydle.gameserver.game;

import dev.totallyspies.spydle.shared.model.ClientSession;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class GameLogic {
    // List of Players is set once when the game starts and doesn't change for the whole game.
    // Before the game starts the list of players is empty.
    private final AtomicReference<List<Player>> players;
    private final AtomicInteger turn;
    private final AtomicReference<String> currentSubString;
    public static final int TOTAL_GAME_TIME_SECONDS = 30;

    public GameLogic() {
        this.players = new AtomicReference<>(new ArrayList<>());
        this.currentSubString = new AtomicReference<>("");
        this.turn = new AtomicInteger(0);
    }

    public void gameStart(Collection<ClientSession> sessions) {
        List<Player> players = new ArrayList<>();
        for (ClientSession session : sessions) {
            var player = new Player(session.getClientId(), session.getPlayerName(), 0);
            players.add(player);
        }
        this.players.set(players);
    }

    public void newTurn() {
        this.turn.incrementAndGet();
        String c = Character.toString((char) (turn.get() % 26 + 'A'));
        this.currentSubString.set(c.repeat(3));
    }

    /* Validates a guess from a user and makes a new turn if it is correct. */
    public boolean guess(String input) {
        if (input.contains(currentSubString.get())) {
            newTurn();
            return true;
        }
        return false;
    }

    public String getCurrentSubString() {
        return this.currentSubString.get();
    }

    public Player getCurrentPlayer() {
        int playerIndex = this.turn.get() % this.getPlayers().size(); // TODO: check for players that lost
        return this.getPlayers().get(playerIndex);
    }

    public List<Player> getPlayers() {
        return this.players.get();
    }
}