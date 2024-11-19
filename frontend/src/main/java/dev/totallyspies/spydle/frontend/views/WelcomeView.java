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
        container.setBackground(new Color(195, 217, 255)); // Light blue background for the container
        container.setPreferredSize(new Dimension(800, 600)); // Adjusted container size for optimal height

        // Title styling
        JLabel titleLabel = new JLabel("Spydle");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40)); // Adjusted font size for larger window
        titleLabel.setForeground(new Color(139, 0, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nickname input field
        JTextField nicknameField = createPlaceholderTextField("Enter your nickname");
        nicknameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add horizontal line separator with increased thickness
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(500, 2)); // Adjusted width for the larger container
        separator.setForeground(new Color(75, 0, 130)); // Darker purple

        // Create room button centered
        JButton createRoomButton = new JButton("Create Room");
        styleButton(createRoomButton);
        createRoomButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Center alignment
        createRoomButton.setMaximumSize(new Dimension(500, 40)); // Adjusted size for the new layout

        // Room code field and join button panel
        JPanel joinPanel = new JPanel();
        joinPanel.setLayout(new BoxLayout(joinPanel, BoxLayout.X_AXIS));
        joinPanel.setBackground(new Color(195, 217, 255)); // Same background color as the container
        joinPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField roomCodeField = createPlaceholderTextField("Enter existing room code");
        roomCodeField.setMaximumSize(new Dimension(300, 30)); // Adjusted width for new layout

        JButton joinRoomButton = new JButton("Join");
        styleButton(joinRoomButton);
        joinRoomButton.setMaximumSize(new Dimension(120, 40)); // Adjusted size for the button
        joinRoomButton.setPreferredSize(new Dimension(120, 40));

        // Add components to the join panel
        joinPanel.add(roomCodeField);
        joinPanel.add(Box.createHorizontalStrut(10));
        joinPanel.add(joinRoomButton);

        // View All Rooms button
        JButton viewAllRoomsButton = new JButton("View All Rooms");
        styleButton(viewAllRoomsButton);
        viewAllRoomsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewAllRoomsButton.setMaximumSize(new Dimension(500, 40));
        viewAllRoomsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.openListRoomsView(); // Open the rooms page (AllRoomScreen.AllRoomsPage)
            }
        });
//        JButton listRoomsButton = new JButton("List of Rooms");
//        styleButton(listRoomsButton);
//        listRoomsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
//        listRoomsButton.setMaximumSize(new Dimension(500, 40)); // Adjusted size for the new layout

        // Adding components to the main container
        container.add(titleLabel);
        container.add(Box.createVerticalStrut(40)); // Space below the title
        container.add(nicknameField);
        container.add(Box.createVerticalStrut(40)); // Increased space between nickname and separator
        container.add(separator);
        container.add(Box.createVerticalStrut(40)); // Reduced space between separator and create button
        container.add(createRoomButton); // Centered create button

        container.add(Box.createVerticalStrut(20)); // Space before join panel
        container.add(joinPanel); // Join panel just below the create room button
        container.add(Box.createVerticalStrut(20)); // Reduced space before the List Rooms button
        container.add(viewAllRoomsButton); // List Rooms button right after join section

        add(container);
        setVisible(true);
    }

    private JTextField createPlaceholderTextField(String placeholder) {
        JTextField textField = new JTextField(15);
        textField.setText(placeholder);
        textField.setForeground(new Color(150, 150, 150)); // Light grey for placeholder
        textField.setMaximumSize(new Dimension(500, 30)); // Adjusted width for the larger container

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
            frame.setSize(800, 600); // Set frame size to 800x600
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            // Add the WelcomeView panel with light blue background
            frame.add(new WelcomeView());
            frame.setVisible(true);
        });
    }
}
