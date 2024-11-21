package dev.totallyspies.spydle.frontend.views;

import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewController;
import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewModel;
import dev.totallyspies.spydle.frontend.views.game_room_panels.GamePanel;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

@Component
public class GameRoomView extends JPanel {

    private final GameRoomViewModel model;
    private final GameRoomViewController controller;

    private final GamePanel gamePanel;

    private final JPanel container;
    private final JTextField substringInputField;
    private final JPanel inputPanel;

    public GameRoomView(GameRoomViewModel model, GameRoomViewController controller) {
        this.model = model;
        this.controller = controller;

        setLayout(new BorderLayout()); // Set layout to BorderLayout for GameRoomView
        setBackground(new Color(195, 217, 255)); // Set light blue background for entire view

        // Create a container panel for the game screen
        container = new JPanel(new BorderLayout());
        container.setOpaque(false); // Make container transparent to show the GameRoomView background
        model.setPlayerList(new ArrayList<>());
        // Initialize the Game Panel with 4 players (adjust the number of players as needed)
        gamePanel = new GamePanel(this.model);

        // Add the game panel to the center of the container
        container.add(gamePanel, BorderLayout.CENTER);

        // Back button styling
        JButton backButton = new JButton("Back to Welcome");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(new Color(138, 43, 226)); // Blueviolet
        backButton.setForeground(Color.BLACK); // Black text color
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setPreferredSize(new Dimension(150, 40));

        // Back button action listener
        backButton.addActionListener(event -> {
            controller.openWelcomeView(); // Open the rooms page (AllRoomScreen.AllRoomsPage)
        });

        // Top panel to hold the back button on the left
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false); // Transparent background to show main background color
        topPanel.add(backButton, BorderLayout.WEST); // Add back button to the left side

        // Add the top panel to the top (NORTH) of the main container
        container.add(topPanel, BorderLayout.NORTH);

        // Create a panel for the substring input field and submit button
        inputPanel = new JPanel();
        inputPanel.setOpaque(false); // Transparent background
        substringInputField = new JTextField(20); // Text field for substring input
        JButton submitButton = new JButton("Submit");

        // Action listener for submit button
        submitButton.addActionListener(event -> {
            model.setStringEntered(substringInputField.getText());
            // Handle the entered substring as needed
            System.out.println("Entered substring: " + model.getStringEntered());
        });

        // Note that we only add the inputPanel to the container if it is our turn during updateGame()
        inputPanel.add(substringInputField); // Add text field to input panel
        inputPanel.add(submitButton); // Add submit button to input panel

        add(container, BorderLayout.CENTER);
    }

    public void updateGame() {
        gamePanel.updateGame(); // Update game panel

        // check if the local player matches the player at the current turn
        if (model.getCurrentTurnPlayer().getPlayerName()
                .equals(model.getLocalPlayer().getPlayerName())){
            // Add the input panel to the bottom of the container
            container.add(inputPanel, BorderLayout.SOUTH);
        } else {
            container.remove(inputPanel);
        }
    }

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Spydle - Game Over");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(800, 600); // Full-screen dimensions
//            frame.setLocationRelativeTo(null);
//            frame.add(new GameRoomView());
//            frame.setVisible(true);
//        });
//    }

}
