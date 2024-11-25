package dev.totallyspies.spydle.frontend.interface_adapters.view_manager;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.frontend.views.GameEndView;
import dev.totallyspies.spydle.frontend.views.GameRoomView;
import dev.totallyspies.spydle.frontend.views.ListRoomsView;
import dev.totallyspies.spydle.frontend.views.WelcomeView;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import org.springframework.web.socket.CloseStatus;

/*
Game View, which also acts as the window Frame
 */
@Component
@Profile("!local")
public class ViewManagerModel extends JFrame {

    // Define the CardLayout and panel container
    private final CardLayout cardLayout;
    private String currentCard;
    private final JPanel panelContainer;

    private final WelcomeView welcomeView;
    private final GameRoomView gameRoomView;
    private final ListRoomsView listRoomsView;
    private final GameEndView gameEndView;

    private final ApplicationEventPublisher publisher;

    public ViewManagerModel(WelcomeView welcomeView, GameRoomView gameRoomView,
                            ListRoomsView listRoomsView, GameEndView gameEndView,
                            ApplicationEventPublisher publisher) {
        this.welcomeView = welcomeView;
        this.gameRoomView = gameRoomView;
        this.listRoomsView = listRoomsView;
        this.gameEndView = gameEndView;

        this.publisher = publisher;

        // Set up the frame properties
        setTitle("Spydle");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize the CardLayout and container panel
        cardLayout = new CardLayout();
        panelContainer = new JPanel(cardLayout);

        // Add the different view panels to the card layout
        panelContainer.add(this.welcomeView, "WelcomeView");
        panelContainer.add(this.listRoomsView, "ListRoomsView");
        panelContainer.add(this.gameRoomView, "GameRoomView");
        panelContainer.add(this.gameEndView, "GameEndView");

        // Add the panel container to the frame
        add(panelContainer);

        // Show the initial panel (WelcomeView)
        // this is the first page that will run!
        currentCard = "WelcomeView";
        cardLayout.show(panelContainer, currentCard);
    }

    // Method to switch between panels
    @EventListener
    public void handleViewSwitch(SwitchViewEvent event) {
        currentCard = event.getViewClass().getName();
        cardLayout.show(panelContainer, currentCard);
    }

    @EventListener
    public void handleViewError(ErrorViewEvent event) {
        JOptionPane.showMessageDialog(this, event.getMessage());
    }

    @EventListener
    public void onSocketFail(ClientSocketHandler.CloseEvent event) {
        if (currentCard.equals("GameRoomView") && event.getStatus().getCode() != CloseStatus.NORMAL.getCode()) {
            publisher.publishEvent(new SwitchViewEvent(this, WelcomeView.class));
            JOptionPane.showMessageDialog(this, "Client socket failed: status " + event.getStatus());
        }
    }

//    public static void launchGameView(String[] args) {
//        // Run the GameWindowFrame
//        SwingUtilities.invokeLater(() -> {
//            ViewManagerModel frame = new ViewManagerModel(new WelcomeView(), new GameRoomView(), new ListRoomsView(), new GameEndView());
//            frame.setVisible(true);
//        });
//    }

}
