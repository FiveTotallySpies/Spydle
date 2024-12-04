package dev.totallyspies.spydle.frontend.views;

import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewController;
import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewModel;
import dev.totallyspies.spydle.frontend.views.game_room_panels.GamePanel;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import lombok.Getter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * The GameRoomView class represents the user interface for the game room, where players can
 * interact with the game, view the room code, and input guesses. This class is responsible for
 * displaying the game screen, handling user input, and updating the game state in real-time.
 */
@Component
@Profile("!test")
public class GameRoomView extends JPanel implements CardView {

  private final GameRoomViewModel model;
  private final GameRoomViewController controller;

  private final GamePanel gamePanel;

  private final JPanel container;
  @Getter private final JTextField substringInputField;
  private final JPanel inputPanel;
  private final JLabel roomCodeLabel;
  private final JButton startGameButton;
  private final JPanel topPanel;

  /**
   * Constructs the GameRoomView, initializing the game UI elements such as the game panel, input
   * fields, and action buttons. This method also sets up the layout of the components within the
   * view.
   *
   * @param model The view model that holds the state and data for the game room.
   * @param controller The controller responsible for handling interactions and updating the view.
   */
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
    JButton backButton =
        createStyledButton(
            "Home",
            new Color(165, 195, 255), // Background color
            Color.WHITE, // Text color
            () -> controller.openWelcomeView() // Action on click
            );

    startGameButton =
        createStyledButton(
            "Start Game",
            new Color(165, 195, 255), // Background color
            Color.WHITE, // Text color
            () -> controller.startGame() // Action on click
            );

    roomCodeLabel = new JLabel("Room Code: ???", SwingConstants.CENTER);
    roomCodeLabel.setFont(new Font("Arial", Font.BOLD, 18));
    roomCodeLabel.setForeground(new Color(139, 0, 0));
    roomCodeLabel.setPreferredSize(new Dimension(170, 40));

    // Top panel to hold the back button on the left
    topPanel = new JPanel(new BorderLayout());
    topPanel.setOpaque(false); // Transparent background to show main background color
    topPanel.add(backButton, BorderLayout.WEST); // Add back button to the left side
    topPanel.add(roomCodeLabel, BorderLayout.CENTER);
    topPanel.add(startGameButton, BorderLayout.EAST);

    // Add the top panel to the top (NORTH) of the main container
    container.add(topPanel, BorderLayout.NORTH);

    // Create a panel for the substring input field and submit button
    inputPanel = new JPanel();
    inputPanel.setOpaque(false); // Transparent background

    substringInputField = new JTextField(20); // Text field for substring input
    substringInputField.setFont(new Font("Arial", Font.BOLD, 18));
    // Occurs when you hit the enter button with the input field in focus
    substringInputField.addActionListener(
        event -> {
          model.setStringEntered(substringInputField.getText());
          controller.guessWord();
        });

    JButton submitButton =
        createStyledButton(
            "Submit",
            new Color(165, 195, 255), // Background color
            Color.WHITE, // Text color
            () -> {
              model.setStringEntered(substringInputField.getText());
              controller.guessWord();
            });

    // Action listener for the string as it is entered
    substringInputField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent documentEvent) {
                updateStringEntered();
              }

              @Override
              public void removeUpdate(DocumentEvent documentEvent) {
                updateStringEntered();
              }

              @Override
              public void changedUpdate(DocumentEvent documentEvent) {
                updateStringEntered();
              }

              private void updateStringEntered() {
                model.setStringEntered(substringInputField.getText());
                controller.updateGuess();
              }
            });

    // Note that we only add the inputPanel to the container if it is our turn during updateGame()
    inputPanel.add(substringInputField); // Add text field to input panel
    inputPanel.add(submitButton); // Add submit button to input panel

    add(container, BorderLayout.CENTER);
  }

  /**
   * Clears the text input field where the user enters their guess. This is called after a valid
   * guess is submitted.
   */
  public void clearSubstringInputField() {
    substringInputField.setText("");
  }

  /**
   * Updates the guess progress for each player by displaying their current guess on the game panel.
   */
  public void updateGuessProgress() {
    for (String playerName : model.getCurrentGuesses().keySet()) {
      GameRoomViewModel.Guess guess = model.getCurrentGuesses().get(playerName);
      gamePanel.updateStringDisplayed(playerName, guess);
    }
  }

  /**
   * Updates the game UI elements based on the current game state, such as the current player's
   * turn, the room code, and whether the input panel should be displayed. If it's the local
   * player's turn, the input panel will be added to the screen.
   */
  public synchronized void updateGame() {
    gamePanel.updateGame(); // Update game panel

    roomCodeLabel.setText("Room Code: " + model.getRoomCode());

    // check if the local player matches the player at the current turn
    if (model.getCurrentTurnPlayer() != null
        && model.getLocalPlayer() != null
        && model
            .getCurrentTurnPlayer()
            .getPlayerName()
            .equals(model.getLocalPlayer().getPlayerName())) {
      // Add the input panel to the bottom of the container
      container.add(inputPanel, BorderLayout.SOUTH);
      substringInputField.requestFocus();
    } else {
      container.remove(inputPanel);
    }

    if (model.getCurrentTurnPlayer() == null) { // Game is running
      topPanel.add(startGameButton, BorderLayout.EAST);
    } else {
      topPanel.remove(startGameButton);
    }
  }

  /**
   * Creates and styles a JButton with specific background and text colors and an associated action.
   *
   * @param text The text displayed on the button.
   * @param backgroundColor The background color of the button.
   * @param textColor The text color of the button.
   * @param action The action to be executed when the button is clicked.
   * @return A JButton with the specified properties and action.
   */
  private JButton createStyledButton(
      String text, Color backgroundColor, Color textColor, Runnable action) {
    JButton button = new JButton(text);
    button.setFont(new Font("Arial", Font.BOLD, 16));
    button.setBackground(backgroundColor);
    button.setForeground(textColor);
    button.setFocusPainted(false);
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    button.setPreferredSize(new Dimension(150, 40));

    // Add action listener
    button.addActionListener(e -> action.run());

    // Add hover effect
    button.addMouseListener(
        new java.awt.event.MouseAdapter() {
          @Override
          public void mouseEntered(java.awt.event.MouseEvent e) {
            button.setBackground(textColor); // Swap to text color
            button.setForeground(backgroundColor); // Swap to background color
          }

          @Override
          public void mouseExited(java.awt.event.MouseEvent e) {
            button.setBackground(backgroundColor); // Revert to original background
            button.setForeground(textColor); // Revert to original text color
          }
        });

    return button;
  }
}
