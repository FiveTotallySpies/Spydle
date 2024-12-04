package dev.totallyspies.spydle.frontend.views.game_room_panels;

import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewModel;
import java.awt.*;
import javax.swing.*;


/**
 * Represents the panel for a player in the game room.
 * This panel displays the player's name, score, and current guess, along with styling for clarity and visibility.
 */
class PlayerPanel extends JPanel {

  private final JLabel pointsLabel;
  private final JLabel guessLabel;
  private final JPanel parent;

  /**
   * Constructs a PlayerPanel instance to represent a player's information.
   *
   * @param name   The player's name.
   * @param score  The player's current score.
   * @param parent The parent panel containing this panel, used for managing layout.
   */
  public PlayerPanel(String name, int score, JPanel parent) {
    this.parent = parent;

    setLayout(new BorderLayout());
    setBackground(new Color(255, 255, 255)); // White background
    setBorder(BorderFactory.createLineBorder(new Color(25, 25, 112))); // Dark blue border

    JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
    nameLabel.setFont(new Font("Arial", Font.BOLD, 15));
    nameLabel.setForeground(new Color(25, 25, 112));
    add(nameLabel, BorderLayout.CENTER);

    pointsLabel = new JLabel(name, SwingConstants.CENTER);
    pointsLabel.setFont(new Font("Arial", Font.BOLD, 16));
    pointsLabel.setForeground(new Color(139, 0, 0)); // Red text for score
    pointsLabel.setText("Score: " + score);
    add(pointsLabel, BorderLayout.SOUTH);

    guessLabel = new JLabel("", SwingConstants.CENTER);
    guessLabel.setHorizontalAlignment(JLabel.CENTER);
    guessLabel.setFont(new Font("Arial", Font.BOLD, 18));
    guessLabel.setForeground(new Color(139, 0, 0)); // Red text for guess
    guessLabel.setPreferredSize(new Dimension(150, 30));
    parent.add(guessLabel); // Add guess label to the parent panel
  }

  /**
   * Sets the location and dimensions of the panel and adjusts the position of the guess label.
   *
   * @param x      The x-coordinate of the panel.
   * @param y      The y-coordinate of the panel.
   * @param width  The width of the panel.
   * @param height The height of the panel.
   */
  public void setLocation(int x, int y, int width, int height) {
    setBounds(x, y, width, height);
    guessLabel.setBounds((int) getBounds().getX() - 25, (int) getBounds().getY() - 30, 150, 30);
  }

  /**
   * Updates the player's score displayed on the panel.
   *
   * @param points The updated score.
   */
  public void updateScore(int points) {
    pointsLabel.setText("Score: " + points);
  }

  /**
   * Sets a custom border for the player's panel.
   *
   * @param color     The color of the border.
   * @param thickness The thickness of the border.
   */
  public void setPlayerBorder(Color color, int thickness) {
    setBorder(BorderFactory.createLineBorder(color, thickness));
  }

  /**
   * Updates the player's guess and sets its verdict (e.g., correct, incorrect, in progress).
   *
   * @param guess   The player's current guess.
   * @param verdict The verdict of the guess, represented by {@link GameRoomViewModel.Guess.Verdict}.
   */
  public void setPlayerGuess(String guess, GameRoomViewModel.Guess.Verdict verdict) {
    switch (verdict) {
      case CORRECT -> guessLabel.setForeground(new Color(34, 139, 34)); // Green for correct
      case INCORRECT -> guessLabel.setForeground(new Color(139, 0, 0)); // Red for incorrect
      case IN_PROGRESS -> guessLabel.setForeground(new Color(0, 0, 0)); // Black for in progress
    }
    guessLabel.setText(guess);
  }

  /**
   * Removes the guess label from the parent panel to clean up resources.
   */
    public void cleanupParent() {
      parent.remove(guessLabel);
    }

  }
