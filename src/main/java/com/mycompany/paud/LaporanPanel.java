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
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.awt.geom.AffineTransform;

public class LaporanPanel extends JPanel {
    private DataStore ds = DataStore.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblRingkasan;
    private JLabel lblDetailInfo;
    private DefaultTableModel detailModel;
    private JComboBox<String> cbBulan, cbTahun, cbHari, cbStatus, cbKelas;
    private CardLayout bodyLayout;
    private JPanel bodyPanel;
    private JPanel listPanel;
    private JPanel detailPanel;
    private int activeBulan = LocalDate.now().getMonthValue();
    private int activeTahun = LocalDate.now().getYear();

    private static final String[] BULAN_NAMA = {
        "Januari","Februari","Maret","April","Mei","Juni",
        "Juli","Agustus","September","Oktober","November","Desember"
    };
    private static final DateTimeFormatter FMT_SHORT_ID =
            DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("id", "ID"));

    public LaporanPanel() {
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(buildFilterBar(), BorderLayout.NORTH);
        add(buildBody(),      BorderLayout.CENTER);
        add(buildSummary(),   BorderLayout.SOUTH);

        loadLaporan();
    }

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel();
        bar.setOpaque(false);
        bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row1.setOpaque(false);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row2.setOpaque(false);

        JLabel lbl = new JLabel("Laporan Bulan:");
        lbl.setFont(Theme.FONT_HEADER);
        row1.add(lbl);

        cbBulan = new JComboBox<>(BULAN_NAMA);
        cbBulan.setFont(Theme.FONT_BODY);
        cbBulan.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        row1.add(cbBulan);

        int tahunSekarang = LocalDate.now().getYear();
        String[] tahuns = new String[5];
        for (int i = 0; i < 5; i++) tahuns[i] = String.valueOf(tahunSekarang - i);
        cbTahun = new JComboBox<>(tahuns);
        cbTahun.setFont(Theme.FONT_BODY);
        row1.add(cbTahun);

        row1.add(new JLabel("Hari:"));
        cbHari = new JComboBox<>();
        cbHari.setFont(Theme.FONT_BODY);
        cbHari.setPreferredSize(new Dimension(90, 28));
        row1.add(cbHari);

        row2.add(new JLabel("Status:"));
        cbStatus = new JComboBox<>(new String[]{"Semua Status", "Hadir", "Izin", "Sakit", "Alpa"});
        cbStatus.setFont(Theme.FONT_BODY);
        cbStatus.setPreferredSize(new Dimension(130, 28));
        row2.add(cbStatus);

        row2.add(new JLabel("Kelas:"));
        cbKelas = new JComboBox<>();
        cbKelas.setFont(Theme.FONT_BODY);
        cbKelas.setPreferredSize(new Dimension(120, 28));
        refreshKelasOptions();
        row2.add(cbKelas);

        cbBulan.addActionListener(e -> refreshHariOptions());
        cbTahun.addActionListener(e -> refreshHariOptions());
        refreshHariOptions();

        JButton btnLoad = new JButton("Tampilkan") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? Theme.BLUE_BTN.darker() : Theme.BLUE_BTN);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLoad.setFont(Theme.FONT_BODY);
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setContentAreaFilled(false);
        btnLoad.setBorderPainted(false);
        btnLoad.setFocusPainted(false);
        btnLoad.setPreferredSize(new Dimension(110, 30));
        btnLoad.addActionListener(e -> loadLaporan());
        row2.add(btnLoad);

        bar.add(row1);
        bar.add(Box.createVerticalStrut(8));
        bar.add(row2);

        return bar;
    }

    private JPanel buildBody() {
        bodyLayout = new CardLayout();
        bodyPanel = new JPanel(bodyLayout);
        bodyPanel.setOpaque(false);

        listPanel = new JPanel(new BorderLayout());
        listPanel.setOpaque(false);
        listPanel.add(buildTable(), BorderLayout.CENTER);

        detailPanel = buildDetailPanel();
        bodyPanel.add(listPanel, "LIST");
        bodyPanel.add(detailPanel, "DETAIL");
        return bodyPanel;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID","Nama Anak","Kelompok","Hadir","Izin","Sakit","Alpa","Total Hadir"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return getColumnCount() > 8 && c == 8;
            }
        };

        table = new JTable(tableModel);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230,230,230));
        table.getTableHeader().setFont(Theme.FONT_HEADER);
        table.getTableHeader().setBackground(new Color(235,245,255));
        table.setSelectionBackground(new Color(180,220,255));

        // Sembunyikan kolom ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        // Warna kolom status
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t,val,sel,focus,row,col);
                if (!sel) {
                    switch (col) {
                        case 3: c.setForeground(Theme.GREEN);    break;
                        case 4: c.setForeground(Theme.ORANGE);   break;
                        case 5: c.setForeground(Theme.RED);      break;
                        case 6: c.setForeground(Theme.GRAY_BTN); break;
                        default: c.setForeground(Theme.TEXT_DARK);
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        for (int i = 3; i <= 7; i++)
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        table.setRowSorter(new TableRowSorter<>(tableModel));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(210,210,210)));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private JPanel buildDetailPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        lblDetailInfo = new JLabel("Detail Absensi");
        lblDetailInfo.setFont(Theme.FONT_BODY);
        top.add(lblDetailInfo, BorderLayout.WEST);

        JButton btnBack = new JButton("Kembali") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? Theme.GRAY_BTN.darker() : Theme.GRAY_BTN);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnBack.setFont(Theme.FONT_BODY);
        btnBack.setForeground(Color.WHITE);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setPreferredSize(new Dimension(120, 30));
        btnBack.addActionListener(e -> showListView());
        top.add(btnBack, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        detailModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable detailTable = new JTable(detailModel);
        detailTable.setFont(Theme.FONT_BODY);
        detailTable.setRowHeight(30);
        detailTable.getTableHeader().setDefaultRenderer(new DiagonalHeaderRenderer());
        detailTable.getTableHeader().setPreferredSize(new Dimension(0, 74));
        detailTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        detailTable.setRowSelectionAllowed(false);
        detailTable.setShowHorizontalLines(true);
        detailTable.setShowVerticalLines(true);
        detailTable.setGridColor(new Color(210, 210, 210));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String v = value == null ? "" : value.toString();
                    if ("Hadir".equalsIgnoreCase(v)) c.setForeground(Theme.GREEN);
                    else if ("Izin".equalsIgnoreCase(v)) c.setForeground(Theme.ORANGE);
                    else if ("Sakit".equalsIgnoreCase(v)) c.setForeground(Theme.RED);
                    else if ("Alpa".equalsIgnoreCase(v)) c.setForeground(Theme.GRAY_BTN);
                    else c.setForeground(Theme.TEXT_DARK);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        detailTable.setDefaultRenderer(Object.class, center);

        JScrollPane sp = new JScrollPane(detailTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(210,210,210)));
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.getViewport().setBackground(Color.WHITE);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildSummary() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        p.setOpaque(false);
        lblRingkasan = new JLabel();
        lblRingkasan.setFont(Theme.FONT_BODY);
        p.add(lblRingkasan);
        return p;
    }

    public void loadLaporan() {
        int bulan = cbBulan.getSelectedIndex() + 1;
        int tahun = Integer.parseInt((String) cbTahun.getSelectedItem());
        activeBulan = bulan;
        activeTahun = tahun;
        String hariVal = (String) cbHari.getSelectedItem();
        Integer hari = null;
        if (hariVal != null && !hariVal.equals("Semua Hari")) {
            hari = Integer.parseInt(hariVal);
        }
        boolean showDetail = hari == null;
        resetMainColumns(showDetail);

        String statusVal = (String) cbStatus.getSelectedItem();
        String status = (statusVal == null || statusVal.equals("Semua Status")) ? null : statusVal;
        String kelasVal = (String) cbKelas.getSelectedItem();
        String kelas = (kelasVal == null || kelasVal.equals("Semua Kelas")) ? null : kelasVal;

        List<Object[]> rows = ds.getLaporanBulanFiltered(tahun, bulan, hari, status, kelas);
        tableModel.setRowCount(0);

        int totalH = 0, totalI = 0, totalS = 0, totalA = 0;
        for (Object[] row : rows) {
            if (showDetail) {
                tableModel.addRow(new Object[]{row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], "Detail"});
            } else {
                tableModel.addRow(row);
            }
            totalH += (int) row[3];
            totalI += (int) row[4];
            totalS += (int) row[5];
            totalA += (int) row[6];
        }
        configureDetailColumn(showDetail);
        showListView();

        String bln = BULAN_NAMA[bulan-1] + " " + tahun;
        String infoFilterHari = (hari == null) ? "Semua Hari" : ("Tanggal " + hari);
        String infoFilterStatus = (status == null) ? "Semua Status" : status;
        String infoFilterKelas = (kelas == null) ? "Semua Kelas" : ("Kelas " + kelas);
        lblRingkasan.setText(
            "<html><b>" + bln + "</b>  |  " +
            "<b>Filter:</b> " + infoFilterHari + ", " + infoFilterStatus + ", " + infoFilterKelas + "  |  " +
            "<font color='#4CAF50'>Hadir: " + totalH + "</font>  " +
            "<font color='#FF9800'>Izin: "  + totalI + "</font>  " +
            "<font color='#E53935'>Sakit: " + totalS + "</font>  " +
            "<font color='#9E9E9E'>Alpa: "  + totalA + "</font>" +
            "  |  <b>Total Absensi Tercatat: " + (totalH+totalI+totalS+totalA) + "</b></html>");
    }

    private void configureDetailColumn(boolean showDetail) {
        if (table == null) return;
        if (!showDetail) return;
        int idx = table.convertColumnIndexToView(8);
        if (idx < 0) return;
        TableColumn col = table.getColumnModel().getColumn(idx);
        col.setMinWidth(90);
        col.setMaxWidth(110);
        col.setPreferredWidth(100);
        col.setCellRenderer(new DetailButtonRenderer());
        col.setCellEditor(new DetailButtonEditor());
    }

    private void resetMainColumns(boolean showDetail) {
        if (showDetail) {
            tableModel.setColumnIdentifiers(new String[]{"ID","Nama Anak","Kelompok","Hadir","Izin","Sakit","Alpa","Total Hadir","Detail"});
        } else {
            tableModel.setColumnIdentifiers(new String[]{"ID","Nama Anak","Kelompok","Hadir","Izin","Sakit","Alpa","Total Hadir"});
        }
        tableModel.setRowCount(0);
        // re-hide ID column after resetting columns
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        applyStatusRenderers();
    }

    private void applyStatusRenderers() {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t,val,sel,focus,row,col);
                if (!sel) {
                    switch (col) {
                        case 3: c.setForeground(Theme.GREEN);    break;
                        case 4: c.setForeground(Theme.ORANGE);   break;
                        case 5: c.setForeground(Theme.RED);      break;
                        case 6: c.setForeground(Theme.GRAY_BTN); break;
                        default: c.setForeground(Theme.TEXT_DARK);
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        for (int i = 3; i <= 7; i++) {
            if (i < table.getColumnModel().getColumnCount()) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    private void openDetailSiswa(int modelRow) {
        if (modelRow < 0 || modelRow >= tableModel.getRowCount()) return;
        int siswaId = (int) tableModel.getValueAt(modelRow, 0);
        Siswa s = ds.getSiswaById(siswaId);
        List<Object[]> detail = ds.getDetailAbsensiSiswaBulanan(siswaId, activeTahun, activeBulan);

        List<String> headers = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (Object[] row : detail) {
            String rawDate = String.valueOf(row[0]);
            String status = String.valueOf(row[1]);
            LocalDate d = LocalDate.parse(rawDate);
            headers.add(d.format(FMT_SHORT_ID));
            values.add(status);
        }

        detailModel.setColumnCount(0);
        detailModel.setRowCount(0);
        for (String h : headers) detailModel.addColumn(h);
        if (!values.isEmpty()) detailModel.addRow(values.toArray());
        JTable detailTable = getDetailTable();
        if (detailTable != null) {
            for (int i = 0; i < detailTable.getColumnModel().getColumnCount(); i++) {
                detailTable.getColumnModel().getColumn(i).setPreferredWidth(84);
                detailTable.getColumnModel().getColumn(i).setMinWidth(84);
            }
            detailTable.getTableHeader().repaint();
        }

        String npd = s == null ? "-" : safe(s.getNpd());
        String nisn = s == null ? "-" : safe(s.getNisn());
        String nama = s == null ? "-" : safe(s.getNama());
        String kelas = s == null ? "-" : safe(s.getKelompok());
        lblDetailInfo.setText("<html><b>NPD:</b> " + npd +
                "  |  <b>NISN:</b> " + nisn +
                "  |  <b>Nama:</b> " + nama +
                "  |  <b>Kelas:</b> " + kelas + "</html>");

        bodyLayout.show(bodyPanel, "DETAIL");
    }

    private String safe(String v) {
        return (v == null || v.trim().isEmpty()) ? "-" : v.trim();
    }

    private void showListView() {
        bodyLayout.show(bodyPanel, "LIST");
    }

    private void refreshHariOptions() {
        int bulan = cbBulan.getSelectedIndex() + 1;
        int tahun = Integer.parseInt((String) cbTahun.getSelectedItem());
        String selected = (String) cbHari.getSelectedItem();

        cbHari.removeAllItems();
        cbHari.addItem("Semua Hari");

        int jumlahHari = YearMonth.of(tahun, bulan).lengthOfMonth();
        for (int h = 1; h <= jumlahHari; h++) {
            cbHari.addItem(String.valueOf(h));
        }

        if (selected != null) {
            cbHari.setSelectedItem(selected);
        }
        if (cbHari.getSelectedItem() == null) {
            cbHari.setSelectedIndex(0);
        }
    }

    private void refreshKelasOptions() {
        if (cbKelas == null) return;
        String current = (String) cbKelas.getSelectedItem();
        cbKelas.removeAllItems();
        cbKelas.addItem("Semua Kelas");
        for (Kelas k : getVisibleKelasList()) cbKelas.addItem(k.getNama());
        if (current != null) cbKelas.setSelectedItem(current);
        if (cbKelas.getSelectedItem() == null) cbKelas.setSelectedIndex(0);
    }

    private List<Kelas> getVisibleKelasList() {
        List<Kelas> all = ds.getDaftarKelas();
        if (!ds.isGuruRoleLogin()) return all;
        Set<String> allowed = new LinkedHashSet<>(ds.getKelasAmpuLoginSet());
        List<Kelas> out = new ArrayList<>();
        for (Kelas k : all) {
            if (allowed.contains(k.getNama())) out.add(k);
        }
        return out;
    }

    private class DetailButtonRenderer extends JButton implements TableCellRenderer {
        DetailButtonRenderer() {
            setText("Detail");
            setFont(Theme.FONT_BODY);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            return this;
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.BLUE_BTN);
            g2.fillRoundRect(6, 4, getWidth() - 12, getHeight() - 8, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class DetailButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button;
        private int editingRow = -1;

        DetailButtonEditor() {
            button = new JButton("Detail") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isPressed() ? Theme.BLUE_BTN.darker() : Theme.BLUE_BTN);
                    g2.fillRoundRect(6, 4, getWidth() - 12, getHeight() - 8, 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            button.setFont(Theme.FONT_BODY);
            button.setFocusPainted(false);
            button.setForeground(Color.WHITE);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setOpaque(false);
            button.setHorizontalAlignment(SwingConstants.CENTER);
            button.addActionListener(e -> {
                int viewRow = editingRow;
                fireEditingStopped();
                if (viewRow >= 0) {
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    openDetailSiswa(modelRow);
                }
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            return button;
        }
        @Override
        public Object getCellEditorValue() { return "Detail"; }
    }

    private JTable getDetailTable() {
        if (detailPanel == null) return null;
        for (Component c : detailPanel.getComponents()) {
            if (c instanceof JScrollPane) {
                JViewport vp = ((JScrollPane) c).getViewport();
                if (vp != null && vp.getView() instanceof JTable) {
                    return (JTable) vp.getView();
                }
            }
        }
        return null;
    }

    private class DiagonalHeaderRenderer extends JLabel implements TableCellRenderer {
        private String textValue = "";

        DiagonalHeaderRenderer() {
            setOpaque(true);
            setBackground(new Color(235, 245, 255));
            setForeground(Theme.TEXT_DARK);
            setFont(Theme.FONT_BODY.deriveFont(Font.ITALIC));
            setHorizontalAlignment(SwingConstants.LEFT);
            setVerticalAlignment(SwingConstants.BOTTOM);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(210, 210, 210)));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            textValue = value == null ? "" : value.toString();
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(210, 210, 210));
            g2.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
            g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight() - 1);

            g2.setColor(getForeground());
            g2.setFont(getFont());
            AffineTransform old = g2.getTransform();
            // Arah teks dari kiri bawah ke kanan atas
            g2.rotate(-Math.toRadians(36), 8, getHeight() - 8);
            g2.drawString(textValue, 8, getHeight() - 8);
            g2.setTransform(old);
            g2.dispose();
        }
    }
}
