package dev.totallyspies.spydle.frontend.views.game_room_panels;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class TimerPanel extends JPanel {
    private JLabel clockLabel;
    private int timeRemaining = 60; // Timer starts at 60 seconds
    private Timer timer;

    public TimerPanel() {
        // Panel styling
        setLayout(new BorderLayout());
        setBackground(new Color(195, 217, 255)); // Light blue background
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding

        // Clock label styling
        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Font style
        clockLabel.setForeground(new Color(138, 43, 226));    // Blue-violet text
        clockLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Initialize the timer display
        updateDisplay();

        // Add the clock label to the panel
        add(clockLabel, BorderLayout.CENTER);

        // Start the timer
        startTimer();
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (timeRemaining > 0) {
                    timeRemaining--;
                    updateDisplay();
                } else {
                    timer.cancel(); // Stop the timer when it reaches zero
                    clockLabel.setText("Time's up!");
                }
            }
        }, 0, 1000); // Run every 1000 ms (1 second)
    }

    private void updateDisplay() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        clockLabel.setText(String.format("Timer: %02d:%02d", minutes, seconds));
    }

    public void resetTimer() {
        timeRemaining = 60; // Reset to 60 seconds
        if (timer != null) {
            timer.cancel(); // Stop the current timer
        }
        startTimer(); // Restart the timer
    }
}
