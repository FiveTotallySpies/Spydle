package dev.totallyspies.spydle.frontend.WelcomeScreen;

import dev.totallyspies.spydle.frontend.AllRoomScreen.AllRoomScreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class WelcomeStyled extends JFrame {

    public WelcomeStyled() {
        setTitle("Welcome - Join or Create Room");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main container panel styling
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        container.setBackground(new Color(195, 217, 255)); // #c3d9ff background color
        container.setPreferredSize(new Dimension(500, 500));

        // Title styling
        JLabel titleLabel = new JLabel("Spydle");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(139, 0, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // "Create Room" section title
        JLabel createRoomTitle = new JLabel("Create a Room");
        createRoomTitle.setFont(new Font("Arial", Font.BOLD, 20));
        createRoomTitle.setForeground(new Color(139, 0, 0));
        createRoomTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        createRoomTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        // Create Room Form
        JPanel createRoomPanel = new JPanel();
        createRoomPanel.setLayout(new BoxLayout(createRoomPanel, BoxLayout.Y_AXIS));
        createRoomPanel.setBackground(new Color(195, 217, 255));
        createRoomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        JTextField nicknameField = createPlaceholderTextField("Enter your nickname");
        JTextField roomNameField = createPlaceholderTextField("Enter room name");

        JButton createRoomButton = new JButton("Create Room");
        styleButton(createRoomButton);

        createRoomPanel.add(nicknameField);
        createRoomPanel.add(Box.createVerticalStrut(10));
        createRoomPanel.add(roomNameField);
        createRoomPanel.add(Box.createVerticalStrut(10));
        createRoomPanel.add(createRoomButton);

        // "Join Room" section title
        JLabel joinRoomTitle = new JLabel("Join an Existing Room");
        joinRoomTitle.setFont(new Font("Arial", Font.BOLD, 20));
        joinRoomTitle.setForeground(new Color(139, 0, 0));
        joinRoomTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinRoomTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        // Join Room Form
        JPanel joinRoomPanel = new JPanel();
        joinRoomPanel.setLayout(new BoxLayout(joinRoomPanel, BoxLayout.Y_AXIS));
        joinRoomPanel.setBackground(new Color(195, 217, 255));
        joinRoomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        JTextField joinNicknameField = createPlaceholderTextField("Enter your nickname");
        JTextField roomCodeField = createPlaceholderTextField("Enter existing room name");

        JButton joinRoomButton = new JButton("Join Room");
        styleButton(joinRoomButton);

        joinRoomPanel.add(joinNicknameField);
        joinRoomPanel.add(Box.createVerticalStrut(10));
        joinRoomPanel.add(roomCodeField);
        joinRoomPanel.add(Box.createVerticalStrut(10));
        joinRoomPanel.add(joinRoomButton);

        // View All Rooms button
        JButton viewAllRoomsButton = new JButton("View All Rooms");
        styleButton(viewAllRoomsButton);
        viewAllRoomsButton.addActionListener(e -> openRoomsPage());

        // Adding components to container
        container.add(titleLabel);
        container.add(Box.createVerticalStrut(10));
        container.add(createRoomTitle);
        container.add(createRoomPanel);
        container.add(joinRoomTitle);
        container.add(joinRoomPanel);
        container.add(Box.createVerticalStrut(10));
        container.add(viewAllRoomsButton);

        add(container);
        setVisible(true);
    }

    private JTextField createPlaceholderTextField(String placeholder) {
        JTextField textField = new JTextField(15);
        textField.setText(placeholder);
        textField.setForeground(new Color(169, 169, 169));
        textField.setMaximumSize(new Dimension(400, 30));

        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(new Color(169, 169, 169));
                }
            }
        });
        return textField;
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(138, 43, 226));
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

    private void openRoomsPage() {
        this.dispose();
        new AllRoomScreen();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WelcomeStyled::new);
    }
}
