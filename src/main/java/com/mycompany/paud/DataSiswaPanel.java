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
import java.awt.event.*;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DataSiswaPanel extends JPanel {

    private DataStore ds = DataStore.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;
    private JScrollPane formScroll;
    private JTextField tfNama, tfNamaWali, tfNpd, tfNisn, tfTempatLahir, tfAlamat, tfKontakWali;
    private JSpinner spTanggalLahir;
    private JComboBox<String> cbKelas, cbJenisKelamin, cbFilterKelas, cbAngkatan, cbAgama;
    private JLabel lblFormTitle, lblStatus;
    private JButton btnSimpan, btnBatal, btnHapus, btnTambah;
    private JPanel statusRowTop;
    private String statusSummaryText = "";
    private int editingId = -1;
    private boolean ignoreFilterEvent = false;
    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] COLUMNS = {
        "ID", "NPD", "NISN", "Nama Anak", "Kelas", "Angkatan", "Jenis Kelamin", "Agama", "Tempat Lahir", "Alamat", "Tgl Lahir", "Nama Wali", "Kontak Wali"
    };

    // Menjalankan inisialisasi objek DataSiswaPanel.
    public DataSiswaPanel() {
        setLayout(new BorderLayout(16, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        add(buildTableSide(), BorderLayout.CENTER);
        add(buildFormSide(),  BorderLayout.EAST);
        loadTable();
    }

    // ==================== TABLE SIDE ====================
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
        btnTambah = makeBtn("+ Tambah Siswa", Theme.GREEN, e -> mulaiTambah());
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
        table.setGridColor(new Color(235,235,235));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(235,245,255));
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.setSelectionBackground(new Color(180,220,255));
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.setRowSorter(new TableRowSorter<>(tableModel));

        // Sembunyikan kolom ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Lebar kolom agar tidak berdesakan (scroll horizontal aktif)
        table.getColumnModel().getColumn(1).setPreferredWidth(90);   // NPD
        table.getColumnModel().getColumn(2).setPreferredWidth(110);  // NISN
        table.getColumnModel().getColumn(3).setPreferredWidth(170);  // Nama Anak
        table.getColumnModel().getColumn(4).setPreferredWidth(70);   // Kelas
        table.getColumnModel().getColumn(5).setPreferredWidth(90);   // Angkatan
        table.getColumnModel().getColumn(6).setPreferredWidth(110);  // Jenis Kelamin
        table.getColumnModel().getColumn(7).setPreferredWidth(110);  // Agama
        table.getColumnModel().getColumn(8).setPreferredWidth(130);  // Tempat Lahir
        table.getColumnModel().getColumn(9).setPreferredWidth(180);  // Alamat
        table.getColumnModel().getColumn(10).setPreferredWidth(100); // Tgl Lahir
        table.getColumnModel().getColumn(11).setPreferredWidth(170); // Nama Wali
        table.getColumnModel().getColumn(12).setPreferredWidth(130); // Kontak Wali

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
                    Siswa s = ds.getSiswaById(id);
                    if (s != null) pilihSiswa(s);
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(210,210,210)));
        sp.getViewport().setBackground(Color.WHITE);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ==================== FORM SIDE ====================
    private JPanel buildFormSide() {
        RoundedPanel form = Theme.makeCard(16);
        form.setLayout(new BorderLayout(0, 0));
        form.setPreferredSize(new Dimension(340, 0));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 20, 12, 20));

        lblFormTitle = new JLabel("FORM TAMBAH SISWA");
        lblFormTitle.setFont(Theme.FONT_HEADER);
        lblFormTitle.setAlignmentX(LEFT_ALIGNMENT);

        tfNpd         = fField("Nomor peserta didik (NPD)");
        tfNisn        = fField("Nomor induk siswa nasional (NISN)");
        tfNama        = fField("Nama lengkap anak");
        tfTempatLahir = fField("Kota/kabupaten");
        tfAlamat      = fField("Alamat lengkap");
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
        tfNamaWali    = fField("Nama wali");
        tfKontakWali  = fField("Nomor HP/WA wali");

        cbKelas = new JComboBox<>();
        cbKelas.setFont(Theme.FONT_BODY);
        cbKelas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbKelas.setAlignmentX(LEFT_ALIGNMENT);
        installAutoScrollOnFocus(cbKelas);
        loadKelasFormOptions();

        cbJenisKelamin = new JComboBox<>(new String[]{"Laki-laki", "Perempuan"});
        cbJenisKelamin.setFont(Theme.FONT_BODY);
        cbJenisKelamin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbJenisKelamin.setAlignmentX(LEFT_ALIGNMENT);
        installAutoScrollOnFocus(cbJenisKelamin);

        cbAgama = new JComboBox<>(new String[]{"Islam", "Kristen", "Hindu", "Budha", "Konghucu"});
        cbAgama.setFont(Theme.FONT_BODY);
        cbAgama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbAgama.setAlignmentX(LEFT_ALIGNMENT);
        cbAgama.setSelectedIndex(0);
        installAutoScrollOnFocus(cbAgama);

        cbAngkatan = new JComboBox<>(buildTahunAngkatanOptions());
        cbAngkatan.setFont(Theme.FONT_BODY);
        cbAngkatan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbAngkatan.setAlignmentX(LEFT_ALIGNMENT);
        cbAngkatan.setSelectedItem(String.valueOf(Year.now().getValue()));
        installAutoScrollOnFocus(cbAngkatan);

        JLabel lblInfo = new JLabel("<html><i>* wajib diisi | klik baris tabel untuk edit</i></html>");
        lblInfo.setFont(Theme.FONT_SMALL);
        lblInfo.setForeground(Theme.TEXT_GRAY);
        lblInfo.setAlignmentX(LEFT_ALIGNMENT);

        // Button row 1
        JPanel row1 = new JPanel(new GridLayout(1,2,8,0));
        row1.setOpaque(false);
        row1.setAlignmentX(LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnSimpan = makeBtn("💾 Simpan", Theme.BLUE_BTN, e -> simpan());
        btnBatal  = makeBtn("✕ Batal",  Theme.GRAY_BTN, e -> clearForm());
        row1.add(btnSimpan);
        row1.add(btnBatal);

        // Button hapus full width
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);
        row2.setAlignmentX(LEFT_ALIGNMENT);
        btnHapus = makeBtn("🗑 Hapus Siswa Ini", Theme.RED, e -> hapus());
        btnHapus.setEnabled(false);
        row2.add(btnHapus);

        content.add(lblFormTitle);
        content.add(Box.createVerticalStrut(16));
        content.add(fLabel("NPD"));                content.add(Box.createVerticalStrut(4));
        content.add(tfNpd);                        content.add(Box.createVerticalStrut(8));
        content.add(fLabel("NISN"));               content.add(Box.createVerticalStrut(4));
        content.add(tfNisn);                       content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Nama Anak *"));        content.add(Box.createVerticalStrut(4));
        content.add(tfNama);                       content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Kelas *"));            content.add(Box.createVerticalStrut(4));
        content.add(cbKelas);                      content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Angkatan *"));         content.add(Box.createVerticalStrut(4));
        content.add(cbAngkatan);                   content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Jenis Kelamin *"));    content.add(Box.createVerticalStrut(4));
        content.add(cbJenisKelamin);               content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Agama"));              content.add(Box.createVerticalStrut(4));
        content.add(cbAgama);                      content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Tempat Lahir"));       content.add(Box.createVerticalStrut(4));
        content.add(tfTempatLahir);                content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Alamat"));             content.add(Box.createVerticalStrut(4));
        content.add(tfAlamat);                     content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Tanggal Lahir"));      content.add(Box.createVerticalStrut(4));
        content.add(spTanggalLahir);               content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Nama Wali *"));        content.add(Box.createVerticalStrut(4));
        content.add(tfNamaWali);                   content.add(Box.createVerticalStrut(8));
        content.add(fLabel("Kontak Wali"));        content.add(Box.createVerticalStrut(4));
        content.add(tfKontakWali);                 content.add(Box.createVerticalStrut(8));
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

    // ==================== ACTIONS ====================
    private void mulaiTambah() {
        editingId = -1;
        clearFields();
        lblFormTitle.setText("FORM TAMBAH SISWA");
        btnHapus.setEnabled(false);
        scrollFormToTopAndFocusFirst();
        table.clearSelection();
    }

    // Menangani proses: pilih siswa.
    private void pilihSiswa(Siswa s) {
        editingId = s.getId();
        setTextValue(tfNpd, s.getNpd(), "Nomor peserta didik (NPD)");
        setTextValue(tfNisn, s.getNisn(), "Nomor induk siswa nasional (NISN)");
        tfNama.setText(s.getNama());             tfNama.setForeground(Theme.TEXT_DARK);
        setTextValue(tfTempatLahir, s.getTempatLahir(), "Kota/kabupaten");
        setTextValue(tfAlamat, s.getAlamat(), "Alamat lengkap");
        String agama = s.getAgama();
        if (agama == null || agama.isEmpty()) agama = "Islam";
        cbAgama.setSelectedItem(agama);
        setTanggalLahirValue(s.getTanggalLahir());
        tfNamaWali.setText(s.getNamaWali()); tfNamaWali.setForeground(Theme.TEXT_DARK);
        setTextValue(tfKontakWali, s.getKontakWali(), "Nomor HP/WA wali");
        cbKelas.setSelectedItem(s.getKelompok());
        String angkatan = s.getAngkatan();
        if (angkatan == null || angkatan.isEmpty()) angkatan = String.valueOf(Year.now().getValue());
        cbAngkatan.setSelectedItem(angkatan);
        String jk = s.getJenisKelamin();
        if (jk == null || jk.isEmpty()) jk = "Laki-laki";
        cbJenisKelamin.setSelectedItem(jk);
        lblFormTitle.setText("EDIT SISWA (ID: " + s.getId() + ")");
        btnHapus.setEnabled(true);
        scrollFormToTopAndFocusFirst();
    }

    // Menyimpan data form ke database.
    private void simpan() {
        String npd  = getFieldText(tfNpd, "Nomor peserta didik (NPD)");
        String nisn = getFieldText(tfNisn, "Nomor induk siswa nasional (NISN)");
        String nama = getFieldText(tfNama,  "Nama lengkap anak");
        String tempatLahir = getFieldText(tfTempatLahir, "Kota/kabupaten");
        String alamat = getFieldText(tfAlamat, "Alamat lengkap");
        String agama = (String) cbAgama.getSelectedItem();
        String tgl = getTanggalLahirValue();
        String namaWali   = getFieldText(tfNamaWali, "Nama wali");
        String kontakWali = getFieldText(tfKontakWali, "Nomor HP/WA wali");
        String kelas = (String) cbKelas.getSelectedItem();
        String angkatan = (String) cbAngkatan.getSelectedItem();
        String jk = (String) cbJenisKelamin.getSelectedItem();

        if (nama.isEmpty()) { showError("Nama anak tidak boleh kosong!"); tfNama.requestFocus(); return; }
        if (namaWali.isEmpty())   { showError("Nama wali tidak boleh kosong!"); tfNamaWali.requestFocus(); return; }
        if (nama.length() < 2) { showError("Nama anak minimal 2 karakter!"); return; }
        if (!npd.isEmpty() && !npd.matches("\\d+")) {
            showError("NPD hanya boleh angka!"); tfNpd.requestFocus(); return;
        }
        if (!nisn.isEmpty() && !nisn.matches("\\d+")) {
            showError("NISN hanya boleh angka!"); tfNisn.requestFocus(); return;
        }
        if (tgl.isEmpty()) { showError("Tanggal lahir tidak valid."); spTanggalLahir.requestFocusInWindow(); return; }
        if (!kontakWali.isEmpty() && !kontakWali.matches("[0-9+\\- ]{8,20}")) {
            showError("Kontak wali tidak valid! Gunakan angka/+/-."); tfKontakWali.requestFocus(); return;
        }

        if (editingId < 0) {
            Siswa baru = new Siswa(0, nama, kelas, namaWali, tgl, npd, nisn, tempatLahir, alamat, jk, agama, kontakWali, angkatan);
            boolean ok = ds.tambahSiswa(baru);
            if (!ok) { showError("Siswa dengan nama '" + nama + "' dan wali '" + namaWali + "' sudah ada!"); return; }
            showSuccess("Siswa \"" + nama + "\" berhasil ditambahkan!\nKelas " + kelas);
        } else {
            ds.updateSiswa(editingId, nama, kelas, namaWali, tgl, npd, nisn, tempatLahir, alamat, jk, agama, kontakWali, angkatan);
            showSuccess("Data siswa berhasil diperbarui!");
        }
        loadTable();
        notifyAngkatanSidebarRefresh();
        clearForm();
    }

    // Menghapus data yang sedang dipilih.
    private void hapus() {
        if (editingId < 0) return;
        Siswa s = ds.getSiswaById(editingId);
        if (s == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Hapus siswa \"" + s.getNama() + "\"?\nData yang dihapus tidak dapat dikembalikan.",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            ds.hapusSiswa(editingId);
            loadTable();
            notifyAngkatanSidebarRefresh();
            clearForm();
            showSuccess("Siswa berhasil dihapus.");
        }
    }

    // Menangani proses: clear form.
    private void clearForm() {
        editingId = -1;
        clearFields();
        lblFormTitle.setText("FORM TAMBAH SISWA");
        btnHapus.setEnabled(false);
        table.clearSelection();
    }

    // Menangani proses: clear fields.
    private void clearFields() {
        setPlaceholder(tfNpd,         "Nomor peserta didik (NPD)");
        setPlaceholder(tfNisn,        "Nomor induk siswa nasional (NISN)");
        setPlaceholder(tfNama,         "Nama lengkap anak");
        setPlaceholder(tfTempatLahir,  "Kota/kabupaten");
        setPlaceholder(tfAlamat,       "Alamat lengkap");
        setTanggalLahirValue(null);
        setPlaceholder(tfNamaWali,     "Nama wali");
        setPlaceholder(tfKontakWali,   "Nomor HP/WA wali");
        cbKelas.setSelectedIndex(0);
        cbAngkatan.setSelectedItem(String.valueOf(Year.now().getValue()));
        cbJenisKelamin.setSelectedIndex(0);
        cbAgama.setSelectedIndex(0);
    }

    // ==================== TABLE ====================
    public void loadTable() {
        List<Siswa> all = ds.getDaftarSiswa();
        refreshKelasFilterOptions();
        String filterKelas = getSelectedFilterKelas();

        tableModel.setRowCount(0);
        for (Siswa s : all) {
            if (!filterKelas.isEmpty() && !filterKelas.equals(s.getKelompok())) continue;
            tableModel.addRow(new Object[]{
                s.getId(), s.getNpd(), s.getNisn(), s.getNama(), s.getKelompok(), s.getAngkatan(),
                s.getJenisKelamin(), s.getAgama(), s.getTempatLahir(), s.getAlamat(), s.getTanggalLahir(),
                s.getNamaWali(), s.getKontakWali()
            });
        }
        updateStatus();
    }

    // Menangani proses: notify angkatan sidebar refresh.
    private void notifyAngkatanSidebarRefresh() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof MainFrame) {
            ((MainFrame) w).refreshAngkatanSidebarOptions(true);
        }
    }

    // Menangani proses: update status.
    private void updateStatus() {
        int total = ds.getTotalSiswa();
        StringBuilder sb = new StringBuilder("Total: ").append(total).append(" siswa");
        List<Siswa> all = ds.getDaftarSiswa();
        for (Kelas k : getVisibleKelasList()) {
            long c = all.stream().filter(s -> k.getNama().equals(s.getKelompok())).count();
            sb.append("  |  Kelas ").append(k.getNama()).append(": ").append(c);
        }
        statusSummaryText = sb.toString();
        renderWrappedStatus();
    }

    // Menangani proses: render wrapped status.
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

    // Menangani proses: refresh kelas filter options.
    private void refreshKelasFilterOptions(List<Siswa> all) {
        refreshKelasFilterOptions();
    }

    // Menangani proses: refresh kelas filter options.
    private void refreshKelasFilterOptions() {
        if (cbFilterKelas == null) return;
        String current = (String) cbFilterKelas.getSelectedItem();
        List<Kelas> kelasList = getVisibleKelasList();
        Set<String> kelasSet = new LinkedHashSet<>();
        for (Kelas k : kelasList) kelasSet.add(k.getNama());

        ignoreFilterEvent = true;
        cbFilterKelas.removeAllItems();
        for (String k : kelasSet) cbFilterKelas.addItem(k);
        if (cbFilterKelas.getItemCount() > 0) {
            if (current != null && kelasSet.contains(current)) cbFilterKelas.setSelectedItem(current);
            else cbFilterKelas.setSelectedIndex(0);
        }
        ignoreFilterEvent = false;
    }

    // Menangani proses: load kelas form options.
    private void loadKelasFormOptions() {
        if (cbKelas == null) return;
        String current = (String) cbKelas.getSelectedItem();
        cbKelas.removeAllItems();
        List<Kelas> kelasList = getVisibleKelasList();
        for (Kelas k : kelasList) cbKelas.addItem(k.getNama());
        if (cbKelas.getItemCount() > 0) {
            if (current != null) cbKelas.setSelectedItem(current);
            if (cbKelas.getSelectedItem() == null) cbKelas.setSelectedIndex(0);
        } else {
            cbKelas.addItem("A");
            cbKelas.setSelectedIndex(0);
        }
    }

    // Menangani proses: get selected filter kelas.
    private String getSelectedFilterKelas() {
        if (cbFilterKelas == null) return "";
        Object selected = cbFilterKelas.getSelectedItem();
        return selected == null ? "" : selected.toString();
    }

    // Menangani proses: build tahun angkatan options.
    private String[] buildTahunAngkatanOptions() {
        int start = Year.now().getValue() + 1;
        int end = 1900;
        String[] years = new String[start - end + 1];
        int idx = 0;
        for (int y = start; y >= end; y--) years[idx++] = String.valueOf(y);
        return years;
    }

    // Menangani proses: get visible kelas list.
    private List<Kelas> getVisibleKelasList() {
        List<Kelas> all = ds.getDaftarKelas();
        if (!ds.isGuruRoleLogin()) return all;
        Set<String> allowed = ds.getKelasAmpuLoginSet();
        List<Kelas> out = new ArrayList<>();
        for (Kelas k : all) {
            if (allowed.contains(k.getNama())) out.add(k);
        }
        return out;
    }

    // ==================== HELPERS ====================
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
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200),1,true),
            new EmptyBorder(4,8,4,8)));
        setPlaceholder(tf, placeholder);
        tf.addFocusListener(new FocusAdapter() {
            // Menangani proses: focus gained.
            public void focusGained(FocusEvent e) {
                ensureFieldVisible(tf);
                if (tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(Theme.TEXT_DARK); }
            }
            // Menangani proses: focus lost.
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) setPlaceholder(tf, placeholder);
            }
        });
        return tf;
    }

    // Menangani proses: set placeholder.
    private void setPlaceholder(JTextField tf, String ph) {
        tf.setText(ph);
        tf.setForeground(Theme.TEXT_GRAY);
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

    // Menangani proses: get tanggal lahir value.
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

    // Menangani proses: set tanggal lahir value.
    private void setTanggalLahirValue(String tgl) {
        if (spTanggalLahir == null) return;
        LocalDate value = LocalDate.now();
        if (tgl != null && !tgl.trim().isEmpty()) {
            try {
                value = LocalDate.parse(tgl.trim(), FMT_TGL);
            } catch (DateTimeParseException ignored) {
                // keep today if old value invalid
            }
        }
        spTanggalLahir.setValue(java.sql.Date.valueOf(value));
    }

    // Menangani proses: scroll form to top and focus first.
    private void scrollFormToTopAndFocusFirst() {
        if (formScroll != null) {
            SwingUtilities.invokeLater(() ->
                formScroll.getVerticalScrollBar().setValue(0)
            );
        }
        if (tfNpd != null) {
            SwingUtilities.invokeLater(() -> tfNpd.requestFocusInWindow());
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
            // extra bottom padding supaya field tidak ketutup panel tombol
            r.y = Math.max(0, r.y - 8);
            r.height += 30;
            formScroll.getViewport().scrollRectToVisible(r);
        });
    }

    // Menangani proses: make btn.
    private JButton makeBtn(String text, Color bg, ActionListener al) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = !isEnabled() ? new Color(180,180,180) :
                          getModel().isPressed() ? bg.darker() : bg;
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
