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
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.time.*;
import java.time.format.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainFrame extends JFrame {
    private JPanel contentArea;
    private CardLayout cardLayout;
    private DashboardPanel dashPanel;
    private DataSiswaPanel dataSiswaPanel;
    private DataKelasPanel dataKelasPanel;
    private DataGuruPanel dataGuruPanel;
    private DataPenggunaPanel dataPenggunaPanel;
    private AbsensiPanel absensiPanel;
    private LaporanPanel laporanPanel;
    private PengaturanPanel pengaturanPanel;
    private JLabel[] navLabels;
    private List<Integer> visibleNavIndexes = new ArrayList<>();
    private int activeNav = 0;
    private JLabel headerTitle;
    private JComboBox<String> cbAngkatanSidebar;
    private JLabel lblSchoolNameSidebar;

    private static final String[] NAV_NAMES = {"Dashboard", "Data Siswa", "Data Kelas", "Data Guru", "Data Pengguna", "Absensi Hari Ini", "Laporan Bulan", "Pengaturan"};
    private static final String[] CARD_KEYS = {"DASHBOARD", "DATASISWA", "DATAKELAS", "DATAGURU", "DATAPENGGUNA", "ABSENSI", "LAPORAN", "PENGATURAN"};

    public MainFrame() {
        setTitle("Java NetBeans PAUD Mockup");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_MAIN);
        setContentPane(root);


        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(buildHeader(), BorderLayout.NORTH);
        right.add(buildContent(), BorderLayout.CENTER);
        root.add(right, BorderLayout.CENTER);
        
        root.add(buildSidebar(), BorderLayout.WEST);
        applyAngkatanFilterRefresh();
    }

    // ---- SIDEBAR ----
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, new Color(224, 232, 240)));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        // Logo area
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 18));
        logo.setBackground(Color.WHITE);
        logo.setMaximumSize(new Dimension(230, 90));

        JLabel logoIcon = new JLabel();
        logoIcon.setIcon(loadLogoIcon(48));
        logoIcon.setPreferredSize(new Dimension(48, 48));
        logoIcon.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        JLabel sub = new JLabel("SISTEM ABSENSI PAUD");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 10));
        sub.setForeground(Theme.TEXT_GRAY);
        lblSchoolNameSidebar = new JLabel(DataStore.getInstance().getNamaSekolah());
        lblSchoolNameSidebar.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblSchoolNameSidebar.setForeground(Theme.TEXT_DARK);
        textCol.add(sub);
        textCol.add(lblSchoolNameSidebar);

        logo.add(logoIcon);
        logo.add(textCol);
        top.add(logo);

        JPanel angkatanWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        angkatanWrap.setOpaque(false);
        JLabel lblAngkatan = new JLabel("Angkatan:");
        lblAngkatan.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblAngkatan.setForeground(Theme.TEXT_GRAY);
        cbAngkatanSidebar = new JComboBox<>();
        cbAngkatanSidebar.setFont(Theme.FONT_BODY);
        cbAngkatanSidebar.setPreferredSize(new Dimension(130, 28));
        initAngkatanSidebarOptions();
        cbAngkatanSidebar.addActionListener(e -> {
            String selected = (String) cbAngkatanSidebar.getSelectedItem();
            DataStore.getInstance().setActiveAngkatan(selected);
            applyAngkatanFilterRefresh();
        });
        angkatanWrap.add(lblAngkatan);
        angkatanWrap.add(cbAngkatanSidebar);
        top.add(angkatanWrap);
        top.add(Box.createVerticalStrut(8));

        sidebar.add(top, BorderLayout.NORTH);

        // Nav items
        JPanel navList = new JPanel();
        navList.setOpaque(false);
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setBorder(new EmptyBorder(0, 8, 0, 8));
        navList.setAlignmentX(Component.LEFT_ALIGNMENT);

        visibleNavIndexes = resolveVisibleNavIndexes();
        navLabels = new JLabel[visibleNavIndexes.size()];
        for (int i = 0; i < visibleNavIndexes.size(); i++) {
            int realIdx = visibleNavIndexes.get(i);
            navLabels[i] = buildNavItem(realIdx);
            navList.add(navLabels[i]);
            navList.add(Box.createVerticalStrut(6));
        }
        sidebar.add(navList, BorderLayout.CENTER);

        // Guru info + logout
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        Guru guru = DataStore.getInstance().getGuruLogin();
        String namaGuru = (guru != null) ? guru.getNamaLengkap() : "Guest";
        String kelGuru  = (guru != null) ? "Kelas: " + guru.getKelompokAmpu() : "";

        JPanel guruInfo = new JPanel();
        guruInfo.setOpaque(false);
        guruInfo.setLayout(new BoxLayout(guruInfo, BoxLayout.Y_AXIS));
        guruInfo.setBorder(new EmptyBorder(8, 14, 4, 14));
        guruInfo.setMaximumSize(new Dimension(230, 60));
        JLabel lNama = new JLabel("👤 " + namaGuru);
        lNama.setFont(new Font("SansSerif", Font.BOLD, 12));
        lNama.setForeground(Theme.TEXT_DARK);
        JLabel lKel = new JLabel(kelGuru);
        lKel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lKel.setForeground(Theme.TEXT_GRAY);
        guruInfo.add(lNama);
        guruInfo.add(lKel);
        bottom.add(guruInfo);

        JButton btnLogout = new JButton("🚪 Keluar") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(200,50,50) : new Color(229,57,53));
                g2.fillRoundRect(8,4,getWidth()-16,getHeight()-8,10,10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogout.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setMaximumSize(new Dimension(230, 40));
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(MainFrame.this,
                "Yakin ingin keluar?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                DataStore.getInstance().logout();
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        bottom.add(btnLogout);
        bottom.add(Box.createVerticalStrut(12));
        sidebar.add(bottom, BorderLayout.SOUTH);

        selectNav(visibleNavIndexes.isEmpty() ? 0 : visibleNavIndexes.get(0));
        return sidebar;
    }

    private List<Integer> resolveVisibleNavIndexes() {
        List<Integer> idx = new ArrayList<>();
        Guru g = DataStore.getInstance().getGuruLogin();
        boolean roleGuru = g != null && "Guru".equalsIgnoreCase(g.getRole());
        if (roleGuru) {
            idx.add(0); // Dashboard
            idx.add(1); // Data Siswa
            idx.add(5); // Absensi
            idx.add(6); // Laporan
        } else {
            for (int i = 0; i < NAV_NAMES.length; i++) idx.add(i);
        }
        return idx;
    }

    private void initAngkatanSidebarOptions() {
        refreshAngkatanSidebarOptions(false);
    }

    private String pickDefaultAngkatan(List<String> list, int tahunSekarang) {
        String fallback = list.get(list.size() - 1);
        String exact = null;
        int bestPrev = Integer.MIN_VALUE;
        String bestPrevStr = null;
        for (String s : list) {
            try {
                int y = Integer.parseInt(s.trim());
                if (y == tahunSekarang) exact = s;
                if (y < tahunSekarang && y > bestPrev) {
                    bestPrev = y;
                    bestPrevStr = s;
                }
            } catch (NumberFormatException ignored) {}
        }
        if (exact != null) return exact;
        if (bestPrevStr != null) return bestPrevStr;
        return fallback;
    }

    public void refreshAngkatanSidebarOptions(boolean preserveCurrentSelection) {
        if (cbAngkatanSidebar == null) return;
        String current = preserveCurrentSelection ? (String) cbAngkatanSidebar.getSelectedItem() : null;
        List<String> list = DataStore.getInstance().getDaftarAngkatan();
        cbAngkatanSidebar.removeAllItems();
        for (String a : list) cbAngkatanSidebar.addItem(a);
        if (list.isEmpty()) {
            DataStore.getInstance().setActiveAngkatan(null);
            applyAngkatanFilterRefresh();
            return;
        }

        String selected = null;
        if (current != null && list.contains(current)) {
            selected = current;
        } else {
            selected = pickDefaultAngkatan(list, LocalDate.now().getYear());
        }
        cbAngkatanSidebar.setSelectedItem(selected);
        DataStore.getInstance().setActiveAngkatan(selected);
        applyAngkatanFilterRefresh();
    }

    private void applyAngkatanFilterRefresh() {
        if (dataSiswaPanel != null) dataSiswaPanel.loadTable();
        if (absensiPanel != null) absensiPanel.refreshGrid();
        if (laporanPanel != null) laporanPanel.loadLaporan();
        if (dashPanel != null) dashPanel.refresh();
    }

    private JLabel buildNavItem(int idx) {
        JLabel lbl = new JLabel(NAV_NAMES[idx]) {
            @Override protected void paintComponent(Graphics g) {
                boolean selected = getClientProperty("selected") != null && (boolean)getClientProperty("selected");
                if (getClientProperty("selected") != null && (boolean)getClientProperty("selected")) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.SIDEBAR_SEL);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                    setForeground(Color.WHITE);
                } else {
                    setForeground(Theme.TEXT_DARK);
                }
                setIcon(makeNavIcon(idx, selected ? Color.WHITE : Theme.TEXT_DARK));
                super.paintComponent(g);
            }
        };
        lbl.setFont(Theme.FONT_NAV);
        lbl.setOpaque(false);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setHorizontalAlignment(SwingConstants.LEFT);
        lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
        lbl.setMinimumSize(new Dimension(0, 42));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        lbl.setPreferredSize(new Dimension(214, 42));
        lbl.setIconTextGap(10);
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        int i = idx;
        lbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { selectNav(i); }
            @Override public void mouseEntered(MouseEvent e) {
                if (!(boolean) lbl.getClientProperty("selected")) lbl.setBackground(new Color(240, 248, 255));
            }
            @Override public void mouseExited(MouseEvent e) { lbl.repaint(); }
        });
        lbl.putClientProperty("selected", false);
        return lbl;
    }

    private Icon makeNavIcon(int idx, Color color) {
        final int s = 16;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean selectedWhite = Color.WHITE.equals(color);
        Color c1 = selectedWhite ? Color.WHITE : new Color(52, 152, 219);
        Color c2 = selectedWhite ? Color.WHITE : new Color(46, 204, 113);
        Color c3 = selectedWhite ? Color.WHITE : new Color(243, 156, 18);
        Color c4 = selectedWhite ? Color.WHITE : new Color(231, 76, 60);
        Color c5 = selectedWhite ? Color.WHITE : new Color(155, 89, 182);
        Color cDark = selectedWhite ? Color.WHITE : new Color(44, 62, 80);

        switch (idx) {
            case 0: // Dashboard (home)
                g.setColor(c1);
                g.fillPolygon(new int[]{2, 8, 14}, new int[]{8, 2, 8}, 3);
                g.setColor(c2);
                g.fillRect(4, 8, 8, 6);
                break;
            case 1: // Data Siswa (2 person)
                g.setColor(c1);
                g.fillOval(2, 2, 5, 5);
                g.setColor(c5);
                g.fillOval(9, 2, 5, 5);
                g.setColor(c1);
                g.fillRoundRect(1, 8, 6, 6, 3, 3);
                g.setColor(c5);
                g.fillRoundRect(9, 8, 6, 6, 3, 3);
                break;
            case 2: // Data Kelas (tag)
                g.setColor(c3);
                g.fillRoundRect(2, 5, 12, 7, 3, 3);
                g.setColor(selectedWhite ? Color.WHITE : new Color(255, 255, 255, 170));
                g.fillOval(4, 7, 2, 2);
                break;
            case 3: // Data Guru (person)
                g.setColor(c3);
                g.fillOval(5, 2, 6, 6);
                g.setColor(c4);
                g.fillRoundRect(4, 8, 8, 6, 4, 4);
                break;
            case 4: // Data Pengguna (single user)
                g.setColor(cDark);
                g.fillOval(5, 2, 6, 6);
                g.setColor(c1);
                g.drawRoundRect(4, 8, 8, 6, 3, 3);
                break;
            case 5: // Absensi (calendar)
                g.setColor(cDark);
                g.drawRect(2, 3, 12, 11);
                g.setColor(c4);
                g.fillRect(2, 5, 12, 2);
                g.setColor(cDark);
                g.fillRect(4, 1, 2, 4);
                g.fillRect(10, 1, 2, 4);
                break;
            case 6: // Laporan (bar chart)
                g.setColor(c2);
                g.fillRect(2, 9, 2, 5);
                g.setColor(c3);
                g.fillRect(6, 6, 2, 8);
                g.setColor(c1);
                g.fillRect(10, 3, 2, 11);
                break;
            default: // Pengaturan (gear-ish)
                g.setColor(cDark);
                g.drawOval(4, 4, 8, 8);
                g.fillOval(7, 7, 2, 2);
                g.setColor(c1);
                g.fillRect(7, 1, 2, 3);
                g.fillRect(7, 12, 2, 3);
                g.fillRect(1, 7, 3, 2);
                g.fillRect(12, 7, 3, 2);
                break;
        }
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon loadLogoIcon(int size) {
        try {
            ImageIcon raw = null;
            File f = resolveLogoFile();
            if (f != null && f.exists()) {
                raw = new ImageIcon(f.getAbsolutePath());
            } else {
                URL u = MainFrame.class.getResource("/com/mycompany/paud/assets/images/logo.png");
                if (u == null) u = MainFrame.class.getResource("/assets/images/logo.png");
                if (u != null) raw = new ImageIcon(u);
            }
            if (raw == null || raw.getIconWidth() <= 0) return fallbackLogoIcon(size);
            Image img = raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception ignored) {
            return fallbackLogoIcon(size);
        }
    }

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

    private void selectNav(int idx) {
        for (int i = 0; i < navLabels.length; i++) {
            int realIdx = visibleNavIndexes.get(i);
            boolean selected = realIdx == idx;
            navLabels[i].putClientProperty("selected", selected);
            navLabels[i].setFont(selected ? Theme.FONT_NAV_SEL : Theme.FONT_NAV);
            navLabels[i].repaint();
        }
        activeNav = idx;
        switch (idx) {
            case 0: headerTitle.setText("Dashboard Utama");      break;
            case 1: headerTitle.setText("DATA SISWA");           break;
            case 2: headerTitle.setText("DATA KELAS");           break;
            case 3: headerTitle.setText("DATA GURU");            break;
            case 4: headerTitle.setText("DATA PENGGUNA");        break;
            case 5: headerTitle.setText("INPUT ABSENSI HARIAN"); break;
            case 6: headerTitle.setText("LAPORAN BULAN");        break;
            default: headerTitle.setText("PENGATURAN");          break;
        }

        cardLayout.show(contentArea, CARD_KEYS[idx]);
        if (idx == 0) dashPanel.refresh();
        if (idx == 2) dataKelasPanel.loadTable();
        if (idx == 3) dataGuruPanel.loadTable();
        if (idx == 4) dataPenggunaPanel.loadTable();
        if (idx == 5) absensiPanel.resetToToday();
        if (idx == 6) laporanPanel.loadLaporan();
    }

    // ---- HEADER ----
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(220, 240, 255));
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        headerTitle = new JLabel("Dashboard Utama");
        headerTitle.setFont(Theme.FONT_TITLE);
        headerTitle.setForeground(Theme.TEXT_DARK);

        // Date + clock
        JPanel datePane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        datePane.setOpaque(false);

        JLabel dateLbl = new JLabel();
        dateLbl.setFont(Theme.FONT_BODY);
        dateLbl.setForeground(Theme.TEXT_DARK);
        updateClock(dateLbl);
        Timer timer = new Timer(1000, e -> updateClock(dateLbl));
        timer.start();

        JLabel calIcon = new JLabel("📅");
        calIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        datePane.add(dateLbl);
        datePane.add(calIcon);

        header.add(headerTitle, BorderLayout.WEST);
        header.add(datePane, BorderLayout.EAST);
        return header;
    }

    private void updateClock(JLabel lbl) {
        LocalDateTime now = LocalDateTime.now();
        String day = now.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id")).toUpperCase();
        String date = now.format(DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("id"))).toUpperCase();
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        lbl.setText(day + ", " + date + " - " + time + " WIB");
    }

    // ---- CONTENT ----
    private JPanel buildContent() {
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);

        dashPanel     = new DashboardPanel();
        dataSiswaPanel = new DataSiswaPanel();
        dataKelasPanel = new DataKelasPanel();
        dataGuruPanel = new DataGuruPanel();
        dataPenggunaPanel = new DataPenggunaPanel();
        absensiPanel  = new AbsensiPanel();
        laporanPanel  = new LaporanPanel();
        pengaturanPanel = new PengaturanPanel();

        contentArea.add(dashPanel,      "DASHBOARD");
        contentArea.add(dataSiswaPanel, "DATASISWA");
        contentArea.add(dataKelasPanel, "DATAKELAS");
        contentArea.add(dataGuruPanel,  "DATAGURU");
        contentArea.add(dataPenggunaPanel,  "DATAPENGGUNA");
        contentArea.add(absensiPanel,   "ABSENSI");
        contentArea.add(laporanPanel,   "LAPORAN");
        contentArea.add(pengaturanPanel,"PENGATURAN");
        return contentArea;
    }

    public void refreshSchoolBranding() {
        if (lblSchoolNameSidebar != null) {
            lblSchoolNameSidebar.setText(DataStore.getInstance().getNamaSekolah());
            lblSchoolNameSidebar.revalidate();
            lblSchoolNameSidebar.repaint();
        }
    }
}
