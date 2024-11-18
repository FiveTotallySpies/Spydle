package dev.totallyspies.spydle.frontend.views;

import dev.totallyspies.spydle.frontend.interface_adapters.list_rooms.ListRoomsViewController;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@org.springframework.stereotype.Component
public class ListRoomsView extends JPanel {

    @Autowired
    private ListRoomsViewController controller;

    public ListRoomsView() {
        setLayout(new GridBagLayout()); // Center the container in the middle of the screen
        setBackground(new Color(195, 217, 255)); // Light blue background for the entire panel

        // Main container panel styling
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); // Padding around elements
        container.setBackground(new Color(195, 217, 255)); // Same background color

        // Title styling
        JLabel titleLabel = new JLabel("All Rooms");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(139, 0, 0)); // Dark red color
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

        // Updated size to fit a larger frame
        JScrollPane roomScrollPane = new JScrollPane(roomList);
        roomScrollPane.setPreferredSize(new Dimension(400, 300)); // Adjusted dimensions

        // Back button
        JButton backButton = new JButton("Back to Welcome");
        styleButton(backButton);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.openWelcomeView(); // Open the rooms page (AllRoomScreen.AllRoomsPage)
            }
        });

        // Adding components to container
        container.add(titleLabel);
        container.add(Box.createVerticalStrut(20));
        container.add(roomScrollPane);
        container.add(Box.createVerticalStrut(20));
        container.add(backButton);

        // Add container to the center of AllRoomView using GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(container, gbc);
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
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Spydle - All Rooms");
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.add(new ListRoomsView());
            frame.setVisible(true);
        });
    }

}
