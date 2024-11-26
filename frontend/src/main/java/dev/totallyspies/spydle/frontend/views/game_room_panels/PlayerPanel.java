package dev.totallyspies.spydle.frontend.views.game_room_panels;

import javax.swing.*;
import java.awt.*;

// Panel for each player
class PlayerPanel extends JPanel {

    private final JLabel nameLabel;
    private final JLabel pointsLabel;

    public PlayerPanel(String name, int score) {
        setLayout(new BorderLayout());
        setBackground(new Color(255, 255, 255));
        setBorder(BorderFactory.createLineBorder(new Color(25, 25, 112)));

        nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        nameLabel.setForeground(new Color(25, 25, 112));
        add(nameLabel, BorderLayout.CENTER);

        pointsLabel = new JLabel(name, SwingConstants.CENTER);
        pointsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        pointsLabel.setForeground(new Color(139, 0, 0));
        pointsLabel.setText("Score: " + score);
        add(pointsLabel, BorderLayout.SOUTH);
    }

    public void updateScore(int points) {
        pointsLabel.setText("Score: " + points);
    }
    public void setPlayerBorder(Color color, int thickness) {
        setBorder(BorderFactory.createLineBorder(color, thickness));
    }
}