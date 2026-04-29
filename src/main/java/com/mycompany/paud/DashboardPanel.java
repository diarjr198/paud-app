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

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        add(buildRingkasanPanel(), BorderLayout.NORTH);
        add(buildPengumumanPanel(), BorderLayout.CENTER);
    }

    private JPanel buildRingkasanPanel() {
        // Gradient card
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
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

    private JPanel buildPengumumanPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 10));
        outer.setOpaque(false);

        JLabel title = new JLabel("PENGUMUMAN & KEGIATAN");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);
        outer.add(title, BorderLayout.NORTH);

        JPanel items = new JPanel(new GridLayout(1, 2, 12, 0));
        items.setOpaque(false);

        items.add(buildAnnouncementCard("🏫", "Outing Class Besok", "15/20 anak", new Color(220, 240, 255)));
        items.add(buildAnnouncementCard("🎂", "Ulang Tahun Ani", "17 Mo anak", new Color(255, 235, 210)));

        outer.add(items, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildAnnouncementCard(String icon, String title, String sub, Color bg) {
        RoundedPanel card = new RoundedPanel(14, bg);
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 14));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_HEADER);
        JLabel s = new JLabel(sub);
        s.setFont(Theme.FONT_BODY);
        s.setForeground(Theme.TEXT_GRAY);
        text.add(t);
        text.add(s);

        card.add(iconLbl);
        card.add(text);
        return card;
    }

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
