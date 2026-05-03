package com.mycompany.paud;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class DataKelasPanel extends JPanel {

    private final DataStore ds = DataStore.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField tfNama;
    private JLabel lblFormTitle, lblStatus;
    private JButton btnSimpan, btnBatal, btnHapus, btnTambah;
    private JPanel statusRowTop;
    private String statusSummaryText = "";
    private int editingId = -1;

    private static final String[] COLUMNS = {"ID", "Nama Kelas"};

    // Menjalankan inisialisasi objek DataKelasPanel.
    public DataKelasPanel() {
        setLayout(new BorderLayout(16, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        add(buildTableSide(), BorderLayout.CENTER);
        add(buildFormSide(), BorderLayout.EAST);
        loadTable();
    }

    // Menangani proses: build table side.
    private JPanel buildTableSide() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        statusRowTop = new JPanel(new BorderLayout());
        statusRowTop.setOpaque(false);
        lblStatus = new JLabel();
        lblStatus.setFont(Theme.FONT_BODY);
        lblStatus.setForeground(Theme.TEXT_GRAY);
        btnTambah = makeBtn("+ Tambah Kelas", Theme.GREEN, e -> mulaiTambah());
        btnTambah.setPreferredSize(new Dimension(150, 34));
        statusRowTop.add(lblStatus, BorderLayout.WEST);
        statusRowTop.add(btnTambah, BorderLayout.EAST);
        statusRowTop.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                renderWrappedStatus();
            }
        });
        p.add(statusRowTop, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(34);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.setRowSorter(new TableRowSorter<>(tableModel));

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row < 0) return;
                int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
                Kelas k = ds.getKelasById(id);
                if (k != null) pilihKelas(k);
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(210,210,210)));
        sp.getViewport().setBackground(Color.WHITE);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // Menangani proses: build form side.
    private JPanel buildFormSide() {
        RoundedPanel form = Theme.makeCard(16);
        form.setLayout(new BorderLayout());
        form.setPreferredSize(new Dimension(320, 0));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        lblFormTitle = new JLabel("FORM TAMBAH KELAS");
        lblFormTitle.setFont(Theme.FONT_HEADER);
        lblFormTitle.setAlignmentX(LEFT_ALIGNMENT);

        tfNama = new JTextField();
        tfNama.setFont(Theme.FONT_BODY);
        tfNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tfNama.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200,200,200),1,true),
                new EmptyBorder(4,8,4,8)));

        content.add(lblFormTitle);
        content.add(Box.createVerticalStrut(14));
        content.add(fLabel("Nama Kelas *"));
        content.add(Box.createVerticalStrut(4));
        content.add(tfNama);
        content.add(Box.createVerticalGlue());

        form.add(content, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JPanel row1 = new JPanel(new GridLayout(1,2,8,0));
        row1.setOpaque(false);
        btnSimpan = makeBtn("💾 Simpan", Theme.BLUE_BTN, e -> simpan());
        btnBatal  = makeBtn("✕ Batal", Theme.GRAY_BTN, e -> clearForm());
        row1.add(btnSimpan);
        row1.add(btnBatal);

        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);
        btnHapus = makeBtn("🗑 Hapus Kelas", Theme.RED, e -> hapus());
        btnHapus.setEnabled(false);
        row2.add(btnHapus);

        buttonPanel.add(row1);
        buttonPanel.add(Box.createVerticalStrut(8));
        buttonPanel.add(row2);
        form.add(buttonPanel, BorderLayout.SOUTH);

        return form;
    }

    // Menangani proses: mulai tambah.
    private void mulaiTambah() {
        editingId = -1;
        clearFields();
        lblFormTitle.setText("FORM TAMBAH KELAS");
        btnHapus.setEnabled(false);
        table.clearSelection();
        tfNama.requestFocusInWindow();
    }

    // Menangani proses: pilih kelas.
    private void pilihKelas(Kelas k) {
        editingId = k.getId();
        tfNama.setText(k.getNama());
        tfNama.setForeground(Theme.TEXT_DARK);
        lblFormTitle.setText("EDIT KELAS (ID: " + k.getId() + ")");
        btnHapus.setEnabled(true);
        tfNama.requestFocusInWindow();
    }

    // Menyimpan data form ke database.
    private void simpan() {
        String nama = tfNama.getText().trim();
        if (nama.isEmpty()) { showError("Nama kelas tidak boleh kosong!"); return; }

        boolean ok;
        if (editingId < 0) {
            ok = ds.tambahKelas(nama);
            if (!ok) { showError("Kelas sudah ada atau gagal disimpan."); return; }
            showSuccess("Kelas berhasil ditambahkan.");
        } else {
            ok = ds.updateKelas(editingId, nama);
            if (!ok) { showError("Nama kelas sudah ada atau gagal diperbarui."); return; }
            showSuccess("Kelas berhasil diperbarui.");
        }

        loadTable();
        clearForm();
    }

    // Menghapus data yang sedang dipilih.
    private void hapus() {
        if (editingId < 0) return;
        if (ds.kelasSedangDipakai(editingId)) {
            showError("Kelas sedang dipakai pada data siswa/guru, tidak bisa dihapus.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Hapus kelas ini?", "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (!ds.hapusKelas(editingId)) {
            showError("Gagal menghapus kelas.");
            return;
        }
        loadTable();
        clearForm();
        showSuccess("Kelas berhasil dihapus.");
    }

    // Menangani proses: clear form.
    private void clearForm() {
        editingId = -1;
        clearFields();
        lblFormTitle.setText("FORM TAMBAH KELAS");
        btnHapus.setEnabled(false);
        table.clearSelection();
    }

    // Menangani proses: clear fields.
    private void clearFields() {
        tfNama.setText("");
        tfNama.setForeground(Theme.TEXT_DARK);
    }

    // Memuat data terbaru ke tabel.
    public void loadTable() {
        List<Kelas> list = ds.getDaftarKelas();
        tableModel.setRowCount(0);
        for (Kelas k : list) {
            tableModel.addRow(new Object[]{k.getId(), k.getNama()});
        }
        statusSummaryText = "Total: " + list.size() + " kelas";
        renderWrappedStatus();
    }

    // Menangani proses: render wrapped status.
    private void renderWrappedStatus() {
        if (lblStatus == null) return;
        if (statusSummaryText == null || statusSummaryText.isEmpty()) {
            lblStatus.setText("");
            return;
        }
        int available = 420;
        if (statusRowTop != null && btnTambah != null) {
            int gap = 18;
            available = statusRowTop.getWidth() - btnTambah.getPreferredSize().width - gap;
        }
        if (available <= 120) {
            lblStatus.setText(statusSummaryText);
            return;
        }

        FontMetrics fm = lblStatus.getFontMetrics(lblStatus.getFont());
        String[] parts = statusSummaryText.split("\\s+\\|\\s+");
        StringBuilder html = new StringBuilder("<html>");
        int lineWidth = 0;
        for (int i = 0; i < parts.length; i++) {
            String token = (i == 0) ? parts[i] : " | " + parts[i];
            int w = fm.stringWidth(token);
            if (lineWidth > 0 && lineWidth + w > available) {
                html.append("<br>");
                token = parts[i];
                lineWidth = 0;
                w = fm.stringWidth(token);
            }
            html.append(token);
            lineWidth += w;
        }
        html.append("</html>");
        lblStatus.setText(html.toString());
    }

    // Menangani proses: f label.
    private JLabel fLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(Theme.FONT_HEADER);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    // Menangani proses: make btn.
    private JButton makeBtn(String text, Color bg, ActionListener al) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = !isEnabled() ? new Color(180,180,180) : getModel().isPressed() ? bg.darker() : bg;
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

    // Menangani proses: show error.
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    // Menangani proses: show success.
    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Berhasil", JOptionPane.INFORMATION_MESSAGE);
    }
}
