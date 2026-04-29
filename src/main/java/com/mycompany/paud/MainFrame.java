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

    private static final String[] NAV_NAMES = {"Dashboard", "Data Siswa", "Data Kelas", "Data Guru", "Data Pengguna", "Absensi Hari Ini", "Laporan Bulan", "Pengaturan"};
    private static final String[] NAV_ICONS = {"🏠", "👥", "🏷️", "👩", "👤", "📅", "📊", "⚙"};
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

        JLabel logoIcon = new JLabel("🎒") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(33, 150, 243));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        logoIcon.setForeground(Color.WHITE);
        logoIcon.setPreferredSize(new Dimension(48, 48));
        logoIcon.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        JLabel sub = new JLabel("SISTEM ABSENSI PAUD");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 10));
        sub.setForeground(Theme.TEXT_GRAY);
        JLabel appName = new JLabel("TUNAS HARAPAN");
        appName.setFont(new Font("SansSerif", Font.BOLD, 13));
        appName.setForeground(Theme.TEXT_DARK);
        textCol.add(sub);
        textCol.add(appName);

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
        String kelGuru  = (guru != null) ? "Kelompok: " + guru.getKelompokAmpu() : "";

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
        JLabel lbl = new JLabel(NAV_ICONS[idx] + "  " + NAV_NAMES[idx]) {
            @Override protected void paintComponent(Graphics g) {
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
}
