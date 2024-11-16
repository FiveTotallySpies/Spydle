package dev.totallyspies.spydle.frontend.views.game_room_panels;

import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

// Arrow Panel (Used to indicate the current player, centered initially)
@Component
class ArrowPanel extends JPanel {
    public ArrowPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.RED);
        int[] xPoints = { 10, 0, 20 };
        int[] yPoints = { 0, 30, 30 };
        g2d.fillPolygon(xPoints, yPoints, 3);
    }
}