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
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class RegisterFrame extends JDialog {

    private JTextField     tfNama, tfUsername;
    private JPasswordField tfPassword, tfConfirm;
    private JComboBox<String> cbKelompok;
    private JLabel         lblError;
    private JFrame         parent;

    public RegisterFrame(JFrame parent) {
        super(parent, "Daftar Akun Guru Baru", true);
        this.parent = parent;
        setSize(420, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        setContentPane(buildContent());
    }

    private JPanel buildContent() {
        JPanel bg = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,
                    new Color(200,235,255),0,getHeight(),new Color(215,255,215));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };

        RoundedPanel card = new RoundedPanel(24, Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 36, 30, 36));
        card.setPreferredSize(new Dimension(340, 490));

        JLabel title = new JLabel("DAFTAR AKUN GURU", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(Theme.TEXT_DARK);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Buat akun baru untuk guru", SwingConstants.CENTER);
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_GRAY);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        lblError = new JLabel("", SwingConstants.CENTER);
        lblError.setFont(Theme.FONT_SMALL);
        lblError.setForeground(Theme.RED);
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        tfNama     = inputField("Nama lengkap guru");
        tfUsername = inputField("Username (tidak bisa diubah)");
        tfPassword = passField("Password (min. 6 karakter)");
        tfConfirm  = passField("Ulangi password");
        cbKelompok = new JComboBox<>(new String[]{"A", "B", "Semua"});
        cbKelompok.setFont(Theme.FONT_BODY);
        cbKelompok.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JButton btnDaftar = makeRoundBtn("DAFTAR", Theme.GREEN);
        btnDaftar.addActionListener(e -> doRegister());

        JButton btnBatal = makeRoundBtn("Batal", Theme.GRAY_BTN);
        btnBatal.addActionListener(e -> dispose());

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(16));
        card.add(fieldLabel("Nama Lengkap"));
        card.add(Box.createVerticalStrut(4));
        card.add(wrapFull(tfNama));
        card.add(Box.createVerticalStrut(10));
        card.add(fieldLabel("Username"));
        card.add(Box.createVerticalStrut(4));
        card.add(wrapFull(tfUsername));
        card.add(Box.createVerticalStrut(10));
        card.add(fieldLabel("Password"));
        card.add(Box.createVerticalStrut(4));
        card.add(wrapFull(tfPassword));
        card.add(Box.createVerticalStrut(10));
        card.add(fieldLabel("Konfirmasi Password"));
        card.add(Box.createVerticalStrut(4));
        card.add(wrapFull(tfConfirm));
        card.add(Box.createVerticalStrut(10));
        card.add(fieldLabel("Kelompok yang Diampu"));
        card.add(Box.createVerticalStrut(4));
        card.add(wrapFull(cbKelompok));
        card.add(Box.createVerticalStrut(8));
        card.add(lblError);
        card.add(Box.createVerticalStrut(16));
        card.add(wrapFull(btnDaftar));
        card.add(Box.createVerticalStrut(8));
        card.add(wrapFull(btnBatal));

        bg.add(card);
        return bg;
    }

    private void doRegister() {
        String nama     = tfNama.getText().trim();
        String username = tfUsername.getText().trim();
        String pass     = new String(tfPassword.getPassword()).trim();
        String confirm  = new String(tfConfirm.getPassword()).trim();
        String kelompok = (String) cbKelompok.getSelectedItem();

        // Bersihkan nilai placeholder agar tidak lolos validasi
        if (nama.equals("Nama lengkap guru"))             nama     = "";
        if (username.equals("Username (tidak bisa diubah)")) username = "";
        if (pass.equals("Password (min. 6 karakter)"))    pass     = "";
        if (confirm.equals("Ulangi password"))            confirm  = "";

        // Validasi kosong
        if (nama.isEmpty() || username.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            lblError.setText("Semua field harus diisi!"); return;
        }
        if (username.length() < 4) {
            lblError.setText("Username minimal 4 karakter!"); return;
        }
        if (pass.length() < 6) {
            lblError.setText("Password minimal 6 karakter!"); return;
        }
        if (!pass.equals(confirm)) {
            lblError.setText("Password tidak cocok!"); return;
        }
        if (!username.matches("[a-zA-Z0-9_]+")) {
            lblError.setText("Username hanya boleh huruf, angka, dan _"); return;
        }

        boolean ok = DataStore.getInstance().registerGuru(username, pass, nama, kelompok);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                "Akun berhasil dibuat!\nSilakan login dengan username: " + username,
                "Registrasi Berhasil", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            lblError.setText("Username '" + username + "' sudah digunakan!");
        }
    }

    // ---- Helpers ----
    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_HEADER);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField inputField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(Theme.FONT_BODY);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200),1,true),
            new EmptyBorder(4,10,4,10)));
        tf.setForeground(Theme.TEXT_GRAY);
        tf.setText(placeholder);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(Theme.TEXT_DARK); }
            }
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) { tf.setText(placeholder); tf.setForeground(Theme.TEXT_GRAY); }
            }
        });
        return tf;
    }

    private JPasswordField passField(String placeholder) {
        JPasswordField pf = new JPasswordField();
        pf.setFont(Theme.FONT_BODY);
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200),1,true),
            new EmptyBorder(4,10,4,10)));
        pf.setEchoChar((char)0);
        pf.setForeground(Theme.TEXT_GRAY);
        pf.setText(placeholder);
        pf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (String.valueOf(pf.getPassword()).equals(placeholder)) {
                    pf.setText(""); pf.setEchoChar('●'); pf.setForeground(Theme.TEXT_DARK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (pf.getPassword().length == 0) {
                    pf.setEchoChar((char)0); pf.setText(placeholder); pf.setForeground(Theme.TEXT_GRAY);
                }
            }
        });
        return pf;
    }

    private JButton makeRoundBtn(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() : bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setAlignmentX(CENTER_ALIGNMENT);
        return b;
    }

    private JPanel wrapFull(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(CENTER_ALIGNMENT);
        p.add(c);
        return p;
    }
}

