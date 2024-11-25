package dev.totallyspies.spydle.frontend.views.game_room_panels;

import javax.swing.*;
import java.awt.*;

// Panel for each player
class PlayerPanel extends JPanel {

    private final JLabel nameLabel;
    private final JLabel pointsLabel;

    public PlayerPanel(String name, int score) {
        setLayout(new BorderLayout());
        setBackground(new Color(173, 216, 230));
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(nameLabel, BorderLayout.CENTER);

        pointsLabel = new JLabel(name, SwingConstants.CENTER);
        pointsLabel.setFont(new Font("Arial", Font.BOLD, 14));
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