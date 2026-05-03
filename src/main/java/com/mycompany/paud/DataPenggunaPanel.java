package com.mycompany.paud;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataPenggunaPanel extends JPanel {
    private static final int FORM_FIELD_MAX_WIDTH = 320;

    private final DataStore ds = DataStore.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;
    private JScrollPane formScroll;

    private JTextField tfNip, tfUsername, tfNama;
    private JPasswordField pfPassword;
    private JComboBox<String> cbRole, cbFilterRole;
    private JPanel kelasOptionsPanel;
    private Map<String, JCheckBox> kelasChecks = new LinkedHashMap<>();

    private JLabel lblFormTitle, lblStatus;
    private JButton btnSimpan, btnBatal, btnHapus, btnTambah;
    private JPanel statusRowTop;
    private String statusSummaryText = "";

    private String editingUsername = null;

    private static final String[] COLUMNS = {
        "NIP", "Username", "Password", "Nama", "Role", "Kelas"
    };

    // Menjalankan inisialisasi objek DataPenggunaPanel.
    public DataPenggunaPanel() {
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

        JPanel toolbar = new JPanel();
        toolbar.setOpaque(false);
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));

        lblStatus = new JLabel();
        lblStatus.setFont(Theme.FONT_BODY);
        lblStatus.setForeground(Theme.TEXT_GRAY);

        statusRowTop = new JPanel(new BorderLayout());
        statusRowTop.setOpaque(false);
        btnTambah = makeBtn("+ Tambah Pengguna", Theme.GREEN, e -> mulaiTambah());
        btnTambah.setPreferredSize(new Dimension(190, 34));
        statusRowTop.add(lblStatus, BorderLayout.WEST);
        statusRowTop.add(btnTambah, BorderLayout.EAST);
        statusRowTop.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                renderWrappedStatus();
            }
        });

        JPanel rowBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rowBottom.setOpaque(false);
        JLabel lblFilter = new JLabel("Filter Role:");
        lblFilter.setFont(Theme.FONT_BODY);
        rowBottom.add(lblFilter);

        cbFilterRole = new JComboBox<>(new String[]{"Semua", "Administrator", "Guru"});
        cbFilterRole.setFont(Theme.FONT_BODY);
        cbFilterRole.setPreferredSize(new Dimension(130, 30));
        cbFilterRole.addActionListener(e -> loadTable());
        rowBottom.add(cbFilterRole);

        toolbar.add(statusRowTop);
        toolbar.add(Box.createVerticalStrut(6));
        toolbar.add(rowBottom);
        p.add(toolbar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(34);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(8, 6));
        table.setGridColor(new Color(235, 235, 235));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(235, 245, 255));
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.setSelectionBackground(new Color(180, 220, 255));
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.setRowSorter(new TableRowSorter<>(tableModel));

        table.getColumnModel().getColumn(0).setPreferredWidth(110); // NIP
        table.getColumnModel().getColumn(1).setPreferredWidth(130); // Username
        table.getColumnModel().getColumn(2).setPreferredWidth(130); // Password
        table.getColumnModel().getColumn(3).setPreferredWidth(180); // Nama
        table.getColumnModel().getColumn(4).setPreferredWidth(130); // Role
        table.getColumnModel().getColumn(5).setPreferredWidth(90);  // Kelas

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row < 0) return;
                String username = tableModel.getValueAt(table.convertRowIndexToModel(row), 1).toString();
                Guru g = ds.getGuruByUsername(username);
                if (g != null) pilihPengguna(g);
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        sp.getViewport().setBackground(Color.WHITE);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // Menangani proses: build form side.
    private JPanel buildFormSide() {
        RoundedPanel form = Theme.makeCard(16);
        form.setLayout(new BorderLayout(0, 0));
        form.setPreferredSize(new Dimension(360, 0));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 20, 12, 20));

        lblFormTitle = new JLabel("FORM TAMBAH PENGGUNA");
        lblFormTitle.setFont(Theme.FONT_HEADER);
        lblFormTitle.setAlignmentX(LEFT_ALIGNMENT);

        tfNip = fField("Nomor induk pegawai (NIP)");
        tfUsername = fField("Username login");
        pfPassword = fPassword("Password minimal 6 karakter");
        tfNama = fField("Nama lengkap pengguna");

        cbRole = new JComboBox<>(new String[]{"Administrator", "Guru"});
        cbRole.setFont(Theme.FONT_BODY);
        cbRole.setMaximumSize(new Dimension(FORM_FIELD_MAX_WIDTH, 36));
        cbRole.setAlignmentX(LEFT_ALIGNMENT);
        installAutoScrollOnFocus(cbRole);
        cbRole.addActionListener(e -> syncKelasByRole());

        kelasOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        kelasOptionsPanel.setOpaque(false);
        kelasOptionsPanel.setAlignmentX(LEFT_ALIGNMENT);
        kelasOptionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        rebuildKelasChecks(null);

        JTextArea lblInfo = new JTextArea("* wajib diisi | role Guru akan otomatis muncul juga di Data Guru");
        lblInfo.setFont(Theme.FONT_SMALL.deriveFont(Font.ITALIC));
        lblInfo.setForeground(Theme.TEXT_GRAY);
        lblInfo.setOpaque(false);
        lblInfo.setEditable(false);
        lblInfo.setFocusable(false);
        lblInfo.setLineWrap(true);
        lblInfo.setWrapStyleWord(true);
        lblInfo.setAlignmentX(LEFT_ALIGNMENT);
        lblInfo.setMaximumSize(new Dimension(FORM_FIELD_MAX_WIDTH, Integer.MAX_VALUE));

        JPanel row1 = new JPanel(new GridLayout(1, 2, 8, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(FORM_FIELD_MAX_WIDTH, 38));
        btnSimpan = makeBtn("💾 Simpan", Theme.BLUE_BTN, e -> simpan());
        btnBatal = makeBtn("✕ Batal", Theme.GRAY_BTN, e -> clearForm());
        row1.add(btnSimpan);
        row1.add(btnBatal);

        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);
        row2.setAlignmentX(LEFT_ALIGNMENT);
        btnHapus = makeBtn("🗑 Hapus Pengguna Ini", Theme.RED, e -> hapus());
        btnHapus.setEnabled(false);
        row2.add(btnHapus);

        content.add(lblFormTitle);
        content.add(Box.createVerticalStrut(16));
        content.add(fLabel("NIP"));            content.add(Box.createVerticalStrut(4));
        content.add(tfNip);                     content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Username *"));     content.add(Box.createVerticalStrut(4));
        content.add(tfUsername);                content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Password *"));     content.add(Box.createVerticalStrut(4));
        content.add(pfPassword);                content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Nama *"));         content.add(Box.createVerticalStrut(4));
        content.add(tfNama);                    content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Role *"));         content.add(Box.createVerticalStrut(4));
        content.add(cbRole);                    content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Kelas"));          content.add(Box.createVerticalStrut(4));
        content.add(kelasOptionsPanel);         content.add(Box.createVerticalStrut(8));
        content.add(lblInfo);
        content.add(Box.createVerticalGlue());

        formScroll = new JScrollPane(content);
        formScroll.setBorder(null);
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        formScroll.getViewport().setOpaque(false);
        formScroll.setOpaque(false);
        form.add(formScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        buttonPanel.add(row1);
        buttonPanel.add(Box.createVerticalStrut(8));
        buttonPanel.add(row2);
        form.add(buttonPanel, BorderLayout.SOUTH);

        return form;
    }

    // Menangani proses: mulai tambah.
    private void mulaiTambah() {
        editingUsername = null;
        clearFields();
        lblFormTitle.setText("FORM TAMBAH PENGGUNA");
        btnHapus.setEnabled(false);
        scrollFormToTopAndFocusFirst();
        table.clearSelection();
    }

    // Menangani proses: pilih pengguna.
    private void pilihPengguna(Guru g) {
        editingUsername = g.getUsername();
        setTextValue(tfNip, g.getNip(), "Nomor induk pegawai (NIP)");
        tfUsername.setText(g.getUsername()); tfUsername.setForeground(Theme.TEXT_DARK);
        setPasswordPlaceholder(pfPassword, "Kosongkan jika tidak diubah");
        tfNama.setText(g.getNamaLengkap()); tfNama.setForeground(Theme.TEXT_DARK);
        cbRole.setSelectedItem(g.getRole() == null || g.getRole().isEmpty() ? "Guru" : g.getRole());
        rebuildKelasChecks(g.getKelasAmpu());
        syncKelasByRole();
        lblFormTitle.setText("EDIT PENGGUNA (" + g.getUsername() + ")");
        btnHapus.setEnabled(true);
        scrollFormToTopAndFocusFirst();
    }

    // Menyimpan data form ke database.
    private void simpan() {
        String nip = getFieldText(tfNip, "Nomor induk pegawai (NIP)");
        String username = getFieldText(tfUsername, "Username login");
        String password = new String(pfPassword.getPassword()).trim();
        String nama = getFieldText(tfNama, "Nama lengkap pengguna");
        String role = (String) cbRole.getSelectedItem();
        String kelas = getKelasSelectedCsv();

        if (username.isEmpty()) { showError("Username tidak boleh kosong!"); tfUsername.requestFocus(); return; }
        if (nama.isEmpty()) { showError("Nama tidak boleh kosong!"); tfNama.requestFocus(); return; }
        boolean keepOldPassword = editingUsername != null &&
                (password.isEmpty() || password.equals("Kosongkan jika tidak diubah"));
        if (!keepOldPassword) {
            if (password.isEmpty() || password.equals("Password minimal 6 karakter")) { showError("Password tidak boleh kosong!"); pfPassword.requestFocus(); return; }
            if (password.length() < 6) { showError("Password minimal 6 karakter!"); pfPassword.requestFocus(); return; }
        }
        if (!username.matches("[a-zA-Z0-9_]+")) { showError("Username hanya boleh huruf, angka, dan _"); tfUsername.requestFocus(); return; }
        if (!nip.isEmpty() && !nip.matches("\\d+")) { showError("NIP hanya boleh angka!"); tfNip.requestFocus(); return; }

        if ("Administrator".equals(role)) kelas = "Semua";
        if (!"Administrator".equals(role) && (kelas == null || kelas.isEmpty())) {
            showError("Pilih minimal 1 kelas untuk role Guru!");
            return;
        }

        Guru g;
        if (keepOldPassword) {
            Guru existing = ds.getGuruByUsername(editingUsername);
            if (existing == null) { showError("Data pengguna tidak ditemukan."); return; }
            g = new Guru(nip, nama, "", "", "", "", "", kelas, role, username, existing.getPassword());
        } else {
            g = new Guru(nip, nama, "", "", "", "", "", kelas, role, username, password);
        }

        boolean ok;
        if (editingUsername == null) {
            ok = ds.tambahGuru(g);
            if (!ok) {
                showError("Username '" + username + "' sudah digunakan!");
                return;
            }
            showSuccess("Pengguna berhasil ditambahkan.");
        } else {
            ok = ds.updateGuru(editingUsername, g);
            if (!ok) {
                showError("Gagal update pengguna. Username mungkin sudah digunakan.");
                return;
            }
            showSuccess("Data pengguna berhasil diperbarui.");
        }

        loadTable();
        clearForm();
    }

    // Menghapus data yang sedang dipilih.
    private void hapus() {
        if (editingUsername == null) return;

        Guru guruLogin = ds.getGuruLogin();
        if (guruLogin != null && editingUsername.equals(guruLogin.getUsername())) {
            showError("Akun yang sedang login tidak bisa dihapus.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Hapus pengguna '" + editingUsername + "'?\nData yang dihapus tidak dapat dikembalikan.",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (ds.hapusGuru(editingUsername)) {
            showSuccess("Pengguna berhasil dihapus.");
            loadTable();
            clearForm();
        } else {
            showError("Gagal menghapus pengguna.");
        }
    }

    // Menangani proses: clear form.
    private void clearForm() {
        editingUsername = null;
        clearFields();
        lblFormTitle.setText("FORM TAMBAH PENGGUNA");
        btnHapus.setEnabled(false);
        table.clearSelection();
    }

    // Menangani proses: clear fields.
    private void clearFields() {
        setPlaceholder(tfNip, "Nomor induk pegawai (NIP)");
        setPlaceholder(tfUsername, "Username login");
        setPasswordPlaceholder(pfPassword, "Password minimal 6 karakter");
        setPlaceholder(tfNama, "Nama lengkap pengguna");
        cbRole.setSelectedItem("Guru");
        rebuildKelasChecks(null);
        syncKelasByRole();
    }

    // Memuat data terbaru ke tabel.
    public void loadTable() {
        rebuildKelasChecks(getKelasSelectedCsv());
        List<Guru> all = ds.getDaftarGuru();
        String filterRole = getSelectedFilterRole();

        tableModel.setRowCount(0);
        int total = 0;
        int totalAdmin = 0;
        int totalGuru = 0;

        for (Guru g : all) {
            String role = g.getRole() == null || g.getRole().isEmpty() ? "Guru" : g.getRole();
            if (!"Semua".equals(filterRole) && !filterRole.equals(role)) continue;
            total++;
            if ("Administrator".equals(role)) totalAdmin++; else totalGuru++;
            tableModel.addRow(new Object[]{
                g.getNip(), g.getUsername(), "********", g.getNamaLengkap(), role, normalizeKelas(g.getKelasAmpu())
            });
        }
        statusSummaryText = "Total: " + total + " akun  |  Administrator: " + totalAdmin + "  |  Guru: " + totalGuru;
        renderWrappedStatus();
    }

    // Menangani proses: render wrapped status.
    private void renderWrappedStatus() {
        if (lblStatus == null) return;
        if (statusSummaryText == null || statusSummaryText.isEmpty()) {
            lblStatus.setText("");
            return;
        }
        int available = 460;
        if (statusRowTop != null && btnTambah != null) {
            int gap = 18;
            available = statusRowTop.getWidth() - btnTambah.getPreferredSize().width - gap;
        }
        if (available <= 140) {
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

    // Menangani proses: get selected filter role.
    private String getSelectedFilterRole() {
        Object selected = cbFilterRole.getSelectedItem();
        return selected == null ? "Semua" : selected.toString();
    }

    // Menangani proses: sync kelas by role.
    private void syncKelasByRole() {
        String role = (String) cbRole.getSelectedItem();
        boolean admin = "Administrator".equals(role);
        for (JCheckBox cb : kelasChecks.values()) cb.setEnabled(!admin);
        if (admin) {
            for (JCheckBox cb : kelasChecks.values()) cb.setSelected(false);
        }
    }

    // Menangani proses: normalize kelas.
    private String normalizeKelas(String kelas) {
        if (kelas == null || kelas.trim().isEmpty()) return "Semua";
        if ("A,B".equals(kelas) || "B,A".equals(kelas)) return "A,B";
        return kelas;
    }

    // Menangani proses: rebuild kelas checks.
    private void rebuildKelasChecks(String selectedCsv) {
        if (kelasOptionsPanel == null) return;
        kelasChecks.clear();
        kelasOptionsPanel.removeAll();
        String csv = selectedCsv == null ? "" : selectedCsv;
        for (Kelas k : ds.getDaftarKelas()) {
            JCheckBox cb = new JCheckBox("Kelas " + k.getNama());
            cb.setOpaque(false);
            cb.setFont(Theme.FONT_BODY);
            cb.setFocusPainted(false);
            installAutoScrollOnFocus(cb);
            cb.setSelected(containsKelas(csv, k.getNama()));
            kelasChecks.put(k.getNama(), cb);
            kelasOptionsPanel.add(cb);
        }
        kelasOptionsPanel.revalidate();
        kelasOptionsPanel.repaint();
    }

    // Menangani proses: contains kelas.
    private boolean containsKelas(String csv, String kelas) {
        if (csv == null || csv.trim().isEmpty()) return false;
        for (String p : csv.split(",")) {
            if (kelas.equalsIgnoreCase(p.trim())) return true;
        }
        return false;
    }

    // Menangani proses: get kelas selected csv.
    private String getKelasSelectedCsv() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, JCheckBox> e : kelasChecks.entrySet()) {
            if (!e.getValue().isSelected()) continue;
            if (sb.length() > 0) sb.append(",");
            sb.append(e.getKey());
        }
        return sb.toString();
    }

    // Menangani proses: f label.
    private JLabel fLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(Theme.FONT_HEADER);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    // Menangani proses: f field.
    private JTextField fField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(Theme.FONT_BODY);
        tf.setMaximumSize(new Dimension(FORM_FIELD_MAX_WIDTH, 36));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        setPlaceholder(tf, placeholder);
        tf.addFocusListener(new FocusAdapter() {
            // Menangani proses: focus gained.
            public void focusGained(FocusEvent e) {
                ensureFieldVisible(tf);
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Theme.TEXT_DARK);
                }
            }
            // Menangani proses: focus lost.
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) setPlaceholder(tf, placeholder);
            }
        });
        return tf;
    }

    // Menangani proses: f password.
    private JPasswordField fPassword(String placeholder) {
        JPasswordField pf = new JPasswordField();
        pf.setFont(Theme.FONT_BODY);
        pf.setMaximumSize(new Dimension(FORM_FIELD_MAX_WIDTH, 36));
        pf.setAlignmentX(LEFT_ALIGNMENT);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        setPasswordPlaceholder(pf, placeholder);
        pf.addFocusListener(new FocusAdapter() {
            // Menangani proses: focus gained.
            public void focusGained(FocusEvent e) {
                ensureFieldVisible(pf);
                if (pf.getForeground().equals(Theme.TEXT_GRAY)) {
                    pf.setText("");
                    pf.setEchoChar('●');
                    pf.setForeground(Theme.TEXT_DARK);
                }
            }
            // Menangani proses: focus lost.
            public void focusLost(FocusEvent e) {
                if (pf.getPassword().length == 0) setPasswordPlaceholder(pf, placeholder);
            }
        });
        return pf;
    }

    // Menangani proses: set placeholder.
    private void setPlaceholder(JTextField tf, String ph) {
        tf.setText(ph);
        tf.setForeground(Theme.TEXT_GRAY);
    }

    // Menangani proses: set password placeholder.
    private void setPasswordPlaceholder(JPasswordField pf, String ph) {
        pf.setEchoChar((char) 0);
        pf.setText(ph);
        pf.setForeground(Theme.TEXT_GRAY);
    }

    // Menangani proses: get field text.
    private String getFieldText(JTextField tf, String placeholder) {
        String t = tf.getText().trim();
        return t.equals(placeholder) ? "" : t;
    }

    // Menangani proses: set text value.
    private void setTextValue(JTextField tf, String value, String placeholder) {
        if (value == null || value.isEmpty()) {
            setPlaceholder(tf, placeholder);
        } else {
            tf.setText(value);
            tf.setForeground(Theme.TEXT_DARK);
        }
    }

    // Menangani proses: install auto scroll on focus.
    private void installAutoScrollOnFocus(JComponent comp) {
        comp.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                ensureFieldVisible(comp);
            }
        });
    }

    // Menangani proses: ensure field visible.
    private void ensureFieldVisible(JComponent comp) {
        if (formScroll == null || comp == null) return;
        SwingUtilities.invokeLater(() -> {
            Component view = formScroll.getViewport().getView();
            Rectangle r = SwingUtilities.convertRectangle(comp.getParent(), comp.getBounds(), view);
            r.y = Math.max(0, r.y - 8);
            r.height += 30;
            r.x = 0;
            r.width = 1;
            formScroll.getViewport().scrollRectToVisible(r);
        });
    }

    // Menangani proses: scroll form to top and focus first.
    private void scrollFormToTopAndFocusFirst() {
        if (formScroll != null) {
            SwingUtilities.invokeLater(() -> formScroll.getVerticalScrollBar().setValue(0));
        }
        if (tfNip != null) {
            SwingUtilities.invokeLater(() -> tfNip.requestFocusInWindow());
        }
    }

    // Menangani proses: make btn.
    private JButton makeBtn(String text, Color bg, ActionListener al) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = !isEnabled() ? new Color(180, 180, 180) :
                        getModel().isPressed() ? bg.darker() : bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
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
