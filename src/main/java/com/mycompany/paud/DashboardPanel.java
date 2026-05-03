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
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class DashboardPanel extends JPanel {
    private DonutChart chartHadir, chartIzin, chartSakit;
    private DataStore ds = DataStore.getInstance();

    // Menjalankan inisialisasi objek DashboardPanel.
    public DashboardPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        add(buildRingkasanPanel(), BorderLayout.NORTH);
        add(Box.createVerticalGlue(), BorderLayout.CENTER);
    }

    // Menangani proses: build ringkasan panel.
    private JPanel buildRingkasanPanel() {
        // Gradient card
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            // Menangani proses: paint component.
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(130, 200, 255),
                        getWidth(), getHeight(), new Color(255, 160, 100));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("RINGKASAN KEHADIRAN HARI INI");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));
        wrapper.add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);

        chartHadir = new DonutChart(75, Theme.GREEN, "75%", "15/20 anak");
        chartIzin  = new DonutChart(15, Theme.ORANGE, "15%", "3/20 anak");
        chartSakit = new DonutChart(10, Theme.RED,  "10%", "2/20 anak");

        cards.add(buildStatCard("HADIR",      chartHadir));
        cards.add(buildStatCard("IZIN",       chartIzin));
        cards.add(buildStatCard("SAKIT/ALPA", chartSakit));

        wrapper.add(cards, BorderLayout.CENTER);
        return wrapper;
    }

    // Menangani proses: build stat card.
    private JPanel buildStatCard(String label, DonutChart chart) {
        RoundedPanel card = Theme.makeCard(16);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(new EmptyBorder(14, 12, 14, 12));

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(Theme.FONT_HEADER);
        lbl.setForeground(Theme.TEXT_DARK);

        JPanel chartWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        chartWrap.setOpaque(false);
        chartWrap.add(chart);

        card.add(lbl, BorderLayout.NORTH);
        card.add(chartWrap, BorderLayout.CENTER);
        return card;
    }

    // Menyegarkan tampilan agar sesuai data terbaru.
    public void refresh() {
        int total = ds.getTotalSiswa();
        if (total == 0) return;
        long hadir = ds.getCountByStatus("Hadir");
        long izin  = ds.getCountByStatus("Izin");
        long sakit = ds.getCountByStatus("Sakit") + ds.getCountByStatus("Alpa");
        double pH = (hadir * 100.0) / total;
        double pI = (izin  * 100.0) / total;
        double pS = (sakit * 100.0) / total;
        chartHadir.setPercentage(pH, String.format("%.0f%%", pH), hadir + "/" + total + " anak");
        chartIzin.setPercentage(pI,  String.format("%.0f%%", pI), izin  + "/" + total + " anak");
        chartSakit.setPercentage(pS, String.format("%.0f%%", pS), sakit + "/" + total + " anak");
    }
}
