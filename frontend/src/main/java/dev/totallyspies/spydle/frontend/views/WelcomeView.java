package dev.totallyspies.spydle.frontend.views;

import dev.totallyspies.spydle.frontend.interface_adapters.welcome.WelcomeViewController;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

@org.springframework.stereotype.Component
public class WelcomeView extends JPanel {

    @Autowired
    private WelcomeViewController controller;

    public WelcomeView() {
        // Main container panel styling
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        container.setBackground(new Color(195, 217, 255)); // Light blue background
        container.setPreferredSize(new Dimension(400, 400)); // Compact container size

        // Title styling
        JLabel titleLabel = new JLabel("Spydle");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32)); // Adjusted font size
        titleLabel.setForeground(new Color(139, 0, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nickname input field
        JTextField nicknameField = createPlaceholderTextField("Enter your nickname");
        nicknameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add horizontal line separator with increased thickness
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(350, 2)); // Smaller width and height for compact layout
        separator.setForeground(new Color(75, 0, 130)); // Darker purple

        // Create room button centered
        JButton createRoomButton = new JButton("Create Room");
        styleButton(createRoomButton);
        createRoomButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Center alignment
        createRoomButton.setMaximumSize(new Dimension(350, 40)); // Adjusted size for compact layout

        // Room code field and join button panel
        JPanel joinPanel = new JPanel();
        joinPanel.setLayout(new BoxLayout(joinPanel, BoxLayout.X_AXIS));
        joinPanel.setBackground(new Color(195, 217, 255)); // Same background color as the container
        joinPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField roomCodeField = createPlaceholderTextField("Enter existing room code");
        roomCodeField.setMaximumSize(new Dimension(200, 30)); // Adjusted width for smaller layout

        JButton joinRoomButton = new JButton("Join");
        styleButton(joinRoomButton);
        joinRoomButton.setMaximumSize(new Dimension(100, 40)); // Smaller size for compact look
        joinRoomButton.setPreferredSize(new Dimension(100, 40));

        // Add components to the join panel
        joinPanel.add(roomCodeField);
        joinPanel.add(Box.createHorizontalStrut(10));
        joinPanel.add(joinRoomButton);

        // Adding components to the main container
        container.add(titleLabel);
        container.add(Box.createVerticalStrut(30)); // Space below the title
        container.add(nicknameField);
        container.add(Box.createVerticalStrut(30)); // Increased space between nickname and separator
        container.add(separator);
        container.add(Box.createVerticalStrut(30)); // Reduced space between separator and create button
        container.add(createRoomButton); // Centered create button
        container.add(Box.createVerticalStrut(5)); // Reduced space between create button and join section
        container.add(joinPanel);

        add(container);
        setVisible(true);
    }

    private JTextField createPlaceholderTextField(String placeholder) {
        JTextField textField = new JTextField(15);
        textField.setText(placeholder);
        textField.setForeground(new Color(150, 150, 150)); // Light grey for placeholder
        textField.setMaximumSize(new Dimension(350, 30)); // Adjusted for smaller container

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
                    textField.setForeground(new Color(150, 150, 150)); // Reset to light grey
                }
            }
        });
        return textField;
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(75, 0, 130)); // Darker purple color
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14)); // Slightly smaller font for a compact layout
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(75, 0, 130), 1), // Dark purple border
                BorderFactory.createEmptyBorder(10, 10, 10, 10) // Inner padding for button
        ));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(new Color(75, 0, 130)); // Dark purple on hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(75, 0, 130));
                button.setForeground(Color.WHITE);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setTitle("Welcome - Join or Create Room");
            frame.setSize(400, 400); // Updated frame size to match the smaller container
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.add(new WelcomeView());
            frame.setVisible(true);
        });
    }
}