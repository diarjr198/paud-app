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
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AbsensiPanel extends JPanel {
    private DataStore ds = DataStore.getInstance();
    private JPanel gridPanel;
    private JLabel lblGroup;
    private JLabel lblHariTanggal;
    private JLabel lblBelum;
    private JCheckBox cbMultiple;
    private JCheckBox cbSelectAll;
    private JSpinner spTanggal;
    private JComboBox<String> cbFilterKelas;
    private JComboBox<String> cbViewMode;
    private JPanel batchPanel;
    private JLabel lblSelected;
    private JButton btnSubmitAbsensi;
    private JButton btnResetAbsensi;
    private final Set<Integer> selectedIds = new LinkedHashSet<>();
    private final Map<Integer, String> statusDraft = new HashMap<>();
    private final Map<Integer, String> statusSaved = new HashMap<>();
    private boolean ignoreFilterEvent = false;
    private boolean ignoreSelectAllEvent = false;
    private LocalDate selectedDate = LocalDate.now();

    public AbsensiPanel() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(buildTopBar(), BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(buildGrid());
        sp.setBorder(null);
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(sp, BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        lblGroup = new JLabel("Absensi Harian");
        lblGroup.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblGroup.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(lblGroup);
        left.add(Box.createVerticalStrut(6));

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setOpaque(false);
        controls.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel rowTanggal = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rowTanggal.setOpaque(false);
        rowTanggal.add(new JLabel("Tanggal:"));
        spTanggal = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spTanggal, "dd/MM/yyyy");
        spTanggal.setEditor(dateEditor);
        JFormattedTextField tfDate = ((JSpinner.DefaultEditor) spTanggal.getEditor()).getTextField();
        tfDate.setEditable(true);
        tfDate.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        if (tfDate.getFormatter() instanceof JFormattedTextField.AbstractFormatter) {
            // no-op, keep formatter but allow valid manual typing + commit
        }
        dateEditor.getFormat().setLenient(false);
        spTanggal.setPreferredSize(new Dimension(120, 30));
        spTanggal.setValue(java.sql.Date.valueOf(selectedDate));
        spTanggal.addChangeListener(e -> onTanggalChanged());
        rowTanggal.add(spTanggal);
        lblHariTanggal = new JLabel();
        lblHariTanggal.setFont(Theme.FONT_SMALL);
        lblHariTanggal.setForeground(Theme.TEXT_GRAY);
        rowTanggal.add(lblHariTanggal);

        JPanel rowTanggalBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rowTanggalBtn.setOpaque(false);
        rowTanggalBtn.add(Box.createHorizontalStrut(68));
        JButton btnTampilkanTanggal = new JButton("Tampilkan");
        btnTampilkanTanggal.setFont(Theme.FONT_BODY);
        btnTampilkanTanggal.setFocusPainted(false);
        btnTampilkanTanggal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTampilkanTanggal.addActionListener(e -> applyTanggalInput());
        rowTanggalBtn.add(btnTampilkanTanggal);

        rowTanggal.add(new JLabel("Kelas:"));
        cbFilterKelas = new JComboBox<>();
        cbFilterKelas.setFont(Theme.FONT_BODY);
        cbFilterKelas.setPreferredSize(new Dimension(80, 30));
        cbFilterKelas.addActionListener(e -> {
            if (ignoreFilterEvent) return;
            selectedIds.clear();
            refreshGrid();
        });
        rowTanggal.add(cbFilterKelas);

        rowTanggal.add(new JLabel("Tampilan:"));
        cbViewMode = new JComboBox<>(new String[]{"Grid", "List"});
        cbViewMode.setFont(Theme.FONT_BODY);
        cbViewMode.setPreferredSize(new Dimension(90, 30));
        cbViewMode.setSelectedItem("List");
        cbViewMode.addActionListener(e -> refreshGrid());
        rowTanggal.add(cbViewMode);

        controls.add(rowTanggal);
        controls.add(Box.createVerticalStrut(4));
        controls.add(rowTanggalBtn);
        left.add(controls);

        // Belum diabsen badge
        lblBelum = new JLabel(" 🔔 Siswa Belum Diabsen: " + ds.getBelumDiabsen() + " ");
        lblBelum.setFont(Theme.FONT_BODY);
        lblBelum.setOpaque(true);
        lblBelum.setBackground(new Color(255, 245, 220));
        lblBelum.setForeground(new Color(160, 80, 0));
        lblBelum.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 180, 80), 1, true),
                new EmptyBorder(4, 10, 4, 10)));

        bar.add(left, BorderLayout.WEST);
        bar.add(lblBelum, BorderLayout.EAST);

        cbMultiple = new JCheckBox("Pilih multiple anak");
        cbMultiple.setFont(Theme.FONT_BODY);
        cbMultiple.setOpaque(false);
        cbMultiple.addActionListener(e -> {
            if (!cbMultiple.isSelected()) {
                selectedIds.clear();
                setSelectAllChecked(false);
            }
            updateBatchPanel();
            refreshGrid();
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        south.setOpaque(false);
        south.add(cbMultiple);
        south.add(Box.createHorizontalStrut(10));
        cbSelectAll = new JCheckBox("Select all");
        cbSelectAll.setFont(Theme.FONT_BODY);
        cbSelectAll.setOpaque(false);
        cbSelectAll.addActionListener(e -> toggleSelectAll());
        south.add(cbSelectAll);

        batchPanel = buildBatchPanel();
        south.add(Box.createHorizontalStrut(12));
        south.add(batchPanel);

        btnSubmitAbsensi = new JButton("Submit Absensi") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = Theme.BLUE_BTN;
                Color c = !isEnabled() ? new Color(160, 160, 160)
                        : getModel().isPressed() ? base.darker() : base;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSubmitAbsensi.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSubmitAbsensi.setForeground(Color.WHITE);
        btnSubmitAbsensi.setContentAreaFilled(false);
        btnSubmitAbsensi.setBorderPainted(false);
        btnSubmitAbsensi.setBorder(new EmptyBorder(8, 14, 8, 14));
        btnSubmitAbsensi.setFocusPainted(false);
        btnSubmitAbsensi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSubmitAbsensi.addActionListener(e -> submitAbsensi());
        btnResetAbsensi = new JButton("Reset/Hapus Absensi") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = new Color(230, 126, 34);
                Color c = getModel().isPressed() ? base.darker() : base;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnResetAbsensi.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnResetAbsensi.setForeground(Color.WHITE);
        btnResetAbsensi.setContentAreaFilled(false);
        btnResetAbsensi.setBorderPainted(false);
        btnResetAbsensi.setBorder(new EmptyBorder(8, 14, 8, 14));
        btnResetAbsensi.setFocusPainted(false);
        btnResetAbsensi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnResetAbsensi.addActionListener(e -> resetAbsensiTanggalKelas());

        south.add(Box.createHorizontalStrut(10));
        south.add(btnSubmitAbsensi);
        south.add(Box.createHorizontalStrut(8));
        south.add(btnResetAbsensi);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(bar, BorderLayout.NORTH);
        wrap.add(south, BorderLayout.SOUTH);
        updateBatchPanel();
        updateSubmitButton();
        return wrap;
    }

    private void toggleSelectAll() {
        if (cbSelectAll == null || ignoreSelectAllEvent) return;
        if (!cbMultiple.isSelected()) {
            setSelectAllChecked(false);
            return;
        }
        Set<Integer> visibleIds = getVisibleSiswaIds();
        if (cbSelectAll.isSelected()) selectedIds.addAll(visibleIds);
        else selectedIds.removeAll(visibleIds);
        updateBatchPanel();
        refreshGrid();
    }

    private JPanel buildBatchPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);

        lblSelected = new JLabel("Terpilih: 0");
        lblSelected.setFont(Theme.FONT_SMALL);
        lblSelected.setForeground(Theme.TEXT_GRAY);
        p.add(lblSelected);
        p.add(batchBtn("Hadir", Theme.GREEN, "Hadir"));
        p.add(batchBtn("Izin", Theme.ORANGE, "Izin"));
        p.add(batchBtn("Sakit", Theme.RED, "Sakit"));
        p.add(batchBtn("Alpa", Theme.GRAY_BTN, "Alpa"));
        return p;
    }

    private JButton batchBtn(String text, Color bg, String status) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = !isEnabled() ? new Color(225, 225, 225)
                        : getModel().isPressed() ? bg.darker() : bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                setForeground(isEnabled() ? Color.WHITE : new Color(105, 105, 105));
                super.paintComponent(g);
            }
        };
        b.setUI(new BasicButtonUI());
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(0, 0, 0, 0));
        b.setFocusPainted(false);
        b.setMargin(new Insets(0, 10, 0, 10));
        b.setPreferredSize(new Dimension(52, 24));
        b.setMinimumSize(new Dimension(52, 24));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> applyBatchStatus(status));
        return b;
    }

    private void applyBatchStatus(String status) {
        if (selectedIds.isEmpty()) return;
        for (Integer id : selectedIds) {
            statusDraft.put(id, status);
        }
        selectedIds.clear();
        updateBatchPanel();
        updateSubmitButton();
        refreshGrid();
    }

    private void updateBatchPanel() {
        if (batchPanel == null || lblSelected == null || cbMultiple == null) return;
        boolean multiOn = cbMultiple.isSelected();
        lblSelected.setText("Terpilih: " + selectedIds.size());
        for (Component c : batchPanel.getComponents()) {
            if (c instanceof JButton) c.setEnabled(multiOn && !selectedIds.isEmpty());
        }
        batchPanel.setVisible(multiOn);
        if (cbSelectAll != null) {
            Set<Integer> visibleIds = getVisibleSiswaIds();
            cbSelectAll.setVisible(multiOn);
            cbSelectAll.setEnabled(multiOn && !visibleIds.isEmpty());
            boolean allChecked = !visibleIds.isEmpty() && selectedIds.containsAll(visibleIds);
            setSelectAllChecked(allChecked);
        }
    }

    private void setSelectAllChecked(boolean checked) {
        if (cbSelectAll == null) return;
        ignoreSelectAllEvent = true;
        cbSelectAll.setSelected(checked);
        ignoreSelectAllEvent = false;
    }

    private Set<Integer> getVisibleSiswaIds() {
        Set<Integer> ids = new LinkedHashSet<>();
        String kelasFilter = getSelectedKelasFilter();
        for (Siswa s : ds.getDaftarSiswa(selectedDate)) {
            if (!kelasFilter.isEmpty() && !kelasFilter.equals(s.getKelompok())) continue;
            ids.add(s.getId());
        }
        return ids;
    }

    private void updateSubmitButton() {
        if (btnSubmitAbsensi == null) return;
        boolean changed = hasDraftChanges();
        btnSubmitAbsensi.setVisible(changed);
        btnSubmitAbsensi.setEnabled(changed);
    }

    private boolean hasDraftChanges() {
        if (statusDraft.size() != statusSaved.size()) return true;
        for (Map.Entry<Integer, String> e : statusDraft.entrySet()) {
            String old = statusSaved.get(e.getKey());
            if (old == null || !old.equals(e.getValue())) return true;
        }
        return false;
    }

    private void submitAbsensi() {
        if (!hasDraftChanges()) return;

        DayOfWeek d = selectedDate.getDayOfWeek();
        if (d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY) {
            String hariId = d.getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
            int liburOk = JOptionPane.showConfirmDialog(this,
                "Tanggal yang dipilih adalah hari libur (" + hariId + ").\nYakin tetap submit absensi?",
                "Konfirmasi Hari Libur", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (liburOk != JOptionPane.YES_OPTION) return;
        }

        String tanggalIndo = selectedDate.format(
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("id", "ID"))
        );
        int ok = JOptionPane.showConfirmDialog(this,
            "Simpan perubahan absensi tanggal " + tanggalIndo + " ke database?",
            "Submit Absensi", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        for (Map.Entry<Integer, String> e : statusDraft.entrySet()) {
            ds.simpanAbsensi(e.getKey(), e.getValue(), selectedDate);
        }
        statusSaved.clear();
        statusSaved.putAll(statusDraft);
        updateSubmitButton();
        refreshGrid();
        JOptionPane.showMessageDialog(this, "Absensi berhasil disimpan.", "Berhasil",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetAbsensiTanggalKelas() {
        String kelas = getSelectedKelasFilter();
        if (kelas == null || kelas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih kelas terlebih dahulu.",
                "Reset Absensi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "Hapus semua absensi untuk tanggal " + selectedDate + " pada Kelas " + kelas + "?",
            "Reset/Hapus Absensi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        int deleted = ds.hapusAbsensi(selectedDate, kelas);
        selectedIds.clear();
        statusDraft.clear();
        statusSaved.clear();
        refreshGrid();
        JOptionPane.showMessageDialog(this,
            "Reset selesai. Data absensi yang dihapus: " + deleted,
            "Reset Absensi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onTanggalChanged() {
        if (spTanggal == null) return;
        java.util.Date d = (java.util.Date) spTanggal.getValue();
        LocalDate newDate = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (newDate.equals(selectedDate)) return;
        selectedDate = newDate;
        selectedIds.clear();
        statusDraft.clear();
        statusSaved.clear();
        refreshGrid();
    }

    private void applyTanggalInput() {
        if (spTanggal == null) return;
        try {
            spTanggal.commitEdit();
        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(this, "Format tanggal tidak valid. Gunakan dd/MM/yyyy.",
                    "Tanggal Tidak Valid", JOptionPane.WARNING_MESSAGE);
            return;
        }
        onTanggalChanged();
    }

    public void resetToToday() {
        selectedIds.clear();
        statusDraft.clear();
        statusSaved.clear();

        LocalDate today = LocalDate.now();
        if (spTanggal == null) {
            selectedDate = today;
            refreshGrid();
            return;
        }

        java.util.Date current = (java.util.Date) spTanggal.getValue();
        LocalDate currentDate = current.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (!currentDate.equals(today)) {
            spTanggal.setValue(java.sql.Date.valueOf(today)); // memicu onTanggalChanged
        } else {
            selectedDate = today;
            refreshGrid();
        }
    }

    private JPanel buildGrid() {
        gridPanel = new JPanel();
        gridPanel.setOpaque(false);
        refreshGrid();
        return gridPanel;
    }

    public void refreshGrid() {
        if (gridPanel == null) return;
        List<Siswa> all = ds.getDaftarSiswa(selectedDate);
        syncDraftStatus(all);
        refreshKelasOptions(all);
        String kelasFilter = getSelectedKelasFilter();

        boolean listMode = cbViewMode != null && "List".equals(cbViewMode.getSelectedItem());
        if (listMode) gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS));
        else gridPanel.setLayout(new GridBagLayout());

        gridPanel.removeAll();
        List<Siswa> filtered = new ArrayList<>();
        for (Siswa s : all) {
            if (!kelasFilter.isEmpty() && !kelasFilter.equals(s.getKelompok())) continue;
            filtered.add(s);
        }
        if (filtered.isEmpty()) {
            JLabel empty = new JLabel("Belum ada data siswa untuk kelas ini.");
            empty.setFont(Theme.FONT_BODY);
            empty.setForeground(Theme.TEXT_GRAY);
            gridPanel.add(empty);
        } else if (listMode) {
            for (Siswa s : filtered) {
                JPanel card = buildSiswaCard(s, true);
                gridPanel.add(card);
                gridPanel.add(Box.createVerticalStrut(8));
            }
        } else {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            for (int i = 0; i < filtered.size(); i++) {
                int col = i % 3;
                int row = i / 3;
                gbc.gridx = col;
                gbc.gridy = row;
                gbc.insets = new Insets(0, 0, 12, col == 2 ? 0 : 12);
                JPanel card = buildSiswaCard(filtered.get(i), false);
                gridPanel.add(card, gbc);
            }

            // Filler agar semua card tetap menempel ke atas (tidak memanjang saat item sedikit)
            gbc.gridx = 0;
            gbc.gridy = (filtered.size() + 2) / 3;
            gbc.gridwidth = 3;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(0, 0, 0, 0);
            gridPanel.add(Box.createGlue(), gbc);
        }
        gridPanel.revalidate();
        gridPanel.repaint();
        updateHeaderByFilter(kelasFilter);
        updateHariTanggalLabel();
        updateBatchPanel();
        updateSubmitButton();
        updateBelum();
    }

    private void updateHariTanggalLabel() {
        if (lblHariTanggal == null) return;
        String hari = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
        lblHariTanggal.setText("(" + hari + ")");
    }

    private void syncDraftStatus(List<Siswa> all) {
        Set<Integer> activeIds = new LinkedHashSet<>();
        for (Siswa s : all) {
            int id = s.getId();
            activeIds.add(id);
            String normalized = normalizeStatus(s.getStatusAbsensi());
            if (!statusSaved.containsKey(id)) statusSaved.put(id, normalized);
            if (!statusDraft.containsKey(id)) statusDraft.put(id, normalized);
        }
        statusSaved.keySet().retainAll(activeIds);
        statusDraft.keySet().retainAll(activeIds);
        selectedIds.retainAll(activeIds);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty() || "-".equals(status)) return "Alpa";
        switch (status) {
            case "H": return "Hadir";
            case "I": return "Izin";
            case "S": return "Sakit";
            case "A": return "Alpa";
            default: return status;
        }
    }

    private void refreshKelasOptions(List<Siswa> all) {
        if (cbFilterKelas == null) return;
        String current = (String) cbFilterKelas.getSelectedItem();
        Set<String> kelas = new LinkedHashSet<>();
        for (Siswa s : all) kelas.add(s.getKelompok());

        ignoreFilterEvent = true;
        cbFilterKelas.removeAllItems();
        for (String k : kelas) cbFilterKelas.addItem(k);
        if (cbFilterKelas.getItemCount() > 0) {
            if (current != null && kelas.contains(current)) cbFilterKelas.setSelectedItem(current);
            else cbFilterKelas.setSelectedIndex(0); // default kelas paling awal/paling atas
        }
        ignoreFilterEvent = false;
    }

    private String getSelectedKelasFilter() {
        if (cbFilterKelas == null || cbFilterKelas.getSelectedItem() == null) return "";
        return cbFilterKelas.getSelectedItem().toString();
    }

    private void updateHeaderByFilter(String kelasFilter) {
        if (lblGroup == null) return;
        if (kelasFilter == null || kelasFilter.isEmpty()) lblGroup.setText("Absensi Harian");
        else lblGroup.setText("Absensi Harian - Kelas " + kelasFilter);
    }

    private void updateBelum() {
        if (lblBelum != null)
            lblBelum.setText(" 🔔 Siswa Belum Diabsen: " + ds.getBelumDiabsen(selectedDate) + " ");
    }

    private JPanel buildSiswaCard(Siswa s, boolean listMode) {
        RoundedPanel card = Theme.makeCard(14);
        card.setLayout(new BorderLayout(8, 0));
        boolean selected = selectedIds.contains(s.getId());
        card.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(selected ? Theme.BLUE_BTN : new Color(220, 220, 220), selected ? 2 : 1, true),
            new EmptyBorder(listMode ? 8 : 10, 10, listMode ? 8 : 10, 10)
        ));
        if (listMode) {
            card.setAlignmentX(LEFT_ALIGNMENT);
            Dimension fixedH = new Dimension(320, 54);
            card.setMinimumSize(new Dimension(0, 54));
            card.setPreferredSize(fixedH);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        } else {
            Dimension fixedGridH = new Dimension(0, 138);
            card.setMinimumSize(fixedGridH);
            card.setPreferredSize(fixedGridH);
        }

        // Avatar
        JLabel avatar = makeAvatar(s);
        JPanel avatarWrap = new JPanel();
        avatarWrap.setOpaque(false);
        avatarWrap.setLayout(new BorderLayout());
        avatarWrap.add(avatar, BorderLayout.NORTH);
        card.add(avatarWrap, BorderLayout.WEST);

        // Info (NPD + Nama)
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel npd = new JLabel("NPD: " + (s.getNpd() == null || s.getNpd().isEmpty() ? "-" : s.getNpd()));
        npd.setFont(Theme.FONT_SMALL);
        npd.setForeground(Theme.TEXT_GRAY);
        npd.setAlignmentX(LEFT_ALIGNMENT);
        info.add(npd);
        info.add(Box.createVerticalStrut(2));

        JLabel name = new JLabel(s.getNama());
        name.setFont(Theme.FONT_HEADER);
        name.setAlignmentX(LEFT_ALIGNMENT);
        info.add(name);
        info.add(Box.createVerticalStrut(listMode ? 2 : 6));

        final String currentStatus = statusDraft.getOrDefault(s.getId(), "Alpa");
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(LEFT_ALIGNMENT);
        row1.add(absBtn("Hadir", Theme.GREEN,   s.getId(), "Hadir", currentStatus));
        row1.add(absBtn("Izin",  Theme.ORANGE,  s.getId(), "Izin", currentStatus));

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(LEFT_ALIGNMENT);
        row2.add(absBtn("Sakit", Theme.RED,     s.getId(), "Sakit", currentStatus));
        row2.add(absBtn("Alpa",  Theme.GRAY_BTN,s.getId(), "Alpa", currentStatus));

        if (listMode) {
            // List mode: tombol di kanan, sejajar tengah area NPD+Nama
            JPanel rowList = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            rowList.setOpaque(false);
            rowList.setAlignmentX(RIGHT_ALIGNMENT);
            rowList.add(absBtn("Hadir", Theme.GREEN,   s.getId(), "Hadir", currentStatus));
            rowList.add(absBtn("Izin",  Theme.ORANGE,  s.getId(), "Izin", currentStatus));
            rowList.add(absBtn("Sakit", Theme.RED,     s.getId(), "Sakit", currentStatus));
            rowList.add(absBtn("Alpa",  Theme.GRAY_BTN,s.getId(), "Alpa", currentStatus));
            JPanel actionWrap = new JPanel();
            actionWrap.setOpaque(false);
            actionWrap.setLayout(new BoxLayout(actionWrap, BoxLayout.Y_AXIS));
            actionWrap.add(Box.createVerticalGlue());
            actionWrap.add(rowList);
            actionWrap.add(Box.createVerticalGlue());

            // Circle avatar 1:1, proporsional mengikuti tinggi blok NPD+Nama
            int infoHeight = npd.getPreferredSize().height + 2 + name.getPreferredSize().height;
            int side = Math.max(30, Math.min(46, infoHeight + 2));
            Dimension avatarSize = new Dimension(side, side);
            avatar.setPreferredSize(avatarSize);
            avatar.setMinimumSize(avatarSize);
            avatar.setMaximumSize(avatarSize);

            card.add(info, BorderLayout.CENTER);
            card.add(actionWrap, BorderLayout.EAST);
        } else {
            info.add(row1);
            info.add(row2);
            card.add(info, BorderLayout.CENTER);
        }

        if (cbMultiple != null && cbMultiple.isSelected()) {
            attachSelectHandler(card, s.getId());
        }

        // Highlight card if status set
        highlightCard(card, currentStatus);
        return card;
    }

    private void attachSelectHandler(Component comp, int siswaId) {
        if (comp instanceof JButton) return;
        comp.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (!cbMultiple.isSelected()) return;
                if (selectedIds.contains(siswaId)) selectedIds.remove(siswaId);
                else selectedIds.add(siswaId);
                updateBatchPanel();
                refreshGrid();
            }
        });
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                attachSelectHandler(child, siswaId);
            }
        }
    }

    private void highlightCard(RoundedPanel card, String status) {
        Color bg = Theme.CARD_BG;
        switch (status) {
            case "H":
            case "Hadir": bg = new Color(232, 255, 232); break;
            case "I":
            case "Izin":  bg = new Color(255, 243, 220); break;
            case "S":
            case "Sakit": bg = new Color(255, 230, 230); break;
            case "A":
            case "Alpa":  bg = new Color(240, 240, 240); break;
        }
        card.bg = bg;
        card.repaint();
    }

    private JButton absBtn(String text, Color bg, int siswaId, String status, String currentStatus) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = currentStatus.equals(status) ? bg.darker() : bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setUI(new BasicButtonUI());
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(0, 0, 0, 0));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setMargin(new Insets(0, 10, 0, 10));
        int btnWidth;
        switch (text) {
            case "Hadir":
            case "Sakit":
                btnWidth = 76;
                break;
            default: // Izin, Alpa
                btnWidth = 70;
                break;
        }
        Dimension btnSize = new Dimension(btnWidth, 28);
        b.setPreferredSize(btnSize);
        b.setMinimumSize(btnSize);
        b.addActionListener(e -> {
            statusDraft.put(siswaId, status);
            updateSubmitButton();
            refreshGrid();
        });
        return b;
    }

    private JLabel makeAvatar(Siswa s) {
        // Simple colored circle with initial
        JLabel l = new JLabel(s.getNama().substring(0, 1), SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color[] palette = {new Color(100,181,246), new Color(165,214,167),
                        new Color(255,204,128), new Color(206,147,216), new Color(240,98,146)};
                int idx = Math.abs(s.getNama().hashCode()) % palette.length;
                g2.setColor(palette[idx]);
                g2.fillOval(2, 2, getWidth()-4, getHeight()-4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("SansSerif", Font.BOLD, 18));
        l.setForeground(Color.WHITE);
        l.setPreferredSize(new Dimension(54, 54));
        l.setMinimumSize(new Dimension(54, 54));
        l.setMaximumSize(new Dimension(54, 54));
        return l;
    }
}
