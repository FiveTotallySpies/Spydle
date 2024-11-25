package dev.totallyspies.spydle.frontend.views.game_room_panels;

import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewModel;
import dev.totallyspies.spydle.shared.proto.messages.Player;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;
import java.awt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GamePanel extends JPanel {

    private final Logger logger = LoggerFactory.getLogger(GamePanel.class);

    private final GameRoomViewModel model;

    private final Map<String, PlayerPanel> playerPanels = new LinkedHashMap<>();
    private final JLabel substringLabel;
    private final JLabel timerLabel;
    private final ArrowPanel arrowPanel;  // Reference to the arrow panel

    public GamePanel(GameRoomViewModel model) {
        this.model = model;
        setLayout(null);
        setBackground(Color.WHITE);

        // Center substring label
        substringLabel = new JLabel("SUBSTRING", SwingConstants.CENTER);
        substringLabel.setFont(new Font("Arial", Font.BOLD, 18));
        substringLabel.setBounds(300, 250, 200, 50);
        add(substringLabel);

        // Timer label
        timerLabel = new JLabel("Timer: 0:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        timerLabel.setBounds(650, 10, 100, 30);
        add(timerLabel);

        // Create player panels in a circle layout
        createPlayerPanels();

        // Add arrow pointing to the current player
        arrowPanel = new ArrowPanel();
        arrowPanel.setBounds(390, 220, 20, 50);  // Initial position
        add(arrowPanel);
    }

    // Position players in a circular layout
    private void createPlayerPanels() {
        for (PlayerPanel panel : playerPanels.values()) {
            remove(panel); // Remove panel from the GamePanel
        }
        playerPanels.clear();
        for (Player player : model.getPlayerList()) {
            PlayerPanel playerPanel = new PlayerPanel(player.getPlayerName(), player.getScore());
            playerPanels.put(player.getPlayerName(), playerPanel);
            add(playerPanel);
        }

        int centerX = 400;
        int centerY = 250;
        int radius = 150;

        int j = 0;
        for (PlayerPanel panel : playerPanels.values()) {
            double angle = 2 * Math.PI * (j++) / playerPanels.size();
            int x = (int) (centerX + radius * Math.cos(angle) - 50);
            int y = (int) (centerY + radius * Math.sin(angle) - 50);
            panel.setBounds(x, y, 100, 100);
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

        // TODO: make arrow change to model.turnPlayer

        substringLabel.setText(model.getCurrentSubstring());
        timerLabel.setText("Timer: " + model.getTimerSeconds() / 60 + ":" + String.format("%02d", model.getTimerSeconds() % 60));

        if (model.getCurrentTurnPlayer() != null) {
            int index = 0;
            for (Player player : model.getPlayerList()) {
                if (player.getPlayerName().equals(model.getCurrentTurnPlayer().getPlayerName())) {
                    break;
                }
                index++;
            }
            if (index >= model.getPlayerList().size()) {
                logger.error("Could not find player in player list with current turn player name!");
            } else {
                updateArrow(index);
            }
        }

        revalidate();
        repaint();
    }

    // Move the arrow to point to the current player
    private void updateArrow(int playerIndex) {
        int centerX = 400;
        int centerY = 250;
        int radius = 150;

        // Calculate the position of the current player in the circle
        double angle = 2 * Math.PI * playerIndex / playerPanels.size();
        int x = (int) (centerX + radius * Math.cos(angle) - 10);  // Adjust for arrow positioning
        int y = (int) (centerY + radius * Math.sin(angle) - 50);  // Adjust for arrow positioning

        // Set the new position of the arrow
        arrowPanel.setBounds(x, y, 20, 50);  // Adjust the arrow size and position
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
