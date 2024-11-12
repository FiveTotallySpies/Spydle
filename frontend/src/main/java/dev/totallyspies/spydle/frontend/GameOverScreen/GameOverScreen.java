package dev.totallyspies.spydle.frontend.GameOverScreen;

import dev.totallyspies.spydle.frontend.WelcomeScreen.WelcomeStyled;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameOverScreen extends JFrame {

    public GameOverScreen() {
        setTitle("Game Over");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Container Panel
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(120, 20, 50, 20));
        container.setBackground(new Color(195, 217, 255)); // Light blue background

        // Title label
        JLabel titleLabel = new JLabel("Congratulations to the Best Players!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.DARK_GRAY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));


        // Player rankings with emojis
        JPanel rankingsPanel = new JPanel();
        rankingsPanel.setLayout(new BoxLayout(rankingsPanel, BoxLayout.Y_AXIS));
        rankingsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rankingsPanel.setBackground(new Color(195, 217, 255)); // Same light blue background as the container

        // First place (Darkened Gold)
        JLabel firstPlace = new JLabel("1st: 🥇 Player 1 - 100 points");
        firstPlace.setFont(new Font("Arial", Font.PLAIN, 18));
        firstPlace.setForeground(new Color(212, 175, 55)); // Darker gold color (#D4AF37)
        firstPlace.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Second place (Brightened Silver)
        JLabel secondPlace = new JLabel("2nd: 🥈 Player 2 - 80 points");
        secondPlace.setFont(new Font("Arial", Font.PLAIN, 18));
        secondPlace.setForeground(new Color(169, 169, 169));
        secondPlace.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Third place (Bronze)
        JLabel thirdPlace = new JLabel("3rd: 🥉 Player 3 - 60 points");
        thirdPlace.setFont(new Font("Arial", Font.PLAIN, 18));
        thirdPlace.setForeground(new Color(205, 136, 64)); // Bronze color (#CD8740)
        thirdPlace.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add the labels to the rankings panel
        rankingsPanel.add(firstPlace);
        rankingsPanel.add(secondPlace);
        rankingsPanel.add(thirdPlace);

        // Thank you message
        JLabel thankYouLabel = new JLabel("Thank you for playing Spydle!");
        thankYouLabel.setFont(new Font("Arial", Font.BOLD, 16));
        thankYouLabel.setForeground(Color.DARK_GRAY);
        thankYouLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        thankYouLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        // Return to Welcome Page Button
        JButton returnButton = new JButton("Return to Welcome Page");
        styleButton(returnButton);

        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the welcome window and close the GameOverScreen
                new WelcomeStyled(); // Open the main Spydle application
                dispose(); // Close the Game Over screen
            }
        });

        // Add components to container
        container.add(titleLabel);
        container.add(Box.createVerticalStrut(10));
        container.add(rankingsPanel);  // Add the rankings panel
        container.add(Box.createVerticalStrut(20));
        container.add(thankYouLabel);
        container.add(Box.createVerticalStrut(20));
        container.add(returnButton);

        add(container);
        setVisible(true);
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
        SwingUtilities.invokeLater(GameOverScreen::new);
    }
}
