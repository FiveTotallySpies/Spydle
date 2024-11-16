package dev.totallyspies.spydle.frontend.views;

import dev.totallyspies.spydle.frontend.views.game_room_panels.GamePanel;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class GameRoomView extends JPanel {
    public GamePanel gamePanel;

    public GameRoomView() {
//        setTitle("SPYDLE Game");
//        setSize(800, 600);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setLocationRelativeTo(null);

        // Create a container panel for the game screen
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());

        // Initialize the Game Panel with 4 players (adjust the number of players as needed)
        gamePanel = new GamePanel(4);

        // Add the game panel to the center of the container
        container.add(gamePanel, BorderLayout.CENTER);

        // Add a Back button at the top-left corner to return to the welcome page
        JButton backButton = new JButton("Back to Welcome");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(new Color(138, 43, 226)); // blueviolet
        backButton.setForeground(new Color(0, 0, 0)); // Set text color to black
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setPreferredSize(new Dimension(150, 40));

        // Add ActionListener to handle back button click
        backButton.addActionListener(e -> {
            // Close the game window and show the welcome page
//            this.dispose(); // Close the game window
            new WelcomeScreenView(); // Open the welcome page
        });

        // Add the back button to the top-left corner (NORTHWEST) of the window
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(backButton, BorderLayout.WEST); // Add back button to the left side

        // Add the top panel to the top (NORTH) of the main container
        container.add(topPanel, BorderLayout.NORTH);

        // Add the container to the frame
        add(container);

        // Make the game window visible
        setVisible(true);
    }

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(GameRoomView::new);
//    }
}
