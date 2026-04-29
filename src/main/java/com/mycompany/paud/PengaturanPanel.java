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

public class PengaturanPanel extends JPanel {
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

        card.add(settingRow("Nama Sekolah:",   "PAUD Tunas Harapan"));
        card.add(Box.createVerticalStrut(14));
        card.add(settingRow("Tahun Ajaran:",   "2023/2024"));
        card.add(Box.createVerticalStrut(14));
        card.add(settingRow("Wali Kelas A:",   "Bu Siti"));
        card.add(Box.createVerticalStrut(14));
        card.add(settingRow("Wali Kelas B:",   "Bu Dewi"));
        card.add(Box.createVerticalStrut(14));
        card.add(settingRow("Jam Masuk:",      "07:30 WIB"));
        card.add(Box.createVerticalStrut(14));
        card.add(settingRow("Jam Pulang:",     "10:30 WIB"));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(16, 0, 0, 0));
        wrap.add(card, BorderLayout.NORTH);
        add(wrap, BorderLayout.CENTER);
    }

    private JPanel settingRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_HEADER);
        lbl.setPreferredSize(new Dimension(160, 30));

        JTextField tf = new JTextField(value);
        tf.setFont(Theme.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(2, 8, 2, 8)));
        tf.setPreferredSize(new Dimension(250, 32));

        row.add(lbl, BorderLayout.WEST);
        row.add(tf,  BorderLayout.CENTER);
        return row;
    }
}
