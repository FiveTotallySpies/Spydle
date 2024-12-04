package dev.totallyspies.spydle.frontend.views;

import dev.totallyspies.spydle.frontend.interface_adapters.game_end.GameEndViewController;
import dev.totallyspies.spydle.frontend.interface_adapters.game_end.GameEndViewModel;
import dev.totallyspies.spydle.shared.proto.messages.Player;
import java.awt.*;
import javax.swing.*;
import org.springframework.context.annotation.Profile;

@org.springframework.stereotype.Component
@Profile("!test")
public class GameEndView extends JPanel implements CardView {

  private final GameEndViewController controller;

  private final JPanel rankingsPanel;
  private final GameEndViewModel model;

  public GameEndView(GameEndViewController controller, GameEndViewModel model) {
    this.controller = controller;
    this.model = model;

    setLayout(new GridBagLayout()); // Center the container panel in the middle of the screen
    setBackground(new Color(195, 217, 255)); // Light blue background for the entire panel

    // Container Panel
    JPanel container = new JPanel();
    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
    container.setBorder(
        BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding around elements
    container.setBackground(new Color(195, 217, 255)); // Same light blue background

    // Title label
    JLabel titleLabel = new JLabel("Congratulations to the Best Players!");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
    titleLabel.setForeground(Color.DARK_GRAY);
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    // Player rankings with emojis
    rankingsPanel = new JPanel();
    rankingsPanel.setLayout(new BoxLayout(rankingsPanel, BoxLayout.Y_AXIS));
    rankingsPanel.setBackground(new Color(195, 217, 255)); // Same light blue background

    // Thank you message
    JLabel thankYouLabel = new JLabel("Thank you for playing Spydle!");
    thankYouLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    thankYouLabel.setForeground(Color.DARK_GRAY);
    thankYouLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    thankYouLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

    // Back button
    JButton backButton = new JButton("Back to Welcome");
    styleButton(backButton);
    backButton.addActionListener(
        e -> {
          controller.openWelcomeView(); // Open the rooms page (AllRoomScreen.AllRoomsPage)
        });

    // Add components to container
    container.add(titleLabel);
    container.add(Box.createVerticalStrut(10));
    container.add(rankingsPanel);
    container.add(Box.createVerticalStrut(20));
    container.add(thankYouLabel);
    container.add(Box.createVerticalStrut(20));
    container.add(backButton);

    // Add container to the center of the GameEndView using GridBagConstraints
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.CENTER;
    add(container, gbc);
  }

  private void styleButton(JButton button) {
    button.setBackground(new Color(25, 25, 112)); // blueviolet
    button.setForeground(Color.WHITE);
    button.setFocusPainted(false);
    button.setFont(new Font("Arial", Font.BOLD, 12));
    button.setAlignmentX(Component.CENTER_ALIGNMENT);
    button.setMaximumSize(new Dimension(400, 40));
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));

    // Set a consistent, fixed padding around the button content
    button.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(25, 25, 112), 1), // Outer border color
            BorderFactory.createEmptyBorder(10, 10, 10, 10) // Inner padding to avoid layout shift
            ));

    // Hover effect
    button.addMouseListener(
        new java.awt.event.MouseAdapter() {
          public void mouseEntered(java.awt.event.MouseEvent evt) {
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(25, 25, 112)); // blueviolet
          }

          public void mouseExited(java.awt.event.MouseEvent evt) {
            button.setBackground(new Color(25, 25, 112));
            button.setForeground(Color.WHITE);
          }
        });
  }

//  create the placements of the players based on their scores
  public void setPlacements() {
    rankingsPanel.removeAll();
    int i = 1;
    for (Player player : model.getPlayers()) {
      JLabel placement =
          new JLabel(
              "#" + (i++) + ": " + player.getPlayerName() + " - " + player.getScore() + " points");
      placement.setFont(new Font("Arial", Font.PLAIN, 18));
      placement.setForeground(new Color(212, 175, 55)); // Darker gold color
      placement.setAlignmentX(Component.CENTER_ALIGNMENT);
      rankingsPanel.add(placement);
    }

  }

}
