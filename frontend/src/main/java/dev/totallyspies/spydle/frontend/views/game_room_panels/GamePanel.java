package dev.totallyspies.spydle.frontend.views.game_room_panels;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {

    private final List<PlayerPanel> playerPanels;
    private final JLabel substringLabel;
    private final JLabel timerLabel;
    private int currentPlayerIndex = 0;
    private ArrowPanel arrowPanel;  // Reference to the arrow panel

    public GamePanel(int playerCount) {
        setLayout(null);
        setBackground(Color.WHITE);

        // Center substring label
        substringLabel = new JLabel("SUBSTRING", SwingConstants.CENTER);
        substringLabel.setFont(new Font("Arial", Font.BOLD, 18));
        substringLabel.setBounds(300, 250, 200, 50);
        add(substringLabel);

        // Timer label
        timerLabel = new JLabel("Timer: 0:30", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        timerLabel.setBounds(650, 10, 100, 30);
        add(timerLabel);

        // Create player panels in a circle layout
        playerPanels = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            PlayerPanel playerPanel = new PlayerPanel("Player " + (i + 1));
            playerPanels.add(playerPanel);
            add(playerPanel);
        }
        positionPlayersInCircle();

        // Add arrow pointing to the current player
        arrowPanel = new ArrowPanel();
        arrowPanel.setBounds(390, 220, 20, 50);  // Initial position
        add(arrowPanel);
    }

    // Position players in a circular layout
    private void positionPlayersInCircle() {
        int centerX = 400;
        int centerY = 250;
        int radius = 150;

        for (int i = 0; i < playerPanels.size(); i++) {
            PlayerPanel playerPanel = playerPanels.get(i);
            double angle = 2 * Math.PI * i / playerPanels.size();
            int x = (int) (centerX + radius * Math.cos(angle) - 50);
            int y = (int) (centerY + radius * Math.sin(angle) - 50);
            playerPanel.setBounds(x, y, 100, 100);
        }
    }

    // Update the substring, timer, and which player the arrow points to
    public void updateGame(String substring, int timer, int activePlayerIndex) {
        substringLabel.setText(substring);
        timerLabel.setText("Timer: " + timer / 60 + ":" + String.format("%02d", timer % 60));
        currentPlayerIndex = activePlayerIndex;
        moveArrowToPlayer(currentPlayerIndex);  // Move arrow to current player
        repaint();
    }

    // Move the arrow to point to the current player
    private void moveArrowToPlayer(int playerIndex) {
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

    // Set new players list and reposition them in a circle
    public void setPlayers(List<String> playerNames) {
        // Clear existing player panels
        for (PlayerPanel panel : playerPanels) {
            remove(panel); // Remove panel from the GamePanel
        }
        playerPanels.clear(); // Clear the list of player panels

        // Create and add new player panels
        for (String playerName : playerNames) {
            PlayerPanel playerPanel = new PlayerPanel(playerName);
            playerPanels.add(playerPanel);
            add(playerPanel);
        }

        // Reposition the players in a circular layout
        positionPlayersInCircle();

        // Refresh the panel to reflect changes
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        // Create a frame to test the GamePanel
        JFrame frame = new JFrame("Game Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Create GamePanel with 4 players
        GamePanel gamePanel = new GamePanel(4);

        // Add the panel to the frame
        frame.add(gamePanel);

        // Show the frame
        frame.setVisible(true);

        // Simulate a game loop or user interaction to update the game
        new Timer(2000, e -> {
            gamePanel.updateGame("New Substring", 30, (gamePanel.currentPlayerIndex + 1) % 4);
        }).start();
    }
}
