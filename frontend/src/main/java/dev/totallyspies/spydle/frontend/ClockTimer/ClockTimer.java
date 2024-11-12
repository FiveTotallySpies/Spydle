package dev.totallyspies.spydle.frontend.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClockTimer extends JPanel {
    private int remainingTime = 30; // Time in seconds
    private Timer timer;
    private int angle; // Angle for the clock hand

    public ClockTimer() {
        setPreferredSize(new Dimension(200, 100));
        setBackground(new Color(195, 217, 255)); // Light blue background
        angle = 360; // Start with a full circle

        // Initialize and start the countdown timer
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (remainingTime > 0) {
                    remainingTime--;
                    angle = (int) (360 * (remainingTime / 30.0)); // Update angle
                    repaint();
                } else {
                    timer.stop();
                    JOptionPane.showMessageDialog(null, "Time's up!");
                }
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw clock face
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = 40;

        g2d.setColor(Color.WHITE);
        g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        g2d.setColor(Color.BLACK);
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Draw clock hand
        double radianAngle = Math.toRadians(90 - angle); // Convert angle to radians
        int handLength = 60;
        int handX = centerX + (int) (handLength * Math.cos(radianAngle));
        int handY = centerY - (int) (handLength * Math.sin(radianAngle));

        g2d.setColor(new Color(138, 43, 226)); // Blue-violet color for hand
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(centerX, centerY, handX, handY);

        // Draw remaining time as text
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(Color.BLACK);
        String timeText = "Time: " + remainingTime + "s";
        int textWidth = g2d.getFontMetrics().stringWidth(timeText);
        g2d.drawString(timeText, centerX - textWidth / 2, centerY + radius + 20);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Clock Timer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ClockTimer());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
