package dev.totallyspies.spydle.frontend.interface_adaptors;

import javax.swing.*;
import java.awt.*;
import dev.totallyspies.spydle.frontend.views.GameOverView;
import dev.totallyspies.spydle.frontend.views.AllRoomView;
import dev.totallyspies.spydle.frontend.views.GameRoomView;
import dev.totallyspies.spydle.frontend.views.WelcomeScreenView;
import org.springframework.stereotype.Component;

@Component
public class GameView extends JFrame {
    // Define the CardLayout and panel container
    private CardLayout cardLayout;
    private JPanel panelContainer;

    public GameView() {
        // Set up the frame properties
        setTitle("SpyDle");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize the CardLayout and container panel
        cardLayout = new CardLayout();
        panelContainer = new JPanel(cardLayout);

        // Add the different view panels to the card layout
        panelContainer.add(new WelcomeScreenView(), "WelcomeScreenView");
        panelContainer.add(new AllRoomView(), "AllRoomView");
        panelContainer.add(new GameRoomView(), "GameRoomView");
        panelContainer.add(new GameOverView(), "GameOverView");

        // Add the panel container to the frame
        add(panelContainer);

        // Show the initial panel (WelcomeScreenView)
        cardLayout.show(panelContainer, "WelcomeScreenView");
    }

    // Method to switch between panels
    public void switchView(String viewName) {
        cardLayout.show(panelContainer, viewName);
    }

    public static void main(String[] args) {
        // Run the GameWindowFrame
        SwingUtilities.invokeLater(() -> {
            GameView frame = new GameView();
            frame.setVisible(true);
        });
    }
}
