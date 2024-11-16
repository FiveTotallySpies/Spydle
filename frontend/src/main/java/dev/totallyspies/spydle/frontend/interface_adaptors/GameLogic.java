package dev.totallyspies.spydle.frontend.interface_adaptors;

import dev.totallyspies.spydle.frontend.views.GameOverView;
import dev.totallyspies.spydle.frontend.views.WelcomeScreenView;
import org.springframework.stereotype.Component;

import javax.swing.*;


// this is basically a presenter.
@Component
public class GameLogic {
    private JFrame frame;
    private WelcomeScreenView welcomeScreen;
    private GameOverView gameOverScreen;

    public GameLogic() {
        // Initialize the main frame that will hold different screens
        frame = new JFrame("Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);
    }

    public void startGame() {
        // Show the welcome screen
        welcomeScreen = new WelcomeScreenView();
        frame.getContentPane().removeAll(); // Clear existing components
        frame.add(welcomeScreen); // Add the welcome screen panel
        frame.revalidate();
        frame.repaint();

        // Simulate a delay to show the welcome screen before moving to the game
        try {
            Thread.sleep(2000); // 2-second delay for demonstration
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Move to the main game screen after the welcome screen
        frame.getContentPane().removeAll(); // Clear welcome screen components
        JLabel gameLabel = new JLabel("Game is running...");
        gameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gameLabel.setBounds(50, 150, 300, 50);
        frame.add(gameLabel); // Add the main game label
        frame.revalidate();
        frame.repaint();

        // Simulate gameplay delay
        try {
            Thread.sleep(2000); // 2-second delay for gameplay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void gameOver() {
        // Show the Game Over screen
        gameOverScreen = new GameOverView();
        frame.getContentPane().removeAll(); // Clear existing components
        frame.add(gameOverScreen.getContentPane()); // Add GameOverScreen content to main frame
        frame.revalidate();
        frame.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameLogic gameLogic = new GameLogic();
            gameLogic.frame.setVisible(true);

            // Start the game sequence: Welcome -> Game -> Game Over
            // gameLogic.startGame();
            gameLogic.gameOver();
        });
    }
}
