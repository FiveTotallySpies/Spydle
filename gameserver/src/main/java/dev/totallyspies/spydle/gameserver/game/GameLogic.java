package dev.totallyspies.spydle.gameserver.game;

import dev.totallyspies.spydle.shared.model.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class GameLogic {

    private final Logger logger = LoggerFactory.getLogger(GameLogic.class);

    // List of Players is set once when the game starts and doesn't change for the whole game.
    // Before the game starts the list of players is empty.
    private final AtomicReference<List<Player>> players;
    private final Map<UUID, Player> playerMap;

    private final AtomicInteger turn;
    private final AtomicReference<String> currentSubString;
    private final AtomicInteger turnTimeSeconds = new AtomicInteger(10); // length of 10 seconds by default
    private final AtomicInteger gameTimeSeconds = new AtomicInteger(60); // length of 1 minute by default
    private final AtomicReference<Set<String>> validWords;
    private final AtomicReference<List<String>> substrings;
    private final AtomicLong lastTurnStartMillis = new AtomicLong(0);
    private final AtomicLong gameStartMillis = new AtomicLong(0);
    private final AtomicLong tickTimeMillis = new AtomicLong(System.currentTimeMillis());

    private final Random random;

    private static final int MINIMUM_OCCURRENCES = 1000;

    public GameLogic(@Value("${spydle.random-seed}") int randomSeed) {
        if (randomSeed == -1) {
            random = new Random();
        } else {
            random = new Random(randomSeed);
        }

        this.players = new AtomicReference<>(new ArrayList<>());
        this.currentSubString = new AtomicReference<>("");
        this.turn = new AtomicInteger(0);
        this.playerMap = new ConcurrentHashMap<>();
        this.validWords = new AtomicReference<>(new TreeSet<>());
        this.substrings = new AtomicReference<>(new ArrayList<>());
    }

    /* Takes a lot of memory, meant to be called once */
    public void parseWords() throws IOException {
        if (!this.validWords.get().isEmpty()) {
            return;
        }

        var words = new TreeSet<String>();
        ClassPathResource resource = new ClassPathResource("words.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    words.add(line.trim().toLowerCase());
                }
            }
        } catch (Exception exception) {
            logger.error("Failed to read words.txt", exception);
        }
        this.validWords.set(words);
    }

    /* Takes a lot of memory, meant to be called once */
    public void parseSubstrings() throws IOException {
        if (!this.substrings.get().isEmpty()) {
            return;
        }

        var substrings = new ArrayList<String>();
        ClassPathResource resource = new ClassPathResource("substrings.csv");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            reader.readLine(); // skip the first line
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    line = line.trim().toLowerCase();

                    String[] split = line.split(",");
                    String substr = split[0];
                    int occ = Integer.parseInt(split[1]);
                    if (occ >= MINIMUM_OCCURRENCES) {
                        substrings.add(substr);
                    }
                }
            }
        } catch (Exception exception) {
            logger.error("Failed to read substrings.csv", exception);
        }
        this.substrings.set(substrings);
    }

    /* Assuming sessions are sorted by name of the player, increasing */
    public void gameStart(Collection<ClientSession> sessions, int proposedGameTimeSeconds, int proposedTurnTimeSeconds) {
        this.updateTickTime();

        if (isValidTotalGameTime(proposedGameTimeSeconds)) {
            this.gameTimeSeconds.set(proposedGameTimeSeconds);
        }

        if (isValidTurnTime(proposedTurnTimeSeconds)) {
            this.turnTimeSeconds.set(proposedTurnTimeSeconds);
        }

        List<Player> players = new ArrayList<>();
        for (ClientSession session : sessions) {
            var player = new Player(session.getClientId(), session.getPlayerName(), 0);
            players.add(player);
        }

        this.players.set(players);
        for (Player player : players) {
            this.playerMap.put(player.getId(), player);
        }

        try {
            parseWords();
            parseSubstrings();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.gameStartMillis.set(this.getTickTime());
    }

    public void newTurn() {
        var randomIndex = this.random.nextInt(this.substrings.get().size());
        this.currentSubString.set(this.substrings.get().get(randomIndex));
        this.turn.incrementAndGet();
        this.lastTurnStartMillis.set(this.getTickTime());
    }

    public boolean isPlayerTurn(UUID playerId) {
        if (!this.playerMap.containsKey(playerId)) {
            return false;
        }

        return getCurrentPlayer().equals(this.playerMap.get(playerId));
    }

    /* Validates a guess from a user and makes a new turn if it is correct. */
    public boolean guess(String input) {
        if (input.length() > 50) {
            return false;
        }
        input = input.trim();
        input = input.toLowerCase();
        if (input.contains(currentSubString.get()) && validWords.get().contains(input)) {
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

    /* Meant to be called every timer tick. */
    public void updateTickTime() { tickTimeMillis.set(System.currentTimeMillis()); }

    public long getTickTime() { return this.tickTimeMillis.get(); }

    public long getGameStartMillis() { return this.gameStartMillis.get(); }

    public long getLastTurnStartMillis() { return this.lastTurnStartMillis.get(); }

    public int getTurnTimeSeconds() { return this.turnTimeSeconds.get(); }

    public long getTurnTimeMillis() { return this.turnTimeSeconds.get() * 1000L; }

    public int getTotalGameTimeSeconds() {
        return this.gameTimeSeconds.get();
    }

    public long getTotalGameTimeMillis() {
        return this.gameTimeSeconds.get() * 1000L;
    }

    public boolean isValidTotalGameTime(int seconds) {
        return (seconds >= 5 && seconds <= 1000);
    }

    public boolean isValidTurnTime(int seconds) {
        return (seconds >= 3 && seconds <= 30);
    }

    public String getCurrentSubString() {
        return this.currentSubString.get();
    }

    private int getCurrentPlayerIndex() {
        return this.turn.get() % this.getPlayers().size();
    }

    public Player getCurrentPlayer() {
        return this.getPlayers().get(getCurrentPlayerIndex());
    }

    public List<Player> getPlayers() {
        return this.players.get();
    }

    public List<Player> getPlayersScoreSorted() {
        var players = this.players.get();
        /* sorting by descending order, -score is intentional */
        players.sort(Comparator.comparingInt(player -> -player.getScore()));
        return players;
    }
}