package dev.totallyspies.spydle.frontend.views.game_room_panels;

import javax.swing.*;
import java.awt.*;

// Panel for each player
class PlayerPanel extends JPanel {
    private JLabel nameLabel;

    public PlayerPanel(String name) {
        setLayout(new BorderLayout());
        setBackground(new Color(173, 216, 230));
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(nameLabel, BorderLayout.CENTER);
    }
}
