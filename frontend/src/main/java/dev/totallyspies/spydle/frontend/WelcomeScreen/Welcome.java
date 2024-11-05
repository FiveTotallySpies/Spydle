package dev.totallyspies.spydle.frontend.WelcomeScreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Welcome extends JFrame {

    public Welcome() {
        setTitle("Welcome - Join or Create Room");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main container
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Spydle");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(titleLabel);

        // Form to create room
        JPanel createRoomPanel = new JPanel();
        createRoomPanel.setLayout(new FlowLayout());
        JLabel createRoomLabel = new JLabel("Create Room:");
        JTextField nicknameField = new JTextField(15);
        nicknameField.setToolTipText("Enter your nickname");
        JButton createRoomButton = new JButton("Create Room");

        createRoomPanel.add(createRoomLabel);
        createRoomPanel.add(nicknameField);
        createRoomPanel.add(createRoomButton);

        // Event for Create Room button
        createRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nickname = nicknameField.getText();
                if (!nickname.isEmpty()) {
                    // Handle create room action
                    JOptionPane.showMessageDialog(container, "Room created with nickname: " + nickname);
                } else {
                    JOptionPane.showMessageDialog(container, "Please enter your nickname.");
                }
            }
        });

        // Form to join existing room
        JPanel joinRoomPanel = new JPanel();
        joinRoomPanel.setLayout(new FlowLayout());
        JLabel joinRoomLabel = new JLabel("Join an existing room:");
        JTextField joinNicknameField = new JTextField(15);
        joinNicknameField.setToolTipText("Enter your nickname");
        JTextField roomCodeField = new JTextField(8);
        roomCodeField.setToolTipText("Room code");
        JButton joinRoomButton = new JButton("Join Room");

        joinRoomPanel.add(joinRoomLabel);
        joinRoomPanel.add(joinNicknameField);
        joinRoomPanel.add(roomCodeField);
        joinRoomPanel.add(joinRoomButton);

        // Event for Join Room button
        joinRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nickname = joinNicknameField.getText();
                String roomCode = roomCodeField.getText();
                if (!nickname.isEmpty() && !roomCode.isEmpty()) {
                    // Handle join room action
                    JOptionPane.showMessageDialog(container, "Joining room: " + roomCode + " with nickname: " + nickname);
                } else {
                    JOptionPane.showMessageDialog(container, "Please enter your nickname and room code.");
                }
            }
        });

        container.add(Box.createVerticalStrut(10));
        container.add(createRoomPanel);
        container.add(Box.createVerticalStrut(10));
        container.add(joinRoomPanel);

        add(container);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Welcome::new);
    }
}