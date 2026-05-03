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
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public class LoginFrame extends JFrame {

    private JTextField    tfUsername;
    private JPasswordField tfPassword;
    private JLabel        lblError;

    // Menjalankan inisialisasi objek LoginFrame.
    public LoginFrame() {
        setTitle("Login - Sistem Absensi PAUD " + DataStore.getInstance().getNamaSekolah());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setContentPane(buildContent());
    }

    // Menyusun komponen isi halaman.
    private JPanel buildContent() {
        // Background gradient
        JPanel bg = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0,
                        new Color(180, 225, 255), 0, getHeight(), new Color(200, 255, 220));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        // Card putih
        RoundedPanel card = new RoundedPanel(24, Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(36, 40, 36, 40));
        card.setPreferredSize(new Dimension(360, 460));

        // Logo
        JLabel logo = new JLabel(loadLogoIcon(70), SwingConstants.CENTER);
        logo.setAlignmentX(CENTER_ALIGNMENT);
        logo.setPreferredSize(new Dimension(70, 70));
        logo.setMaximumSize(new Dimension(70, 70));
        logo.setMinimumSize(new Dimension(70, 70));

        JPanel logoWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoWrap.setOpaque(false);
        logoWrap.add(logo);

        JLabel appSub = new JLabel("SISTEM ABSENSI PAUD", SwingConstants.CENTER);
        appSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        appSub.setForeground(Theme.TEXT_GRAY);
        appSub.setAlignmentX(CENTER_ALIGNMENT);

        JLabel appName = new JLabel(DataStore.getInstance().getNamaSekolah(), SwingConstants.CENTER);
        appName.setFont(new Font("SansSerif", Font.BOLD, 18));
        appName.setForeground(Theme.TEXT_DARK);
        appName.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subTitle = new JLabel("Masuk sebagai Guru", SwingConstants.CENTER);
        subTitle.setFont(Theme.FONT_BODY);
        subTitle.setForeground(Theme.TEXT_GRAY);
        subTitle.setAlignmentX(CENTER_ALIGNMENT);

        // Fields
        JLabel lblUser = fieldLabel("Username");
        tfUsername = inputField("Masukkan username");

        JLabel lblPass = fieldLabel("Password");
        tfPassword = new JPasswordField();
        stylePasswordField(tfPassword, "Masukkan password");

        // Error label
        lblError = new JLabel("", SwingConstants.CENTER);
        lblError.setFont(Theme.FONT_SMALL);
        lblError.setForeground(Theme.RED);
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        // Tombol Login
        JButton btnLogin = makeRoundBtn("MASUK", new Color(33, 150, 243));
        btnLogin.addActionListener(e -> doLogin());

        // Hint akun default
        JLabel hint = new JLabel("<html><center>Akun default: <b>admin</b> / <b>admin123</b></center></html>", SwingConstants.CENTER);
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_GRAY);
        hint.setAlignmentX(CENTER_ALIGNMENT);

        // Separator
        JPanel sepPanel = makeSeparator();

        // Tombol Register
        JButton btnReg = makeLinkBtn("Belum punya akun? Daftar di sini");
        btnReg.addActionListener(e -> openRegister());

        // Enter key
        getRootPane().setDefaultButton(btnLogin);

        card.add(logoWrap);
        card.add(Box.createVerticalStrut(4));
        card.add(appSub);
        card.add(appName);
        card.add(Box.createVerticalStrut(6));
        card.add(subTitle);
        card.add(Box.createVerticalStrut(22));
        card.add(wrapFull(lblUser));
        card.add(Box.createVerticalStrut(4));
        card.add(wrapFull(tfUsername));
        card.add(Box.createVerticalStrut(12));
        card.add(wrapFull(lblPass));
        card.add(Box.createVerticalStrut(4));
        card.add(wrapFull(tfPassword));
        card.add(Box.createVerticalStrut(6));
        card.add(lblError);
        card.add(Box.createVerticalStrut(16));
        card.add(wrapFull(btnLogin));
        card.add(Box.createVerticalStrut(10));
        card.add(hint);
        card.add(Box.createVerticalStrut(14));
        card.add(sepPanel);
        card.add(Box.createVerticalStrut(10));
        card.add(btnReg);

        bg.add(card);
        return bg;
    }

    // Memproses login dari input pengguna.
    private void doLogin() {
        String user = tfUsername.getText().trim();
        String pass = getPasswordInput().trim();
        // Abaikan jika masih isi placeholder
        if (user.equals("Masukkan username")) user = "";
        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("Username dan password tidak boleh kosong!");
            return;
        }
        Guru g = DataStore.getInstance().login(user, pass);
        if (g != null) {
            lblError.setText("");
            dispose();
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        } else {
            lblError.setText("Username atau password salah!");
            clearPasswordForRetry();
        }
    }

    // Menangani proses: open register.
    private void openRegister() {
        RegisterFrame reg = new RegisterFrame(this);
        reg.setVisible(true);
    }

    // ---- Helpers ----
    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(Theme.FONT_HEADER);
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    // Menangani proses: input field.
    private JTextField inputField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(Theme.FONT_BODY);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200), 1, true),
            new EmptyBorder(4, 10, 4, 10)));
        tf.setForeground(Theme.TEXT_GRAY);
        tf.setText(placeholder);
        tf.addFocusListener(new FocusAdapter() {
            // Menangani proses: focus gained.
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(Theme.TEXT_DARK); }
            }
            // Menangani proses: focus lost.
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) { tf.setText(placeholder); tf.setForeground(Theme.TEXT_GRAY); }
            }
        });
        return tf;
    }

    // Menangani proses: style password field.
    private void stylePasswordField(JPasswordField pf, String placeholder) {
        pf.setFont(Theme.FONT_BODY);
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200), 1, true),
            new EmptyBorder(4, 10, 4, 10)));
        pf.setEchoChar((char) 0);
        pf.setForeground(Theme.TEXT_GRAY);
        pf.setText(placeholder);
        pf.addFocusListener(new FocusAdapter() {
            // Menangani proses: focus gained.
            public void focusGained(FocusEvent e) {
                if (String.valueOf(pf.getPassword()).equals(placeholder)) {
                    pf.setText(""); pf.setEchoChar('●'); pf.setForeground(Theme.TEXT_DARK);
                }
            }
            // Menangani proses: focus lost.
            public void focusLost(FocusEvent e) {
                if (pf.getPassword().length == 0) {
                    pf.setEchoChar((char)0); pf.setText(placeholder); pf.setForeground(Theme.TEXT_GRAY);
                }
            }
        });
    }

    // Menangani proses: get password input.
    private String getPasswordInput() {
        String raw = new String(tfPassword.getPassword());
        return "Masukkan password".equals(raw) ? "" : raw;
    }

    // Menangani proses: clear password for retry.
    private void clearPasswordForRetry() {
        tfPassword.setText("");
        tfPassword.setEchoChar('●');
        tfPassword.setForeground(Theme.TEXT_DARK);
        SwingUtilities.invokeLater(() -> tfPassword.requestFocusInWindow());
    }

    // Menangani proses: make round btn.
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
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setAlignmentX(CENTER_ALIGNMENT);
        return b;
    }

    // Menangani proses: make link btn.
    private JButton makeLinkBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setForeground(new Color(33, 150, 243));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(CENTER_ALIGNMENT);
        return b;
    }

    // Menangani proses: wrap full.
    private JPanel wrapFull(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(CENTER_ALIGNMENT);
        p.add(c);
        return p;
    }

    // Menangani proses: make separator.
    private JPanel makeSeparator() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setAlignmentX(CENTER_ALIGNMENT);
        JSeparator l = new JSeparator(); l.setForeground(new Color(210,210,210));
        JSeparator r = new JSeparator(); r.setForeground(new Color(210,210,210));
        JLabel or = new JLabel("atau", SwingConstants.CENTER);
        or.setFont(Theme.FONT_SMALL); or.setForeground(Theme.TEXT_GRAY);
        p.add(l, BorderLayout.WEST); p.add(or, BorderLayout.CENTER); p.add(r, BorderLayout.EAST);
        return p;
    }

    // Menangani proses: load logo icon.
    private Icon loadLogoIcon(int size) {
        try {
            ImageIcon raw = null;
            File f = resolveLogoFile();
            if (f != null && f.exists()) {
                raw = new ImageIcon(f.getAbsolutePath());
            } else {
                URL u = LoginFrame.class.getResource("/com/mycompany/paud/assets/images/logo.png");
                if (u == null) u = LoginFrame.class.getResource("/assets/images/logo.png");
                if (u != null) raw = new ImageIcon(u);
            }
            if (raw == null || raw.getIconWidth() <= 0) return fallbackLogoIcon(size);
            Image img = raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception ignored) {
            return fallbackLogoIcon(size);
        }
    }

    // Menangani proses: resolve logo file.
    private File resolveLogoFile() {
        String[] candidates = {
            "assets/images/logo.png",
            "src/main/java/com/mycompany/paud/assets/images/logo.png",
            "./src/main/java/com/mycompany/paud/assets/images/logo.png",
            "../src/main/java/com/mycompany/paud/assets/images/logo.png"
        };
        for (String p : candidates) {
            File f = new File(p);
            if (f.exists()) return f;
        }
        return null;
    }

    // Menangani proses: fallback logo icon.
    private Icon fallbackLogoIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(33, 150, 243));
        g.fillOval(0, 0, size, size);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, size / 2)));
        FontMetrics fm = g.getFontMetrics();
        String t = "P";
        int x = (size - fm.stringWidth(t)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(t, x, y);
        g.dispose();
        return new ImageIcon(img);
    }
}
