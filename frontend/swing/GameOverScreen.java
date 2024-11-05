import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameOverScreen extends JFrame {

    public GameOverScreen() {
        setTitle("Game Over");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Container Panel
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
        container.setBackground(new Color(195, 217, 255)); // Light blue background

        // Title label
        JLabel titleLabel = new JLabel("Hopefully you enjoyed the play!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.DARK_GRAY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Thank you message
        JLabel thankYouLabel = new JLabel("Thank you for playing Spydle!");
        thankYouLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        thankYouLabel.setForeground(Color.DARK_GRAY);
        thankYouLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        thankYouLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        // Return to Welcome Page Button
        JButton returnButton = new JButton("Return to Welcome Page");
        styleButton(returnButton);

//        // Action for Return button I replaced under here with the main spydle app
//        returnButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                // Action to return to the welcome screen or exit for now
//                JOptionPane.showMessageDialog(container, "Returning to Welcome Page...");
//                dispose(); // Close the current window
//            }
//        });

        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the welcome window and close the GameOverScreen
                new welcome(); // Open the main Spydle application
                dispose(); // Close the Game Over screen
            }
        });

        // Add components to container
        container.add(titleLabel);
        container.add(Box.createVerticalStrut(10));
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
