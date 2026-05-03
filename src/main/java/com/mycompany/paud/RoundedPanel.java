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

public class RoundedPanel extends JPanel {
    private int arc;
    protected Color bg;

    // Menjalankan inisialisasi objek RoundedPanel.
    public RoundedPanel(int arc, Color bg) {
        this.arc = arc;
        this.bg = bg;
        setOpaque(false);
    }

    @Override
    // Menangani proses: paint component.
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg != null ? bg : getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}
