package dev.totallyspies.spydle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class GameLogic {
    HashSet<String> tokens;
    HashMap<String, HashSet<String>> gamePlayers;

    GameLogic() {
        this.tokens = new HashSet<>();
        this.gamePlayers = new HashMap<>();
    }

    String newToken() {
        String token = UUID.randomUUID().toString();
        tokens.add(token);
        return token;
    }

    String newGame(String player) {
        String gameId = UUID.randomUUID().toString().substring(0,4);

        var players = gamePlayers.getOrDefault(gameId, new HashSet<>());
        players.add(player);
        gamePlayers.put(gameId, players);
        
        return gameId;
    }

    public String validatePlayer(String token) throws Exception {
        if (!tokens.contains(token)) {
            throw new Exception("token is invalid!");
        }

        return token;
    }

    public String validateGame(String gameId) throws Exception {
        if (!gamePlayers.containsKey(gameId)) {
            throw new Exception("gameId is invalid!");
        }

        return gameId;
    }

    public void joinPlayerToGame(String player, String game) {
        var players = gamePlayers.getOrDefault(game, new HashSet<>());
        players.add(player);
        gamePlayers.put(game, players);

        System.out.println("player joined to the game!");
        System.out.println(gamePlayers.getOrDefault(game, new HashSet<>()).toString());
    }
}
