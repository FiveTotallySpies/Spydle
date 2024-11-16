package dev.totallyspies.spydle.frontend.views;

import javax.swing.*;
import java.awt.*;

@org.springframework.stereotype.Component
public class GameOverView extends JPanel {

    public GameOverView() {
        setLayout(new GridBagLayout()); // Center the container panel in the middle of the screen
        setBackground(new Color(195, 217, 255)); // Light blue background for the entire panel

        // Container Panel
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding around elements
        container.setBackground(new Color(195, 217, 255)); // Same light blue background

        // Title label
        JLabel titleLabel = new JLabel("Congratulations to the Best Players!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.DARK_GRAY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Player rankings with emojis
        JPanel rankingsPanel = new JPanel();
        rankingsPanel.setLayout(new BoxLayout(rankingsPanel, BoxLayout.Y_AXIS));
        rankingsPanel.setBackground(new Color(195, 217, 255)); // Same light blue background

        JLabel firstPlace = new JLabel("1st: 🥇 Player 1 - 100 points");
        firstPlace.setFont(new Font("Arial", Font.PLAIN, 18));
        firstPlace.setForeground(new Color(212, 175, 55)); // Darker gold color
        firstPlace.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel secondPlace = new JLabel("2nd: 🥈 Player 2 - 80 points");
        secondPlace.setFont(new Font("Arial", Font.PLAIN, 18));
        secondPlace.setForeground(new Color(169, 169, 169));
        secondPlace.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel thirdPlace = new JLabel("3rd: 🥉 Player 3 - 60 points");
        thirdPlace.setFont(new Font("Arial", Font.PLAIN, 18));
        thirdPlace.setForeground(new Color(205, 136, 64)); // Bronze color
        thirdPlace.setAlignmentX(Component.CENTER_ALIGNMENT);

        rankingsPanel.add(firstPlace);
        rankingsPanel.add(secondPlace);
        rankingsPanel.add(thirdPlace);

        // Thank you message
        JLabel thankYouLabel = new JLabel("Thank you for playing Spydle!");
        thankYouLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        thankYouLabel.setForeground(Color.DARK_GRAY);
        thankYouLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        thankYouLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        // Return to Welcome Page Button
        JButton returnButton = new JButton("Return to Welcome Page");
        styleButton(returnButton);
        returnButton.addActionListener(e -> {
            new WelcomeView(); // Open the main Spydle application
        });

        // Add components to container
        container.add(titleLabel);
        container.add(Box.createVerticalStrut(10));
        container.add(rankingsPanel);
        container.add(Box.createVerticalStrut(20));
        container.add(thankYouLabel);
        container.add(Box.createVerticalStrut(20));
        container.add(returnButton);

        // Add container to the center of the GameOverView using GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(container, gbc);
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(138, 43, 226)); // Blueviolet color
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(new Color(138, 43, 226));
                button.setBorder(BorderFactory.createLineBorder(new Color(138, 43, 226), 1));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(138, 43, 226));
                button.setForeground(Color.WHITE);
                button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("SpyDle - Game Over");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 500); // Full-screen dimensions
            frame.setLocationRelativeTo(null);
            frame.add(new GameOverView());
            frame.setVisible(true);
        });
    }
}
