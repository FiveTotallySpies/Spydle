package dev.totallyspies.spydle.frontend.views.game_room_panels;

import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewModel;
import java.awt.*;
import javax.swing.*;

// Panel for each player
class PlayerPanel extends JPanel {

  private final JLabel pointsLabel;
  private final JLabel guessLabel;
  private final JPanel parent;

  public PlayerPanel(String name, int score, JPanel parent) {
    this.parent = parent;

    setLayout(new BorderLayout());
    setBackground(new Color(255, 255, 255));
    setBorder(BorderFactory.createLineBorder(new Color(25, 25, 112)));

    JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
    nameLabel.setFont(new Font("Arial", Font.BOLD, 15));
    nameLabel.setForeground(new Color(25, 25, 112));
    add(nameLabel, BorderLayout.CENTER);

    pointsLabel = new JLabel(name, SwingConstants.CENTER);
    pointsLabel.setFont(new Font("Arial", Font.BOLD, 16));
    pointsLabel.setForeground(new Color(139, 0, 0));
    pointsLabel.setText("Score: " + score);
    add(pointsLabel, BorderLayout.SOUTH);

    guessLabel = new JLabel("", SwingConstants.CENTER);
    guessLabel.setHorizontalAlignment(JLabel.CENTER);
    guessLabel.setFont(new Font("Arial", Font.BOLD, 18));
    // Hide by default
    guessLabel.setForeground(new Color(139, 0, 0));
    guessLabel.setPreferredSize(new Dimension(150, 30));
    parent.add(guessLabel);
  }

  public void setLocation(int x, int y, int width, int height) {
    setBounds(x, y, width, height);
    guessLabel.setBounds((int) getBounds().getX() - 25, (int) getBounds().getY() - 30, 150, 30);
  }

  public void updateScore(int points) {
    pointsLabel.setText("Score: " + points);
  }

  public void setPlayerBorder(Color color, int thickness) {
    setBorder(BorderFactory.createLineBorder(color, thickness));
  }

  public void setPlayerGuess(String guess, GameRoomViewModel.Guess.Verdict verdict) {
    switch (verdict) {
      case CORRECT -> guessLabel.setForeground(new Color(34, 139, 34));
      case INCORRECT -> guessLabel.setForeground(new Color(139, 0, 0));
      case IN_PROGRESS -> guessLabel.setForeground(new Color(0, 0, 0));
    }
    guessLabel.setText(guess);
  }

  public void cleanupParent() {
    parent.remove(guessLabel);
  }
}
