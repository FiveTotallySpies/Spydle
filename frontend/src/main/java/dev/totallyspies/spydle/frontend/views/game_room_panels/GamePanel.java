package dev.totallyspies.spydle.frontend.views.game_room_panels;

import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewModel;
import dev.totallyspies.spydle.shared.proto.messages.Player;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.swing.*;
import java.awt.*;

// Main Game Panel
public class GamePanel extends JPanel {

    private final GameRoomViewModel model;

    private final Map<Player, PlayerPanel> playerPanels = new ConcurrentHashMap<>();
    private final JLabel substringLabel;
    private final JLabel timerLabel;

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
        ArrowPanel arrowPanel = new ArrowPanel();
        arrowPanel.setBounds(390, 220, 20, 50);
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

        // TODO: make arrow change to model.turnPlayer

        substringLabel.setText(model.getCurrentSubstring());
        timerLabel.setText("Timer: " + model.getTimerSeconds() / 60 + ":" + String.format("%02d", model.getTimerSeconds() % 60));

        revalidate();
        repaint();
    }

}
