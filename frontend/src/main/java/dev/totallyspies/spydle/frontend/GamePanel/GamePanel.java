package dev.totallyspies.spydle.frontend.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Main Game Panel
class GamePanel extends JPanel {
    private List<PlayerPanel> playerPanels;
    private JLabel substringLabel;
    private JLabel timerLabel;
    private int currentPlayerIndex = 0;

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
        ArrowPanel arrowPanel = new ArrowPanel();
        arrowPanel.setBounds(390, 220, 20, 50);
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
        repaint();
    }

    // Paint the arrow to point to the current player
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentPlayerIndex < playerPanels.size()) {
            PlayerPanel activePlayer = playerPanels.get(currentPlayerIndex);
            Point arrowPoint = new Point(400, 275); // Center point for arrow

            int targetX = activePlayer.getX() + activePlayer.getWidth() / 2;
            int targetY = activePlayer.getY() + activePlayer.getHeight() / 2;

            g.setColor(Color.RED);
            g.drawLine(arrowPoint.x, arrowPoint.y, targetX, targetY);
            g.fillOval(targetX - 5, targetY - 5, 10, 10);
        }
    }
}
