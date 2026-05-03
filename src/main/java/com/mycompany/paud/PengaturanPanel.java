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
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class PengaturanPanel extends JPanel {
    private final DataStore ds = DataStore.getInstance();
    private JTextField tfNamaSekolah;
    private JTextField tfJamMasuk;
    private JTextField tfJamPulang;

    // Menjalankan inisialisasi objek PengaturanPanel.
    public PengaturanPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("PENGATURAN");
        title.setFont(Theme.FONT_TITLE);
        add(title, BorderLayout.NORTH);

        RoundedPanel card = Theme.makeCard(16);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        tfNamaSekolah = new JTextField(ds.getNamaSekolah());
        styleField(tfNamaSekolah);
        card.add(settingRow("Nama Sekolah:", tfNamaSekolah));
        card.add(Box.createVerticalStrut(10));
        tfJamMasuk = new JTextField(ds.getJamMasuk());
        styleField(tfJamMasuk);
        card.add(settingRow("Jam Masuk:", tfJamMasuk));
        card.add(Box.createVerticalStrut(14));

        tfJamPulang = new JTextField(ds.getJamPulang());
        styleField(tfJamPulang);
        card.add(settingRow("Jam Pulang:", tfJamPulang));
        card.add(Box.createVerticalStrut(10));

        JButton btnSimpan = makeBtn("Simpan Pengaturan", Theme.BLUE_BTN, e -> simpanPengaturan());
        btnSimpan.setPreferredSize(new Dimension(200, 34));
        btnSimpan.setMaximumSize(new Dimension(220, 34));
        btnSimpan.setAlignmentX(LEFT_ALIGNMENT);
        card.add(btnSimpan);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(16, 0, 0, 0));
        wrap.add(card, BorderLayout.NORTH);
        add(wrap, BorderLayout.CENTER);
    }

    // Menangani proses: setting row.
    private JPanel settingRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_HEADER);
        lbl.setPreferredSize(new Dimension(160, 30));

        row.add(lbl, BorderLayout.WEST);
        row.add(field,  BorderLayout.CENTER);
        return row;
    }

    // Menangani proses: style field.
    private void styleField(JTextField tf) {
        tf.setFont(Theme.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(2, 8, 2, 8)));
        tf.setPreferredSize(new Dimension(250, 32));
    }

    // Menangani proses: make btn.
    private JButton makeBtn(String text, Color bg, ActionListener al) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? bg.darker() : bg;
                g2.setColor(c);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(Theme.FONT_BODY);
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(al);
        return b;
    }

    // Menangani proses: simpan pengaturan.
    private void simpanPengaturan() {
        String nama = tfNamaSekolah == null ? "" : tfNamaSekolah.getText().trim();
        String jamMasuk = tfJamMasuk == null ? "" : tfJamMasuk.getText().trim();
        String jamPulang = tfJamPulang == null ? "" : tfJamPulang.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama sekolah tidak boleh kosong.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!jamMasuk.matches("\\d{2}:\\d{2}(\\s*WIB)?")) {
            JOptionPane.showMessageDialog(this, "Format Jam Masuk harus HH:mm atau HH:mm WIB.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!jamPulang.matches("\\d{2}:\\d{2}(\\s*WIB)?")) {
            JOptionPane.showMessageDialog(this, "Format Jam Pulang harus HH:mm atau HH:mm WIB.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!jamMasuk.toUpperCase().contains("WIB")) jamMasuk = jamMasuk + " WIB";
        if (!jamPulang.toUpperCase().contains("WIB")) jamPulang = jamPulang + " WIB";

        boolean okNama = ds.setNamaSekolah(nama);
        boolean okMasuk = ds.setJamMasuk(jamMasuk);
        boolean okPulang = ds.setJamPulang(jamPulang);
        if (!okNama || !okMasuk || !okPulang) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan pengaturan.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tfJamMasuk.setText(jamMasuk);
        tfJamPulang.setText(jamPulang);
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof MainFrame) ((MainFrame) w).refreshSchoolBranding();
        JOptionPane.showMessageDialog(this, "Pengaturan berhasil disimpan.", "Berhasil", JOptionPane.INFORMATION_MESSAGE);
    }
}
