package AllRoomScreen;

import javax.swing.*;
import java.awt.*;

public class AllRoomScreen extends JFrame {

    public AllRoomScreen() {
        setTitle("All Rooms");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main container panel styling
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        container.setBackground(new Color(195, 217, 255));

        // Title styling
        JLabel titleLabel = new JLabel("All Rooms");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(139, 0, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fake room data with the format "Room 1: Name"
        String[] roomData = {
                "Room 1: When you wake up",
                "Room 2: Next to him",
                "Room 3: In the middle",
                "Room 4: Of the night"
        };

        // Create JList with fake data
        JList<String> roomList = new JList<>(roomData);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomList.setFont(new Font("Arial", Font.PLAIN, 16));
        JScrollPane roomScrollPane = new JScrollPane(roomList);
        roomScrollPane.setPreferredSize(new Dimension(400, 200));

        // Back button
        JButton backButton = new JButton("Back to Welcome");
        styleButton(backButton);
        backButton.addActionListener(e -> {
            this.dispose(); // Close current window
            new WelcomeStyled(); // Open the welcome page again (ensure the class is correctly named)
        });

        // Adding components to container
        container.add(titleLabel);
        container.add(Box.createVerticalStrut(20));
        container.add(roomScrollPane);
        container.add(Box.createVerticalStrut(20));
        container.add(backButton);

        add(container);
        setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(138, 43, 226)); // blueviolet
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(400, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(new Color(138, 43, 226)); // blueviolet
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
        SwingUtilities.invokeLater(AllRoomScreen::new);
    }
}
