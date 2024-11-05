import javax.swing.*;
import java.awt.*;

public class welcomestyled extends JFrame {

    public welcomestyled() {
        setTitle("Welcome - Join or Create Room");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main container panel styling
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        container.setBackground(new Color(195, 217, 255)); // #c3d9ff background color
        container.setPreferredSize(new Dimension(500, 500));

        // Title styling
        JLabel titleLabel = new JLabel("Spydle");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(139, 0 ,0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle styling
        JLabel joinLabel = new JLabel("Join an existing room");
        joinLabel.setFont(new Font("Arial", Font.BOLD, 20));
        joinLabel.setForeground(new Color(139, 0 ,0));
        joinLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        // Create Room Form
        JPanel createRoomPanel = new JPanel();
        createRoomPanel.setLayout(new BoxLayout(createRoomPanel, BoxLayout.Y_AXIS));
        createRoomPanel.setBackground(new Color(195, 217, 255)); // same as container
        createRoomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        JTextField nicknameField = new JTextField(15);
        nicknameField.setToolTipText("Enter your nickname");
        nicknameField.setMaximumSize(new Dimension(400, 30));
        JButton createRoomButton = new JButton("Create Room");
        styleButton(createRoomButton);

        createRoomPanel.add(nicknameField);
        createRoomPanel.add(Box.createVerticalStrut(10));
        createRoomPanel.add(createRoomButton);

        // Join Room Form
        JPanel joinRoomPanel = new JPanel();
        joinRoomPanel.setLayout(new BoxLayout(joinRoomPanel, BoxLayout.Y_AXIS));
        joinRoomPanel.setBackground(new Color(195, 217, 255));
        joinRoomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JTextField joinNicknameField = new JTextField(15);
        joinNicknameField.setToolTipText("Enter your nickname");
        joinNicknameField.setMaximumSize(new Dimension(400, 30));
        JTextField roomCodeField = new JTextField(15);
        roomCodeField.setToolTipText("Room code");
        roomCodeField.setMaximumSize(new Dimension(400, 30));

        JButton joinRoomButton = new JButton("Join Room");
        styleButton(joinRoomButton);

        joinRoomPanel.add(joinNicknameField);
        joinRoomPanel.add(Box.createVerticalStrut(10));
        joinRoomPanel.add(roomCodeField);
        joinRoomPanel.add(Box.createVerticalStrut(10));
        joinRoomPanel.add(joinRoomButton);

        // Adding components to container
        container.add(titleLabel);
        container.add(Box.createVerticalStrut(20));
        container.add(createRoomPanel);
        container.add(joinLabel);
        container.add(joinRoomPanel);

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
        SwingUtilities.invokeLater(welcomestyled::new);
    }
}