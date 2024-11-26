package dev.totallyspies.spydle.frontend.views.game_room_panels;

import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewModel;
import dev.totallyspies.spydle.shared.proto.messages.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GamePanel extends JPanel {

    private final Logger logger = LoggerFactory.getLogger(GamePanel.class);

    private final GameRoomViewModel model;

    private final Map<String, PlayerPanel> playerPanels = new LinkedHashMap<>();
    private final JLabel substringLabel;
    private final JLabel timerLabel;
    private final JLabel timerPlayer;

    public GamePanel(GameRoomViewModel model) {
        this.model = model;
        setLayout(null);
        setBackground(new Color(195, 217, 255));

        // Center substring label
        substringLabel = new JLabel("SUBSTRING", SwingConstants.CENTER);
        substringLabel.setFont(new Font("Arial", Font.BOLD, 18));
        substringLabel.setBounds(300, 200, 200, 50);
        add(substringLabel);

        timerPlayer = new JLabel("", SwingConstants.CENTER);
        timerPlayer.setFont(new Font("Arial", Font.PLAIN, 16));
        timerPlayer.setBounds(300, 250, 200, 50);
        add(timerPlayer);

        // Timer label
        timerLabel = new JLabel("Timer: 0:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        timerLabel.setBounds(650, 10, 100, 30);
        add(timerLabel);

        // Create player panels in a circle layout
        createPlayerPanels();

    }

    // Position players in a circular layout
    private void createPlayerPanels() {
        for (PlayerPanel panel : playerPanels.values()) {
            remove(panel); // Remove panel from the GamePanel
        }
        playerPanels.clear();
        for (Player player : model.getPlayerList()) {
            PlayerPanel playerPanel = new PlayerPanel(player.getPlayerName(), player.getScore(), this);
            playerPanels.put(player.getPlayerName(), playerPanel);
            add(playerPanel);
        }

        int centerX = 400;
        int centerY = 250;
        int radius = 150;

        int width = 100;
        int height = 100;
        int j = 0;
        for (PlayerPanel panel : playerPanels.values()) {
            double angle = 2 * Math.PI * (j++) / playerPanels.size();
            int x = (int) (centerX + radius * Math.cos(angle) - ((double) width / 2));
            int y = (int) (centerY + radius * Math.sin(angle) - ((double) height / 2));
            panel.setLocation(x, y, width, height);
        }
    }

    public void updateGame() {
        // Update list of players
        Set<String> currentPlayerNames = playerPanels.keySet();
        Set<String> modelPlayerNames = model.getPlayerList()
                .stream()
                .map(Player::getPlayerName)
                .collect(Collectors.toSet());
        boolean playersChanged = currentPlayerNames.size() != modelPlayerNames.size()
                || modelPlayerNames.stream().anyMatch(name -> !currentPlayerNames.contains(name));
        if (playersChanged) {
            createPlayerPanels();
        }

        // Update points
        for (Player player : model.getPlayerList()) {
            playerPanels.get(player.getPlayerName()).updateScore(player.getScore());
        }

        // Highlight the current turn player's panel with a red border
        Player currentTurnPlayer = model.getCurrentTurnPlayer();
        if (currentTurnPlayer != null) {
            highlightPlayerPanel(currentTurnPlayer);  // Highlight current player
        }

        substringLabel.setText(model.getCurrentSubstring());
        timerPlayer.setText("Guess in " + model.getTurnTimerSeconds());
        timerLabel.setText("Timer: " + model.getGameTimerSeconds() / 60 + ":" + String.format("%02d", model.getGameTimerSeconds() % 60));

        revalidate();
        repaint();
    }

    // displays the string that the current player is typing
    public void updateStringDisplayed(String playerName, GameRoomViewModel.GuessInProgress guess) {
        playerPanels.get(playerName).setPlayerGuess(guess.getCurrentWord(), guess.isCorrect());
    }

    private void highlightPlayerPanel(Player currentTurnPlayer) {
        // Loop through all player panels and set the border accordingly
        for (Map.Entry<String, PlayerPanel> entry : playerPanels.entrySet()) {
            Player player = model.getPlayerList().stream()
                    .filter(p -> p.getPlayerName().equals(entry.getKey()))
                    .findFirst().orElse(null);
            if (player != null && player.equals(currentTurnPlayer)) {
                entry.getValue().setPlayerBorder(Color.RED, 5);  // Set red border for current player
            } else {
                entry.getValue().setPlayerBorder(Color.GRAY, 2);  // Set default gray border for others
            }
        }
    }


//    public static void main(String[] args) {
//        // Create a frame to test the GamePanel
//        JFrame frame = new JFrame("Game Test");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(800, 600);
//
//        // Create GamePanel with 4 players
//        GamePanel gamePanel = new GamePanel(4);
//
//        // Add the panel to the frame
//        frame.add(gamePanel);
//
//        // Show the frame
//        frame.setVisible(true);
//
//        // Simulate a game loop or user interaction to update the game
//        new Timer(2000, e -> {
//            gamePanel.updateGame("New Substring", 30, (gamePanel.currentPlayerIndex + 1) % 4);
//        }).start();
//    }

}