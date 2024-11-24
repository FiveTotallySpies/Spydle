package dev.totallyspies.spydle.frontend.views.game_room_panels;

import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewModel;
import dev.totallyspies.spydle.shared.proto.messages.Player;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.swing.*;
import java.awt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GamePanel extends JPanel {

    private final Logger logger = LoggerFactory.getLogger(GamePanel.class);

    private final GameRoomViewModel model;

    private final Map<Player, PlayerPanel> playerPanels = new LinkedHashMap<>();
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
        int i = 1;
        for (Player player : model.getPlayerList()) {
            PlayerPanel playerPanel = new PlayerPanel("Player " + (i++));
            playerPanels.put(player, playerPanel);
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
        Set<String> currentPlayerNames = playerPanels
                .keySet()
                .stream()
                .map(Player::getPlayerName)
                .collect(Collectors.toSet());
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
            playerPanels.get(player).updateScore(player.getScore());
        }

        // Update the arrow for the current turn player
        Player currentTurnPlayer = model.getCurrentTurnPlayer();
        if (currentTurnPlayer != null) {
            moveArrowToPlayer(currentTurnPlayer);
        }

        substringLabel.setText(model.getCurrentSubstring());
        timerLabel.setText("Timer: " + model.getTimerSeconds() / 60 + ":" + String.format("%02d", model.getTimerSeconds() % 60));

        revalidate();
        repaint();
    }

    // Move the arrow to point to the current player
    private void moveArrowToPlayer(Player currentTurnPlayer) {
        // Find the index of the current turn player in the player list
        int playerIndex = this.model.getPlayerList().indexOf(currentTurnPlayer);

        int centerX = 400; // Center of the circle (horizontal)
        int centerY = 250; // Center of the circle (vertical)
        int radius = 150;  // Radius of the player circle

        // Calculate the angle of the current player in the circle
        double angle = 2 * Math.PI * playerIndex / playerPanels.size();

        // Calculate the arrow's base position, slightly offset from the circle
        int arrowX = (int) (centerX + (radius + 20) * Math.cos(angle)); // 20 pixels outward
        int arrowY = (int) (centerY + (radius + 20) * Math.sin(angle));

        // Rotate the arrow to point towards the center
        Graphics2D g2d = (Graphics2D) arrowPanel.getGraphics();
        if (g2d != null) {
            g2d.clearRect(0, 0, arrowPanel.getWidth(), arrowPanel.getHeight());
            double arrowAngle = angle; // Arrow angle matches the player's position angle
            g2d.rotate(arrowAngle, arrowPanel.getWidth() / 2.0, arrowPanel.getHeight() / 2.0);

            // Draw the arrow
            g2d.setColor(Color.RED); // Example arrow color
            Polygon arrowShape = new Polygon();
            arrowShape.addPoint(10, 0); // Arrow tip
            arrowShape.addPoint(-10, -20); // Left wing
            arrowShape.addPoint(-10, 20); // Right wing
            g2d.fill(arrowShape);
        }

        // Set the new position of the arrow panel
        arrowPanel.setBounds(arrowX - 10, arrowY - 10, 20, 20); // Adjust bounds to center arrow
    }

}
