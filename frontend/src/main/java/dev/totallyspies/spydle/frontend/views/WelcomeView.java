package dev.totallyspies.spydle.frontend.views;

import dev.totallyspies.spydle.frontend.interface_adaptors.welcome_screen_adaptors.WelcomeScreenController;
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
    private WelcomeScreenController controller;

    public WelcomeView() {

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
        createRoomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JTextField nicknameField = createPlaceholderTextField("Enter your nickname");
        JTextField roomNameField = createPlaceholderTextField("Enter room name");

        JButton createRoomButton = new JButton("Create Room");
        styleButton(createRoomButton);

        createRoomPanel.add(nicknameField);
        createRoomPanel.add(Box.createVerticalStrut(10));
        createRoomPanel.add(roomNameField);
        createRoomPanel.add(Box.createVerticalStrut(10));
        createRoomPanel.add(createRoomButton);

        // Join Room Form
        JPanel joinRoomPanel = new JPanel();
        joinRoomPanel.setLayout(new BoxLayout(joinRoomPanel, BoxLayout.Y_AXIS));
        joinRoomPanel.setBackground(new Color(195, 217, 255));
        joinRoomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

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
        viewAllRoomsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.changeView("AllRoomView"); // Open the rooms page (AllRoomScreen.AllRoomsPage)
            }
        });

        // Adding components to container
        container.add(titleLabel);
        container.add(Box.createVerticalStrut(20));
        container.add(createRoomPanel);
        container.add(joinLabel);
        container.add(joinRoomPanel);
        container.add(Box.createVerticalStrut(20));
        container.add(viewAllRoomsButton); // Add the button to the panel

        add(container);
        setVisible(true);
    }

    private JTextField createPlaceholderTextField(String placeholder) {
        JTextField textField = new JTextField(15);
        textField.setText(placeholder);  // Set the placeholder text
        textField.setForeground(new Color(169, 169, 169));  // Grey color for placeholder
        textField.setMaximumSize(new Dimension(400, 30));

        // FocusListener to implement placeholder text behavior
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
                    textField.setForeground(new Color(169, 169, 169)); // Grey color
                }
            }
        });
        return textField;
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(138, 43, 226)); // blueviolet
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(400, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Set a consistent, fixed padding around the button content
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(138, 43, 226), 1), // Outer border color
                BorderFactory.createEmptyBorder(10, 10, 10, 10)  // Inner padding to avoid layout shift
        ));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(new Color(138, 43, 226)); // blueviolet
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(138, 43, 226));
                button.setForeground(Color.WHITE);
            }
        });
    }

    // Test the JPanel in a JFrame with size 500x500
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setTitle("Welcome - Join or Create Room");
            frame.setSize(500, 500);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.add(new WelcomeView());
            frame.setVisible(true);
        });


    }

}
