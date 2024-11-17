package dev.totallyspies.spydle.frontend.interface_adaptors.game_view_adaptors;

import dev.totallyspies.spydle.frontend.views.ListRoomsView;
import dev.totallyspies.spydle.frontend.views.GameOverView;
import dev.totallyspies.spydle.frontend.views.GameRoomView;
import dev.totallyspies.spydle.frontend.views.WelcomeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

/*
Game View, which also acts as the window Frame
 */
@Component
public class GameViewModel extends JFrame {

    // Define the CardLayout and panel container
    private final CardLayout cardLayout;
    private final JPanel panelContainer;

    private final WelcomeView welcomeView;
    private final GameRoomView gameRoomView;
    private final ListRoomsView listRoomsView;
    private final GameOverView gameOverView;

    @Autowired // dependency injection
    public GameViewModel(WelcomeView welcomeView, GameRoomView gameRoomView,
                         ListRoomsView listRoomsView, GameOverView gameOverView) {
        this.welcomeView = welcomeView;
        this.gameRoomView = gameRoomView;
        this.listRoomsView = listRoomsView;
        this.gameOverView = gameOverView;

        // Set up the frame properties
        setTitle("SpyDle");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize the CardLayout and container panel
        cardLayout = new CardLayout();
        panelContainer = new JPanel(cardLayout);

        // Add the different view panels to the card layout
        panelContainer.add(this.welcomeView, "WelcomeView");
        panelContainer.add(this.listRoomsView, "AllRoomView");
        panelContainer.add(this.gameRoomView, "GameRoomView");
        panelContainer.add(this.gameOverView, "GameOverView");

        // Add the panel container to the frame
        add(panelContainer);

        // Show the initial panel (WelcomeView)
        cardLayout.show(panelContainer, "WelcomeView");
    }

    // Method to switch between panels
    @EventListener
    public void handleViewSwitch(SwitchViewEvent event) {
        cardLayout.show(panelContainer, event.getViewName());
    }

    public static void launchGameView(String[] args) {
        // Run the GameWindowFrame
        SwingUtilities.invokeLater(() -> {
            GameViewModel frame = new GameViewModel(new WelcomeView(), new GameRoomView(), new ListRoomsView(), new GameOverView());
            frame.setVisible(true);
        });
    }

}