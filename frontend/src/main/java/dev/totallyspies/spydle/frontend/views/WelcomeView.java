package dev.totallyspies.spydle.frontend.views;

import dev.totallyspies.spydle.frontend.interface_adapters.welcome.WelcomeViewController;
import dev.totallyspies.spydle.frontend.interface_adapters.welcome.WelcomeViewModel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

@org.springframework.stereotype.Component
public class WelcomeView extends JPanel {

    private final WelcomeViewController controller;
    private final WelcomeViewModel welcomeViewModel;

    private JTextField nicknameField;
    private JTextField roomCodeField;
    private JLabel welcomeMessageLabel; // New label for welcome message

    public WelcomeView(WelcomeViewController controller, WelcomeViewModel welcomeViewModel) {
        this.controller = controller;
        this.welcomeViewModel = welcomeViewModel;
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        container.setBackground(new Color(195, 217, 255)); // Light blue background for the container
        container.setPreferredSize(new Dimension(800, 600)); // Adjusted container size for optimal height

        JLabel titleLabel = new JLabel("Spydle");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(new Color(139, 0, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        nicknameField = createPlaceholderTextField("Enter your nickname");
        nicknameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        nicknameField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateNickname(); }
            public void removeUpdate(DocumentEvent e) { updateNickname(); }
            public void changedUpdate(DocumentEvent e) { updateNickname(); }

            private void updateNickname() {
                welcomeViewModel.setPlayerName(nicknameField.getText());
            }
        });

        JButton enterButton = new JButton("Enter");
        styleButton(enterButton);
        enterButton.setMaximumSize(new Dimension(120, 40));
        enterButton.setPreferredSize(new Dimension(120, 40));

        enterButton.addActionListener(new ActionListener() {
            private boolean isEnter = true;

            @Override
            public void actionPerformed(ActionEvent e) {
                welcomeViewModel.setPlayerName(nicknameField.getText());

                if (isEnter) {
                    welcomeMessageLabel.setText("Welcome " + welcomeViewModel.getPlayerName() + "!");
                    enterButton.setText("Cancel");
                    isEnter = false;
                } else {
                    welcomeMessageLabel.setText(""); // Clear the welcome message
                    enterButton.setText("Enter");
                    nicknameField.setText("Enter your nickname");
                    isEnter = true;
                }
            }
        });

        JPanel nicknamePanel = new JPanel();
        nicknamePanel.setLayout(new BoxLayout(nicknamePanel, BoxLayout.X_AXIS));
        nicknamePanel.setBackground(new Color(195, 217, 255));
        nicknamePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        nicknamePanel.add(nicknameField);
        nicknamePanel.add(Box.createHorizontalStrut(10));
        nicknamePanel.add(enterButton);

        welcomeMessageLabel = new JLabel(""); // Initialize welcome message label
        welcomeMessageLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        welcomeMessageLabel.setForeground(new Color(75, 0, 130)); // Purple color
        welcomeMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(500, 2));
        separator.setForeground(new Color(75, 0, 130));

        JButton createRoomButton = new JButton("Create Room");
        styleButton(createRoomButton);
        createRoomButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createRoomButton.setMaximumSize(new Dimension(500, 40));
        createRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                welcomeViewModel.setRoomCode(nicknameField.getText());
                controller.createGame();
            }
        });

        JPanel joinPanel = new JPanel();
        joinPanel.setLayout(new BoxLayout(joinPanel, BoxLayout.X_AXIS));
        joinPanel.setBackground(new Color(195, 217, 255));
        joinPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        roomCodeField = createPlaceholderTextField("Enter existing room code");
        roomCodeField.setMaximumSize(new Dimension(300, 30));
        roomCodeField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateRoomCode(); }
            public void removeUpdate(DocumentEvent e) { updateRoomCode(); }
            public void changedUpdate(DocumentEvent e) { updateRoomCode(); }

            private void updateRoomCode() {
                welcomeViewModel.setRoomCode(roomCodeField.getText());
            }
        });

        JButton joinRoomButton = new JButton("Join");
        styleButton(joinRoomButton);
        joinRoomButton.setMaximumSize(new Dimension(120, 40));
        joinRoomButton.setPreferredSize(new Dimension(120, 40));
        joinRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                welcomeViewModel.setRoomCode(roomCodeField.getText());
                controller.joinGame();
            }
        });

        joinPanel.add(roomCodeField);
        joinPanel.add(Box.createHorizontalStrut(10));
        joinPanel.add(joinRoomButton);

        JButton viewAllRoomsButton = new JButton("View All Rooms");
        styleButton(viewAllRoomsButton);
        viewAllRoomsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewAllRoomsButton.setMaximumSize(new Dimension(500, 40));
        viewAllRoomsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.openListRoomsView();
            }
        });

        container.add(titleLabel);
        container.add(Box.createVerticalStrut(40));
        container.add(nicknamePanel);
        container.add(welcomeMessageLabel); // Add welcome message label here
        container.add(Box.createVerticalStrut(40));
        container.add(separator);
        container.add(Box.createVerticalStrut(40));
        container.add(createRoomButton);
        container.add(Box.createVerticalStrut(20));
        container.add(joinPanel);
        container.add(Box.createVerticalStrut(20));
        container.add(viewAllRoomsButton);

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

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame();
//            frame.setTitle("Welcome - Join or Create Room");
//            frame.setSize(800, 600); // Set frame size to 800x600
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setLocationRelativeTo(null);
//            // Add the WelcomeView panel with light blue background
//            frame.add(new WelcomeView());
//            frame.setVisible(true);
//        });
//    }
}
