package dev.totallyspies.spydle.frontend.views;

import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewController;
import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewModel;
import dev.totallyspies.spydle.frontend.views.game_room_panels.GamePanel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

@Component
@Profile("!local")
public class GameRoomView extends JPanel {

    private final GameRoomViewModel model;
    private final GameRoomViewController controller;

    private final GamePanel gamePanel;

    private final JPanel container;
    private final JTextField substringInputField;
    private final JPanel inputPanel;
    private final JLabel roomCodeLabel;

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
        backButton.setPreferredSize(new Dimension(170, 40));

        // Back button action listener
        backButton.addActionListener(event -> {
            controller.openWelcomeView(); // Open the rooms page (AllRoomScreen.AllRoomsPage)
        });

        JButton startGameButton = new JButton("Start Game");
        startGameButton.setFont(new Font("Arial", Font.BOLD, 14));
        startGameButton.setBackground(new Color(138, 43, 226)); // Blueviolet
        startGameButton.setForeground(Color.BLACK); // Black text color
        startGameButton.setFocusPainted(false);
        startGameButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startGameButton.setPreferredSize(new Dimension(170, 40));

        startGameButton.addActionListener(event -> {
            controller.startGame(); // Starts the game
        });

        roomCodeLabel = new JLabel("Room Code: ???");
        roomCodeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        roomCodeLabel.setPreferredSize(new Dimension(170, 40));

        // Top panel to hold the back button on the left
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false); // Transparent background to show main background color
        topPanel.add(backButton, BorderLayout.WEST); // Add back button to the left side
        topPanel.add(startGameButton, BorderLayout.EAST);
        topPanel.add(roomCodeLabel, BorderLayout.CENTER);

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
            controller.guessWord();
        });
        // Occurs when you hit the enter button with the input field in focus
        substringInputField.addActionListener(event -> {
            model.setStringEntered(substringInputField.getText());
            // Handle the entered substring as needed
            controller.guessWord();
        });

        // Note that we only add the inputPanel to the container if it is our turn during updateGame()
        inputPanel.add(substringInputField); // Add text field to input panel
        inputPanel.add(submitButton); // Add submit button to input panel

        add(container, BorderLayout.CENTER);
    }

    public void updateGame() {
        gamePanel.updateGame(); // Update game panel

        roomCodeLabel.setText("Room Code: " + model.getRoomCode());

        // check if the local player matches the player at the current turn
        if (model.getCurrentTurnPlayer() != null
                && model.getLocalPlayer() != null
                && model.getCurrentTurnPlayer().getPlayerName()
                .equals(model.getLocalPlayer().getPlayerName())) {
            // Add the input panel to the bottom of the container
            container.add(inputPanel, BorderLayout.SOUTH);
            substringInputField.requestFocus();
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
