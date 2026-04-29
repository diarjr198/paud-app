package com.mycompany.paud;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataGuruPanel extends JPanel {

    private final DataStore ds = DataStore.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;
    private JScrollPane formScroll;

    private JTextField tfNip, tfNama, tfTempatLahir, tfAlamat, tfUsername;
    private JSpinner spTanggalLahir;
    private JPasswordField pfPassword;
    private JComboBox<String> cbJenisKelamin, cbAgama, cbFilterKelas;
    private JPanel kelasOptionsPanel;
    private Map<String, JCheckBox> kelasChecks = new LinkedHashMap<>();

    private JLabel lblFormTitle, lblStatus;
    private JButton btnSimpan, btnBatal, btnHapus, btnTambah;
    private JPanel statusRowTop;
    private String statusSummaryText = "";

    private String editingUsername = null;
    private boolean ignoreFilterEvent = false;
    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] COLUMNS = {
        "Username", "NIP", "Nama Guru", "Tempat Lahir", "Tanggal Lahir", "Jenis Kelamin", "Agama", "Kelas", "Alamat"
    };

    public DataGuruPanel() {
        setLayout(new BorderLayout(16, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        add(buildTableSide(), BorderLayout.CENTER);
        add(buildFormSide(), BorderLayout.EAST);
        loadTable();
    }

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
        btnTambah = makeBtn("+ Tambah Guru", Theme.GREEN, e -> mulaiTambah());
        btnTambah.setPreferredSize(new Dimension(150, 34));
        statusRowTop.add(lblStatus, BorderLayout.WEST);
        statusRowTop.add(btnTambah, BorderLayout.EAST);
        statusRowTop.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                renderWrappedStatus();
            }
        });

        JPanel rowBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rowBottom.setOpaque(false);
        JLabel lblFilter = new JLabel("Filter Kelas:");
        lblFilter.setFont(Theme.FONT_BODY);
        rowBottom.add(lblFilter);

        cbFilterKelas = new JComboBox<>();
        cbFilterKelas.setFont(Theme.FONT_BODY);
        cbFilterKelas.setPreferredSize(new Dimension(90, 30));
        cbFilterKelas.addActionListener(e -> {
            if (!ignoreFilterEvent) loadTable();
        });
        rowBottom.add(cbFilterKelas);
        refreshKelasFilterOptions();

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

        table.getColumnModel().getColumn(0).setPreferredWidth(120); // Username
        table.getColumnModel().getColumn(1).setPreferredWidth(110); // NIP
        table.getColumnModel().getColumn(2).setPreferredWidth(180); // Nama
        table.getColumnModel().getColumn(3).setPreferredWidth(140); // Tempat Lahir
        table.getColumnModel().getColumn(4).setPreferredWidth(110); // Tanggal Lahir
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // JK
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Agama
        table.getColumnModel().getColumn(7).setPreferredWidth(90);  // Kelas
        table.getColumnModel().getColumn(8).setPreferredWidth(220); // Alamat

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String username = tableModel.getValueAt(table.convertRowIndexToModel(row), 0).toString();
                    Guru g = ds.getGuruByUsername(username);
                    if (g != null) pilihGuru(g);
                }
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

    private JPanel buildFormSide() {
        RoundedPanel form = Theme.makeCard(16);
        form.setLayout(new BorderLayout(0, 0));
        form.setPreferredSize(new Dimension(360, 0));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 20, 12, 20));

        lblFormTitle = new JLabel("FORM TAMBAH GURU");
        lblFormTitle.setFont(Theme.FONT_HEADER);
        lblFormTitle.setAlignmentX(LEFT_ALIGNMENT);

        tfNip = fField("Nomor induk pegawai (NIP)");
        tfNama = fField("Nama lengkap guru");
        tfTempatLahir = fField("Kota/kabupaten");
        spTanggalLahir = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dobEditor = new JSpinner.DateEditor(spTanggalLahir, "dd/MM/yyyy");
        spTanggalLahir.setEditor(dobEditor);
        JFormattedTextField tfDob = ((JSpinner.DefaultEditor) spTanggalLahir.getEditor()).getTextField();
        tfDob.setEditable(true);
        tfDob.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        dobEditor.getFormat().setLenient(false);
        spTanggalLahir.setValue(java.sql.Date.valueOf(LocalDate.now()));
        spTanggalLahir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        spTanggalLahir.setAlignmentX(LEFT_ALIGNMENT);
        installAutoScrollOnFocus(spTanggalLahir);
        tfAlamat = fField("Alamat lengkap");
        tfUsername = fField("Username login");
        pfPassword = fPassword("Password minimal 6 karakter");

        cbJenisKelamin = new JComboBox<>(new String[]{"Laki-laki", "Perempuan"});
        cbJenisKelamin.setFont(Theme.FONT_BODY);
        cbJenisKelamin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbJenisKelamin.setAlignmentX(LEFT_ALIGNMENT);
        installAutoScrollOnFocus(cbJenisKelamin);

        cbAgama = new JComboBox<>(new String[]{"Islam", "Kristen", "Hindu", "Budha", "Konghucu"});
        cbAgama.setFont(Theme.FONT_BODY);
        cbAgama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbAgama.setAlignmentX(LEFT_ALIGNMENT);
        installAutoScrollOnFocus(cbAgama);

        kelasOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        kelasOptionsPanel.setOpaque(false);
        kelasOptionsPanel.setAlignmentX(LEFT_ALIGNMENT);
        kelasOptionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        rebuildKelasChecks(null);

        JLabel lblInfo = new JLabel("<html><i>* wajib diisi | klik baris tabel untuk edit</i></html>");
        lblInfo.setFont(Theme.FONT_SMALL);
        lblInfo.setForeground(Theme.TEXT_GRAY);
        lblInfo.setAlignmentX(LEFT_ALIGNMENT);

        JPanel row1 = new JPanel(new GridLayout(1, 2, 8, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnSimpan = makeBtn("💾 Simpan", Theme.BLUE_BTN, e -> simpan());
        btnBatal = makeBtn("✕ Batal", Theme.GRAY_BTN, e -> clearForm());
        row1.add(btnSimpan);
        row1.add(btnBatal);

        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);
        row2.setAlignmentX(LEFT_ALIGNMENT);
        btnHapus = makeBtn("🗑 Hapus Guru Ini", Theme.RED, e -> hapus());
        btnHapus.setEnabled(false);
        row2.add(btnHapus);

        content.add(lblFormTitle);
        content.add(Box.createVerticalStrut(16));
        content.add(fLabel("NIP"));                   content.add(Box.createVerticalStrut(4));
        content.add(tfNip);                            content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Nama Guru *"));           content.add(Box.createVerticalStrut(4));
        content.add(tfNama);                           content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Tempat Lahir"));          content.add(Box.createVerticalStrut(4));
        content.add(tfTempatLahir);                    content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Tanggal Lahir"));         content.add(Box.createVerticalStrut(4));
        content.add(spTanggalLahir);                   content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Jenis Kelamin *"));       content.add(Box.createVerticalStrut(4));
        content.add(cbJenisKelamin);                   content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Agama"));                 content.add(Box.createVerticalStrut(4));
        content.add(cbAgama);                          content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Alamat"));                content.add(Box.createVerticalStrut(4));
        content.add(tfAlamat);                         content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Kelas Diampu *"));        content.add(Box.createVerticalStrut(4));
        content.add(kelasOptionsPanel);                content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Username *"));            content.add(Box.createVerticalStrut(4));
        content.add(tfUsername);                       content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Password *"));            content.add(Box.createVerticalStrut(4));
        content.add(pfPassword);                       content.add(Box.createVerticalStrut(8));
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

    private void mulaiTambah() {
        editingUsername = null;
        clearFields();
        lblFormTitle.setText("FORM TAMBAH GURU");
        btnHapus.setEnabled(false);
        scrollFormToTopAndFocusFirst();
        table.clearSelection();
    }

    private void pilihGuru(Guru g) {
        editingUsername = g.getUsername();
        setTextValue(tfNip, g.getNip(), "Nomor induk pegawai (NIP)");
        tfNama.setText(g.getNamaLengkap()); tfNama.setForeground(Theme.TEXT_DARK);
        setTextValue(tfTempatLahir, g.getTempatLahir(), "Kota/kabupaten");
        setTanggalLahirValue(g.getTanggalLahir());
        setTextValue(tfAlamat, g.getAlamat(), "Alamat lengkap");
        tfUsername.setText(g.getUsername()); tfUsername.setForeground(Theme.TEXT_DARK);
        setPasswordPlaceholder(pfPassword, "Kosongkan jika tidak diubah");

        String jk = g.getJenisKelamin();
        if (jk == null || jk.isEmpty()) jk = "Laki-laki";
        cbJenisKelamin.setSelectedItem(jk);

        String agama = g.getAgama();
        if (agama == null || agama.isEmpty()) agama = "Islam";
        cbAgama.setSelectedItem(agama);

        rebuildKelasChecks(g.getKelasAmpu());

        lblFormTitle.setText("EDIT GURU (" + g.getUsername() + ")");
        btnHapus.setEnabled(true);
        scrollFormToTopAndFocusFirst();
    }

    private void simpan() {
        String nip = getFieldText(tfNip, "Nomor induk pegawai (NIP)");
        String nama = getFieldText(tfNama, "Nama lengkap guru");
        String tempatLahir = getFieldText(tfTempatLahir, "Kota/kabupaten");
        String tanggalLahir = getTanggalLahirValue();
        String alamat = getFieldText(tfAlamat, "Alamat lengkap");
        String username = getFieldText(tfUsername, "Username login");
        String password = new String(pfPassword.getPassword()).trim();
        String jk = (String) cbJenisKelamin.getSelectedItem();
        String agama = (String) cbAgama.getSelectedItem();
        String kelasAmpu = getKelasAmpuValue();

        if (nama.isEmpty()) { showError("Nama guru tidak boleh kosong!"); tfNama.requestFocus(); return; }
        if (username.isEmpty()) { showError("Username tidak boleh kosong!"); tfUsername.requestFocus(); return; }
        boolean keepOldPassword = editingUsername != null &&
                (password.isEmpty() || password.equals("Kosongkan jika tidak diubah"));
        if (!keepOldPassword) {
            if (password.isEmpty() || password.equals("Password minimal 6 karakter")) {
                showError("Password tidak boleh kosong!"); pfPassword.requestFocus(); return;
            }
            if (password.length() < 6) { showError("Password minimal 6 karakter!"); pfPassword.requestFocus(); return; }
        }
        if (!username.matches("[a-zA-Z0-9_]+")) {
            showError("Username hanya boleh huruf, angka, dan _"); tfUsername.requestFocus(); return;
        }
        if (!nip.isEmpty() && !nip.matches("\\d+")) {
            showError("NIP hanya boleh angka!"); tfNip.requestFocus(); return;
        }
        if (tanggalLahir.isEmpty()) {
            showError("Tanggal lahir tidak valid!"); spTanggalLahir.requestFocusInWindow(); return;
        }
        if (kelasAmpu.isEmpty()) {
            showError("Pilih minimal 1 kelas yang diampu!");
            return;
        }

        Guru g;
        if (keepOldPassword) {
            Guru existing = ds.getGuruByUsername(editingUsername);
            if (existing == null) { showError("Data guru tidak ditemukan."); return; }
            g = new Guru(nip, nama, tempatLahir, tanggalLahir, jk, alamat, agama, kelasAmpu, "Guru", username, existing.getPassword());
        } else {
            g = new Guru(nip, nama, tempatLahir, tanggalLahir, jk, alamat, agama, kelasAmpu, "Guru", username, password);
        }
        boolean ok;
        if (editingUsername == null) {
            ok = ds.tambahGuru(g);
            if (!ok) {
                showError("Username '" + username + "' sudah digunakan!");
                return;
            }
            showSuccess("Guru berhasil ditambahkan.");
        } else {
            ok = ds.updateGuru(editingUsername, g);
            if (!ok) {
                showError("Gagal update guru. Username mungkin sudah digunakan.");
                return;
            }
            showSuccess("Data guru berhasil diperbarui.");
        }

        loadTable();
        clearForm();
    }

    private void hapus() {
        if (editingUsername == null) return;

        Guru guruLogin = ds.getGuruLogin();
        if (guruLogin != null && editingUsername.equals(guruLogin.getUsername())) {
            showError("Akun yang sedang login tidak bisa dihapus.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Hapus guru '" + editingUsername + "'?\nData yang dihapus tidak dapat dikembalikan.",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (ds.hapusGuru(editingUsername)) {
            showSuccess("Guru berhasil dihapus.");
            loadTable();
            clearForm();
        } else {
            showError("Gagal menghapus guru.");
        }
    }

    private void clearForm() {
        editingUsername = null;
        clearFields();
        lblFormTitle.setText("FORM TAMBAH GURU");
        btnHapus.setEnabled(false);
        table.clearSelection();
    }

    private void clearFields() {
        setPlaceholder(tfNip, "Nomor induk pegawai (NIP)");
        setPlaceholder(tfNama, "Nama lengkap guru");
        setPlaceholder(tfTempatLahir, "Kota/kabupaten");
        setTanggalLahirValue(null);
        setPlaceholder(tfAlamat, "Alamat lengkap");
        setPlaceholder(tfUsername, "Username login");
        setPasswordPlaceholder(pfPassword, "Password minimal 6 karakter");

        cbJenisKelamin.setSelectedIndex(0);
        cbAgama.setSelectedIndex(0);
        rebuildKelasChecks(null);
    }

    public void loadTable() {
        refreshKelasFilterOptions();
        List<Guru> all = ds.getDaftarGuru();
        String filter = getSelectedFilterKelas();

        tableModel.setRowCount(0);
        int totalGuru = 0;
        Map<String, Integer> countPerKelas = new LinkedHashMap<>();
        for (Kelas k : ds.getDaftarKelas()) countPerKelas.put(k.getNama(), 0);
        for (Guru g : all) {
            if (!"Guru".equals(g.getRole())) continue;
            totalGuru++;
            String kelas = g.getKelasAmpu() == null ? "" : g.getKelasAmpu();
            for (String namaKelas : countPerKelas.keySet()) {
                if (containsKelas(kelas, namaKelas)) {
                    countPerKelas.put(namaKelas, countPerKelas.get(namaKelas) + 1);
                }
            }
            if (!matchFilterKelas(g.getKelasAmpu(), filter)) continue;
            tableModel.addRow(new Object[]{
                g.getUsername(), g.getNip(), g.getNamaLengkap(), g.getTempatLahir(),
                g.getTanggalLahir(), g.getJenisKelamin(), g.getAgama(), g.getKelasAmpu(), g.getAlamat()
            });
        }
        updateStatus(totalGuru, countPerKelas);
    }

    private boolean matchFilterKelas(String kelasAmpu, String filter) {
        if ("Semua".equals(filter)) return true;
        String v = kelasAmpu == null ? "" : kelasAmpu;
        return v.contains(filter);
    }

    private String getSelectedFilterKelas() {
        Object selected = cbFilterKelas.getSelectedItem();
        return selected == null ? "Semua" : selected.toString();
    }

    private void refreshKelasFilterOptions() {
        if (cbFilterKelas == null) return;
        ignoreFilterEvent = true;
        String current = (String) cbFilterKelas.getSelectedItem();
        cbFilterKelas.removeAllItems();
        cbFilterKelas.addItem("Semua");
        for (Kelas k : ds.getDaftarKelas()) cbFilterKelas.addItem(k.getNama());
        if (current != null) cbFilterKelas.setSelectedItem(current);
        if (cbFilterKelas.getSelectedItem() == null) cbFilterKelas.setSelectedItem("Semua");
        ignoreFilterEvent = false;
    }

    private void updateStatus(int totalGuru, Map<String, Integer> countPerKelas) {
        StringBuilder sb = new StringBuilder("Total: ").append(totalGuru).append(" guru");
        for (Map.Entry<String, Integer> e : countPerKelas.entrySet()) {
            sb.append("  |  Pengampu ").append(e.getKey()).append(": ").append(e.getValue());
        }
        statusSummaryText = sb.toString();
        renderWrappedStatus();
    }

    private void renderWrappedStatus() {
        if (lblStatus == null) return;
        if (statusSummaryText == null || statusSummaryText.isEmpty()) {
            lblStatus.setText("");
            return;
        }
        int available = 480;
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

    private String getKelasAmpuValue() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, JCheckBox> e : kelasChecks.entrySet()) {
            if (!e.getValue().isSelected()) continue;
            if (sb.length() > 0) sb.append(",");
            sb.append(e.getKey());
        }
        return sb.toString();
    }

    private void rebuildKelasChecks(String selectedCsv) {
        kelasChecks.clear();
        kelasOptionsPanel.removeAll();
        String csv = selectedCsv == null ? "" : selectedCsv;
        for (Kelas k : ds.getDaftarKelas()) {
            JCheckBox cb = new JCheckBox("Kelas " + k.getNama());
            styleCheckBox(cb);
            if (containsKelas(csv, k.getNama())) cb.setSelected(true);
            kelasChecks.put(k.getNama(), cb);
            kelasOptionsPanel.add(cb);
        }
        kelasOptionsPanel.revalidate();
        kelasOptionsPanel.repaint();
    }

    private boolean containsKelas(String csv, String kelas) {
        if (csv == null || csv.trim().isEmpty()) return false;
        String[] parts = csv.split(",");
        for (String p : parts) if (kelas.equalsIgnoreCase(p.trim())) return true;
        return false;
    }

    private JLabel fLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(Theme.FONT_HEADER);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField fField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(Theme.FONT_BODY);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        setPlaceholder(tf, placeholder);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                ensureFieldVisible(tf);
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Theme.TEXT_DARK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) setPlaceholder(tf, placeholder);
            }
        });
        return tf;
    }

    private JPasswordField fPassword(String placeholder) {
        JPasswordField pf = new JPasswordField();
        pf.setFont(Theme.FONT_BODY);
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        pf.setAlignmentX(LEFT_ALIGNMENT);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        setPasswordPlaceholder(pf, placeholder);
        pf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                ensureFieldVisible(pf);
                if (pf.getForeground().equals(Theme.TEXT_GRAY)) {
                    pf.setText("");
                    pf.setEchoChar('●');
                    pf.setForeground(Theme.TEXT_DARK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (pf.getPassword().length == 0) setPasswordPlaceholder(pf, placeholder);
            }
        });
        return pf;
    }

    private void setPlaceholder(JTextField tf, String ph) {
        tf.setText(ph);
        tf.setForeground(Theme.TEXT_GRAY);
    }

    private void setPasswordPlaceholder(JPasswordField pf, String ph) {
        pf.setEchoChar((char) 0);
        pf.setText(ph);
        pf.setForeground(Theme.TEXT_GRAY);
    }

    private String getFieldText(JTextField tf, String placeholder) {
        String t = tf.getText().trim();
        return t.equals(placeholder) ? "" : t;
    }

    private void setTextValue(JTextField tf, String value, String placeholder) {
        if (value == null || value.isEmpty()) {
            setPlaceholder(tf, placeholder);
        } else {
            tf.setText(value);
            tf.setForeground(Theme.TEXT_DARK);
        }
    }

    private String getTanggalLahirValue() {
        if (spTanggalLahir == null) return "";
        try {
            spTanggalLahir.commitEdit();
        } catch (java.text.ParseException ex) {
            return "";
        }
        java.util.Date d = (java.util.Date) spTanggalLahir.getValue();
        LocalDate ld = d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return ld.format(FMT_TGL);
    }

    private void setTanggalLahirValue(String tgl) {
        if (spTanggalLahir == null) return;
        LocalDate value = LocalDate.now();
        if (tgl != null && !tgl.trim().isEmpty()) {
            try {
                value = LocalDate.parse(tgl.trim(), FMT_TGL);
            } catch (DateTimeParseException ignored) {
            }
        }
        spTanggalLahir.setValue(java.sql.Date.valueOf(value));
    }

    private void installAutoScrollOnFocus(JComponent comp) {
        comp.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                ensureFieldVisible(comp);
            }
        });
    }

    private void ensureFieldVisible(JComponent comp) {
        if (formScroll == null || comp == null) return;
        SwingUtilities.invokeLater(() -> {
            Component view = formScroll.getViewport().getView();
            Rectangle r = SwingUtilities.convertRectangle(comp.getParent(), comp.getBounds(), view);
            r.y = Math.max(0, r.y - 8);
            r.height += 30;
            formScroll.getViewport().scrollRectToVisible(r);
        });
    }

    private void scrollFormToTopAndFocusFirst() {
        if (formScroll != null) {
            SwingUtilities.invokeLater(() -> formScroll.getVerticalScrollBar().setValue(0));
        }
        if (tfNip != null) {
            SwingUtilities.invokeLater(() -> tfNip.requestFocusInWindow());
        }
    }

    private void styleCheckBox(JCheckBox cb) {
        cb.setOpaque(false);
        cb.setFont(Theme.FONT_BODY);
        cb.setFocusPainted(false);
        installAutoScrollOnFocus(cb);
    }

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

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Berhasil", JOptionPane.INFORMATION_MESSAGE);
    }
}
