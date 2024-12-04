package dev.totallyspies.spydle.frontend.views;

import dev.totallyspies.spydle.frontend.interface_adapters.list_rooms.ListRoomsViewController;
import dev.totallyspies.spydle.frontend.interface_adapters.list_rooms.ListRoomsViewModel;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

/**
 * The ListRoomsView class represents the user interface where players can view the available
 * game rooms and interact with them. It provides a list of rooms, a title, an image, and a
 * back button to navigate to the previous view.
 *
 * This view is responsible for displaying the available game rooms in a list, handling user
 * interactions such as navigating back to the welcome screen, and updating the room list dynamically
 * based on the data provided by the controller and model.
 *
 * The ListRoomsView listens for application events such as the application being ready and view
 * switch events to ensure that the room list is always up-to-date.
 *
 * Some key features include:
 *   Displaying a list of available rooms using a {@link JList} component.
 *   Displaying an image and title at the top of the view.
 *   Handling navigation to the welcome screen via a "Back to Welcome" button.
 *   Updating the room list dynamically when the view is switched to.
 *
 * @see ListRoomsViewController The controller responsible for managing the state of the room list.
 * @see ListRoomsViewModel The model that holds the data about the available rooms.
 */
@org.springframework.stereotype.Component
@Profile("!test")
public class ListRoomsView extends JPanel implements CardView {

  private final ListRoomsViewController controller;
  private final ListRoomsViewModel model;

  private final JList<String> roomList;
  private final JLabel imageLabel;

  /**
   * Constructs a new ListRoomsView with the specified model and controller.
   *
   * @param model The view model that holds the data for the room list.
   * @param controller The controller that manages the interaction between the view and the model.
   */
  public ListRoomsView(ListRoomsViewModel model, ListRoomsViewController controller) {
    this.controller = controller;
    this.model = model;

    setLayout(new GridBagLayout());
    setBackground(new Color(195, 217, 255)); // Light blue background for the entire panel

    // Main container panel styling
    JPanel container = new JPanel();
    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
    container.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); // Padding around elements
    container.setBackground(new Color(195, 217, 255));

    // Title styling
    JLabel titleLabel = new JLabel("All Rooms");
    try {
      Font customFont =
              Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/Sabrina.ttf"));
      customFont = customFont.deriveFont(Font.PLAIN, 35); // Adjust size and style
      titleLabel.setFont(customFont);
    } catch (Exception e) {
      e.printStackTrace();
      titleLabel.setFont(new Font("Arial", Font.BOLD, 35)); // Fallback to Arial
    }
    titleLabel.setForeground(new Color(139, 0, 0)); // Dark red color
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    // Fake room data
    String[] roomData = {"Loading..."};

    model.setLinesInRoomList(roomData);

    // Create JList with fake data
    roomList = new JList<>(roomData);
    roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    roomList.setFont(new Font("Arial", Font.PLAIN, 16));

    JScrollPane roomScrollPane = new JScrollPane(roomList);
    roomScrollPane.setPreferredSize(new Dimension(400, 300));

    // Load the uploaded image
    imageLabel = new JLabel();
    ImageIcon icon = new ImageIcon(getClass().getResource("/imagelistrooms2.png"));
    Image image = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
    imageLabel.setIcon(new ImageIcon(image));

    // Back button
    JButton backButton = new JButton("Back to Welcome");
    styleButton(backButton);
    backButton.addActionListener(
            new ActionListener() {
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
    container.add(Box.createVerticalStrut(20));
    container.add(backButton);

    add(container, gbc);
  }

  /**
   * Event listener that is triggered when the application is ready.
   * This will update the room list as soon as the application is fully initialized.
   *
   * @param event The application ready event.
   */
  @EventListener
  public void onApplicationReady(ApplicationReadyEvent event) {
    controller.updateRoomList();
  }

  /**
   * Event listener for switching views. Updates the room list when the ListRoomsView is displayed.
   *
   * @param event The view switch event.
   */
  @EventListener
  public void onSwitchView(SwitchViewEvent event) {
    if (event.getViewClass().equals(ListRoomsView.class)) {
      controller.updateRoomList();
    }
  }

  /**
   * Updates the room list in the view using the current data from the model.
   * This method is called to refresh the list of rooms whenever the room list is updated.
   */
  public void updateRoomList() {
    roomList.setListData(model.getLinesInRoomList());
  }

  /**
   * Styles the given button by setting background color, text color, font, and adding hover effects.
   *
   * @param button The button to style.
   */
  private void styleButton(JButton button) {
    button.setBackground(new Color(25, 25, 112)); // blueviolet
    button.setForeground(Color.WHITE);
    button.setFocusPainted(false);
    button.setFont(new Font("Arial", Font.BOLD, 12));
    button.setAlignmentX(Component.CENTER_ALIGNMENT);
    button.setMaximumSize(new Dimension(400, 40));
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));

    // Set a consistent, fixed padding around the button content
    button.setBorder(
            BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(25, 25, 112), 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

    // Hover effect
    button.addMouseListener(
            new java.awt.event.MouseAdapter() {
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
}
