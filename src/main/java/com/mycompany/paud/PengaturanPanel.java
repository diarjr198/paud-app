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
        JButton btnSimpan = makeBtn("Simpan Nama Sekolah", Theme.BLUE_BTN, e -> simpanNamaSekolah());
        btnSimpan.setPreferredSize(new Dimension(200, 34));
        btnSimpan.setMaximumSize(new Dimension(220, 34));
        btnSimpan.setAlignmentX(LEFT_ALIGNMENT);
        card.add(btnSimpan);
        card.add(Box.createVerticalStrut(14));
        card.add(settingRow("Tahun Ajaran:",   readOnlyField("2023/2024")));
        card.add(Box.createVerticalStrut(14));
        card.add(settingRow("Wali Kelas A:",   readOnlyField("Bu Siti")));
        card.add(Box.createVerticalStrut(14));
        card.add(settingRow("Wali Kelas B:",   readOnlyField("Bu Dewi")));
        card.add(Box.createVerticalStrut(14));
        card.add(settingRow("Jam Masuk:",      readOnlyField("07:30 WIB")));
        card.add(Box.createVerticalStrut(14));
        card.add(settingRow("Jam Pulang:",     readOnlyField("10:30 WIB")));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(16, 0, 0, 0));
        wrap.add(card, BorderLayout.NORTH);
        add(wrap, BorderLayout.CENTER);
    }

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

    private JTextField readOnlyField(String value) {
        JTextField tf = new JTextField(value);
        styleField(tf);
        tf.setEditable(false);
        tf.setBackground(new Color(248, 248, 248));
        return tf;
    }

    private void styleField(JTextField tf) {
        tf.setFont(Theme.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(2, 8, 2, 8)));
        tf.setPreferredSize(new Dimension(250, 32));
    }

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

    private void simpanNamaSekolah() {
        String nama = tfNamaSekolah == null ? "" : tfNamaSekolah.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama sekolah tidak boleh kosong.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean ok = ds.setNamaSekolah(nama);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan nama sekolah.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof MainFrame) ((MainFrame) w).refreshSchoolBranding();
        JOptionPane.showMessageDialog(this, "Nama sekolah berhasil disimpan.", "Berhasil", JOptionPane.INFORMATION_MESSAGE);
    }
}
