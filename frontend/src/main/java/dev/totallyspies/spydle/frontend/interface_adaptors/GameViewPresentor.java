package dev.totallyspies.spydle.frontend.interface_adaptors;

import dev.totallyspies.spydle.frontend.views.AllRoomView;
import dev.totallyspies.spydle.frontend.views.GameOverView;
import dev.totallyspies.spydle.frontend.views.GameRoomView;
import dev.totallyspies.spydle.frontend.views.WelcomeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class GameViewPresentor extends JFrame {
    // Define the CardLayout and panel container
    private CardLayout cardLayout;
    private JPanel panelContainer;

    private final WelcomeView welcomeView;
    private final GameRoomView gameRoomView;
    private final AllRoomView allRoomView;
    private final GameOverView gameOverView;

    @Autowired // dependency injection
    public GameViewPresentor(WelcomeView welcomeView, GameRoomView gameRoomView,
                             AllRoomView allRoomView, GameOverView gameOverView) {
        this.welcomeView = welcomeView;
        this.gameRoomView = gameRoomView;
        this.allRoomView = allRoomView;
        this.gameOverView = gameOverView;

        // Set up the frame properties
        setTitle("SpyDle");
        setSize(500,500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize the CardLayout and container panel
        cardLayout = new CardLayout();
        panelContainer = new JPanel(cardLayout);

        // Add the different view panels to the card layout
        panelContainer.add(this.welcomeView, "WelcomeScreenView");
        panelContainer.add(this.allRoomView, "AllRoomView");
        panelContainer.add(this.gameRoomView, "GameRoomView");
        panelContainer.add(this.gameOverView, "GameOverView");

        // Add the panel container to the frame
        add(panelContainer);

        // Show the initial panel (WelcomeScreenView)
        cardLayout.show(panelContainer, "AllRoomView");
    }

    // Method to switch between panels
    public void switchView(String viewName) {
        cardLayout.show(panelContainer, viewName);
    }

    public static void main(String[] args) {
        // Run the GameWindowFrame
        SwingUtilities.invokeLater(() -> {
            GameViewPresentor frame = new GameViewPresentor();
            frame.setVisible(true);
        });
    }
}
