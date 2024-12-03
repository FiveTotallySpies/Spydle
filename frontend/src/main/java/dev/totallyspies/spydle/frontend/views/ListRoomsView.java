package dev.totallyspies.spydle.frontend.views;

import dev.totallyspies.spydle.frontend.interface_adapters.list_rooms.ListRoomsViewController;
import dev.totallyspies.spydle.frontend.interface_adapters.list_rooms.ListRoomsViewModel;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

@org.springframework.stereotype.Component
@Profile("!test")
public class ListRoomsView extends JPanel implements CardView {

    private final ListRoomsViewController controller;
    private final ListRoomsViewModel model;

    private JList<String> roomList;

    private JLabel imageLabel;

    public ListRoomsView(ListRoomsViewModel model, ListRoomsViewController controller) {
        this.controller = controller;
        this.model = model;

        setLayout(new GridBagLayout()); // Center the container in the middle of the screen
        setBackground(new Color(195, 217, 255)); // Light blue background for the entire panel

        // Main container panel styling
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); // Padding around elements
        container.setBackground(new Color(195, 217, 255)); // Same background color

        // Title styling
        JLabel titleLabel = new JLabel("All Rooms");
        try {
            Font customFont = Font.createFont(Font.TRUETYPE_FONT,
                    getClass().getResourceAsStream("/Sabrina.ttf"));
            customFont = customFont.deriveFont(Font.PLAIN, 35); // Adjust size and style
            titleLabel.setFont(customFont);
        } catch (Exception e) {
            e.printStackTrace();
            titleLabel.setFont(new Font("Arial", Font.BOLD, 35)); // Fallback to Arial
        }
        titleLabel.setForeground(new Color(139, 0, 0)); // Dark red color
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fake room data
        String[] roomData = {
                "Loading..."
        };

        model.setLinesInRoomList(roomData);

        // Create JList with fake data
        roomList = new JList<>(roomData);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomList.setFont(new Font("Arial", Font.PLAIN, 16));

        // Updated size to fit a larger frame
        JScrollPane roomScrollPane = new JScrollPane(roomList);
        roomScrollPane.setPreferredSize(new Dimension(400, 300)); // Adjusted dimensions

        // Load the uploaded image
        imageLabel = new JLabel();
        ImageIcon icon = new ImageIcon(getClass().getResource("/imagelistrooms2.png"));
        Image image = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH); // Scale image
        imageLabel.setIcon(new ImageIcon(image));

        // Back button
        JButton backButton = new JButton("Back to Welcome");
        styleButton(backButton);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.openWelcomeView(); // Open the rooms page (AllRoomScreen.AllRoomsPage)
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 20, 20, 20);

        // Container for list and image
        JPanel listAndImagePanel = new JPanel();
        listAndImagePanel.setLayout(new GridBagLayout());
        listAndImagePanel.setBackground(new Color(195, 217, 255));

        // Add image and room list side-by-side
        gbc.gridx = 0;
        listAndImagePanel.add(imageLabel, gbc);
        gbc.gridx = 1;
        listAndImagePanel.add(roomScrollPane, gbc);

        // Adding components to container
        container.add(titleLabel);
        container.add(Box.createVerticalStrut(20));
        container.add(listAndImagePanel);
        // container.add(roomScrollPane);
        container.add(Box.createVerticalStrut(20));
        container.add(backButton);

        // Add container to the center of AllRoomView using GridBagConstraints
        // GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        // gbc.anchor = GridBagConstraints.CENTER;
        add(container, gbc);
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        controller.updateRoomList();
    }

    @EventListener
    public void onSwitchView(SwitchViewEvent event) {
        if (event.getViewClass().equals(ListRoomsView.class)) {
            controller.updateRoomList();
        }
    }

    private void styleButton(JButton button) {

        button.setBackground(new Color(25, 25, 112)); // dark blue
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(400, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Set a consistent, fixed padding around the button content
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(25, 25, 112), 1), // Outer border color
                BorderFactory.createEmptyBorder(10, 10, 10, 10)  // Inner padding to avoid layout shift
        ));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(new Color(25, 25, 112)); // blueviolet
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(25, 25, 112));
                button.setForeground(Color.WHITE);
            }
        });
    }


    public void updateRoomList() {
        roomList.setListData(model.getLinesInRoomList());
    }



}
