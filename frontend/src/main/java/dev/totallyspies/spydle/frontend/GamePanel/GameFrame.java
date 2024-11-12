package dev.totallyspies.spydle.frontend.GamePanel;

import dev.totallyspies.spydle.frontend.WelcomeScreen.WelcomeStyled;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    private GamePanel gamePanel;
    private dev.totallyspies.spydle.frontend.GamePanel.ClockTimer clockTimer; // Declare the ClockTimer

    public GameFrame() {
        setTitle("SPYDLE Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main container panel styling
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(195, 217, 255)); // Light blue background

        // Initialize the Game Panel with 4 players (adjust the number of players as needed)
        gamePanel = new GamePanel(4);

        // Add the game panel to the center of the container
        container.add(gamePanel, BorderLayout.CENTER);

        // Add a Back button at the top-left corner to return to the welcome page
        JButton backButton = new JButton("Back to Welcome");
        styleButton(backButton);

        backButton.addActionListener(e -> {
            this.dispose(); // Close the game window
            new WelcomeStyled(); // Open the welcome page
        });

        // Initialize ClockTimer
        clockTimer = new dev.totallyspies.spydle.frontend.GamePanel.ClockTimer();

        // Top panel with separate sections for back button and clock timer
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(195, 217, 255)); // Match container background

        // Panel for the back button (aligned to the left)
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButtonPanel.setOpaque(false); // Transparent background
        backButtonPanel.add(backButton);

        // Panel for the clock timer (aligned to the right)
        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        timerPanel.setOpaque(false); // Transparent background
        timerPanel.add(clockTimer);

        // Add the back button panel to the left and the timer panel to the right
        topPanel.add(backButtonPanel, BorderLayout.WEST);
        topPanel.add(timerPanel, BorderLayout.EAST);

        // Add the top panel to the top (NORTH) of the main container
        container.add(topPanel, BorderLayout.NORTH);

        // Add container to the frame
        add(container);
        setVisible(true);
    }

    // Button styling method for consistent appearance
    private void styleButton(JButton button) {
        button.setBackground(new Color(138, 43, 226)); // Blue-violet color
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(new Color(138, 43, 226)); // Blue-violet color
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
        SwingUtilities.invokeLater(GameFrame::new);
    }
}
