package dev.totallyspies.spydle.frontend.GamePanel;

import javax.swing.*;

// Main Game Frame
public class GameFrame extends JFrame {
    private GamePanel gamePanel;

    public GameFrame() {
        setTitle("SPYDLE Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        gamePanel = new GamePanel(4); // Change the number of players as needed
        add(gamePanel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}



