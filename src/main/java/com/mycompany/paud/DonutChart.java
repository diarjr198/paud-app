/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.paud;

/**
 *
 * @author diarjr198
 */
import javax.swing.*;
import java.awt.*;

public class DonutChart extends JPanel {
    private double percentage;
    private Color arcColor;
    private String label;
    private String sublabel;

    // Menjalankan inisialisasi objek DonutChart.
    public DonutChart(double percentage, Color arcColor, String label, String sublabel) {
        this.percentage = percentage;
        this.arcColor = arcColor;
        this.label = label;
        this.sublabel = sublabel;
        setOpaque(false);
        setPreferredSize(new Dimension(130, 130));
    }

    // Menangani proses: set percentage.
    public void setPercentage(double pct, String lbl, String sub) {
        this.percentage = pct;
        this.label = lbl;
        this.sublabel = sub;
        repaint();
    }

    @Override
    // Menangani proses: paint component.
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = getWidth() / 2, cy = getHeight() / 2;
        int r = Math.min(getWidth(), getHeight()) / 2 - 10;
        int stroke = 14;

        // Background arc
        g2.setColor(new Color(220, 220, 220));
        g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(cx - r, cy - r, 2 * r, 2 * r, 90, -360);

        // Foreground arc
        int sweep = (int) (percentage / 100.0 * 360);
        g2.setColor(arcColor);
        g2.drawArc(cx - r, cy - r, 2 * r, 2 * r, 90, -sweep);

        // Center text
        g2.setColor(Theme.TEXT_DARK);
        g2.setFont(Theme.FONT_BIG_PCT);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(label);
        g2.drawString(label, cx - tw / 2, cy + fm.getAscent() / 2 - 6);

        g2.setFont(Theme.FONT_SMALL);
        fm = g2.getFontMetrics();
        tw = fm.stringWidth(sublabel);
        g2.setColor(Theme.TEXT_GRAY);
        g2.drawString(sublabel, cx - tw / 2, cy + fm.getAscent() / 2 + 12);

        g2.dispose();
    }
}
