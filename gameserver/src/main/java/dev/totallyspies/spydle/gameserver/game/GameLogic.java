package dev.totallyspies.spydle.gameserver.game;

import dev.totallyspies.spydle.shared.model.ClientSession;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class GameLogic {
    // List of Players is set once when the game starts and doesn't change for the whole game.
    // Before the game starts the list of players is empty.
    private final AtomicReference<List<Player>> players;
    private final Map<UUID, Player> playerMap;

    private final AtomicInteger turn;
    private final AtomicReference<String> currentSubString;
    public static final int TOTAL_GAME_TIME_SECONDS = 30;

    public GameLogic() {
        this.players = new AtomicReference<>(new ArrayList<>());
        this.currentSubString = new AtomicReference<>("");
        this.turn = new AtomicInteger(0);
        this.playerMap = new ConcurrentHashMap<>();
    }

    public void gameStart(Collection<ClientSession> sessions) {
        List<Player> players = new ArrayList<>();
        for (ClientSession session : sessions) {
            var player = new Player(session.getClientId(), session.getPlayerName(), 0);
            players.add(player);
        }

        this.players.set(players);
        for (Player player : players) {
            this.playerMap.put(player.getId(), player);
        }
    }

    public void newTurn() {
        this.turn.incrementAndGet();
        String c = Character.toString((char) (turn.get() % 26 + 'A'));
        this.currentSubString.set(c.repeat(3));
    }

    public boolean isPlayerTurn(UUID playerId) {
        if (!this.playerMap.containsKey(playerId)) {
            return false;
        }

        return getCurrentPlayer().equals(this.playerMap.get(playerId));
    }

    /* Validates a guess from a user and makes a new turn if it is correct. */
    public boolean guess(String input) {
        if (input.contains(currentSubString.get())) {
            addScore(input);
            newTurn();
            return true;
        }
        return false;
    }

    public void addScore(String input) {
        var newPoints = input.length();
        var playerIndex = getCurrentPlayerIndex();

        /* To avoid deadlock, players and playerMap must be locked in this order everywhere */
        synchronized (this.players) {
            synchronized (this.playerMap) {
                var players = this.players.get();
                var player = players.get(playerIndex);
                player.setScore(player.getScore() + newPoints);
                players.set(playerIndex, player);

                this.playerMap.replace(player.getId(), player);
                this.players.set(players);
            }
        }
    }

    public Player getWinner() {
        var players = this.players.get();

        var maxPlayer = players.get(0);
        for (var player : players) {
            if (player.getScore() > maxPlayer.getScore()) {
                maxPlayer = player;
            }
        }

        return maxPlayer;
    }

    public String getCurrentSubString() {
        return this.currentSubString.get();
    }

    private int getCurrentPlayerIndex() {
        return this.turn.get() % this.getPlayers().size(); // TODO: check for players that lost
    }

    public Player getCurrentPlayer() {
        return this.getPlayers().get(getCurrentPlayerIndex());
    }

    public List<Player> getPlayers() {
        return this.players.get();
    }
}