/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.paud;

/**
 *
 * @author diarjr198
 */
import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DataStore - Jembatan antara UI dan DatabaseHelper.
 * Semua operasi data sekarang tersimpan ke SQLite.
 */
public class DataStore {
    private static DataStore instance;
    private DatabaseHelper db;
    private Guru guruLogin;
    private String activeAngkatan;

    private DataStore() {
        db = DatabaseHelper.getInstance();
    }

    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    public DatabaseHelper getDb() { return db; }
    public String getActiveAngkatan() { return activeAngkatan; }
    public boolean isGuruRoleLogin() {
        return guruLogin != null && "Guru".equalsIgnoreCase(guruLogin.getRole());
    }

    public Set<String> getKelasAmpuLoginSet() {
        Set<String> out = new LinkedHashSet<>();
        if (!isGuruRoleLogin()) return out;
        String csv = guruLogin.getKelasAmpu();
        if (csv == null || csv.trim().isEmpty()) return out;
        for (String p : csv.split(",")) {
            String k = p == null ? "" : p.trim();
            if (!k.isEmpty()) out.add(k);
        }
        return out;
    }

    public List<String> getDaftarAngkatan() { return db.getDaftarAngkatan(); }

    public void setActiveAngkatan(String angkatan) {
        this.activeAngkatan = (angkatan == null || angkatan.trim().isEmpty()) ? null : angkatan.trim();
    }

    public String getNamaSekolah() {
        return db.getSetting("nama_sekolah", "TUNAS HARAPAN");
    }

    public boolean setNamaSekolah(String nama) {
        String v = (nama == null) ? "" : nama.trim();
        if (v.isEmpty()) return false;
        return db.setSetting("nama_sekolah", v);
    }

    // ===== KELAS =====
    public List<Kelas> getDaftarKelas() { return db.getAllKelas(); }
    public Kelas getKelasById(int id) { return db.getKelasById(id); }

    public boolean tambahKelas(String nama) {
        if (nama == null || nama.trim().isEmpty()) return false;
        if (db.kelasExists(nama.trim(), null)) return false;
        return db.insertKelas(nama.trim()) > 0;
    }

    public boolean updateKelas(int id, String nama) {
        if (nama == null || nama.trim().isEmpty()) return false;
        if (db.kelasExists(nama.trim(), id)) return false;
        return db.updateKelas(id, nama.trim());
    }

    public boolean hapusKelas(int id) {
        Kelas k = db.getKelasById(id);
        if (k == null) return false;
        String nama = k.getNama();

        boolean dipakaiSiswa = getDaftarSiswa().stream().anyMatch(s -> nama.equalsIgnoreCase(s.getKelompok()));
        boolean dipakaiGuru = getDaftarGuru().stream().anyMatch(g -> containsKelas(g.getKelasAmpu(), nama));
        if (dipakaiSiswa || dipakaiGuru) return false;
        return db.deleteKelas(id);
    }

    public boolean kelasSedangDipakai(int id) {
        Kelas k = db.getKelasById(id);
        if (k == null) return false;
        String nama = k.getNama();
        boolean dipakaiSiswa = getDaftarSiswa().stream().anyMatch(s -> nama.equalsIgnoreCase(s.getKelompok()));
        boolean dipakaiGuru = getDaftarGuru().stream().anyMatch(g -> containsKelas(g.getKelasAmpu(), nama));
        return dipakaiSiswa || dipakaiGuru;
    }

    private boolean containsKelas(String csv, String kelas) {
        if (csv == null || csv.trim().isEmpty()) return false;
        String[] parts = csv.split(",");
        for (String p : parts) {
            if (kelas.equalsIgnoreCase(p.trim())) return true;
        }
        return false;
    }

    // ===== SISWA =====

    public List<Siswa> getDaftarSiswa() {
        return filterSiswaByKelasAmpu(db.getAllSiswa(LocalDate.now(), activeAngkatan));
    }

    public List<Siswa> getDaftarSiswa(LocalDate tanggal) {
        return filterSiswaByKelasAmpu(db.getAllSiswa(tanggal, activeAngkatan));
    }

    /**
     * Tambah siswa baru. Return false jika duplikat.
     */
    public boolean tambahSiswa(Siswa s) {
        if (db.isDuplicateSiswa(s.getNama(), s.getNamaWali(), -1)) return false;
        int newId = db.insertSiswa(s.getNama(), s.getKelompok(),
                                   s.getNamaWali(), s.getTanggalLahir(),
                                   s.getNpd(), s.getNisn(), s.getTempatLahir(),
                                   s.getAlamat(), s.getJenisKelamin(), s.getAgama(), s.getKontakWali(), s.getAngkatan());
        if (newId > 0) { s.setId(newId); return true; }
        return false;
    }

    public boolean updateSiswa(int id, String nama, String kelompok,
                                String namaOrangTua, String tanggalLahir) {
        return db.updateSiswa(id, nama, kelompok, namaOrangTua, tanggalLahir);
    }

    public boolean updateSiswa(int id, String nama, String kelas, String namaWali, String tanggalLahir,
                               String npd, String nisn, String tempatLahir, String alamat,
                               String jenisKelamin, String agama, String kontakWali, String angkatan) {
        return db.updateSiswa(id, nama, kelas, namaWali, tanggalLahir,
                              npd, nisn, tempatLahir, alamat, jenisKelamin, agama, kontakWali, angkatan);
    }

    public boolean hapusSiswa(int id) {
        return db.deleteSiswa(id);
    }

    public Siswa getSiswaById(int id) {
        return db.getSiswaById(id);
    }

    public int getTotalSiswa() {
        return getDaftarSiswa().size();
    }

    public long getCountByStatus(String s) {
        long count = 0;
        for (Siswa siswa : getDaftarSiswa(LocalDate.now())) {
            String st = normalizeStatus(siswa.getStatusAbsensi());
            if (s.equalsIgnoreCase(st)) count++;
        }
        return count;
    }

    public long getBelumDiabsen() {
        return getBelumDiabsen(LocalDate.now());
    }

    public long getBelumDiabsen(LocalDate tanggal) {
        long count = 0;
        for (Siswa siswa : getDaftarSiswa(tanggal)) {
            String st = normalizeStatus(siswa.getStatusAbsensi());
            if ("Alpa".equals(st)) count++;
        }
        return count;
    }

    // ===== ABSENSI =====

    public boolean simpanAbsensi(int siswaId, String status) {
        return db.simpanAbsensi(siswaId, status);
    }

    public boolean simpanAbsensi(int siswaId, String status, LocalDate tanggal) {
        return db.simpanAbsensi(siswaId, status, tanggal);
    }

    public int hapusAbsensi(LocalDate tanggal, String kelas) {
        return db.hapusAbsensi(tanggal, kelas);
    }

    public List<Object[]> getLaporanBulanFiltered(int tahun, int bulan, Integer hari, String status) {
        return filterLaporanByKelasAmpu(
                db.getLaporanBulanFiltered(tahun, bulan, hari, status, null, activeAngkatan));
    }

    public List<Object[]> getLaporanBulanFiltered(int tahun, int bulan, Integer hari, String status, String kelas) {
        return filterLaporanByKelasAmpu(
                db.getLaporanBulanFiltered(tahun, bulan, hari, status, kelas, activeAngkatan));
    }

    public List<Object[]> getLaporanHarian(int tahun, int bulan, int hari, String status) {
        return db.getLaporanBulanFiltered(tahun, bulan, hari, status, null, activeAngkatan);
    }

    public List<Object[]> getLaporanHarian(LocalDate tanggal, String status) {
        return filterLaporanByKelasAmpu(
                db.getLaporanBulanFiltered(tanggal.getYear(), tanggal.getMonthValue(), tanggal.getDayOfMonth(), status, null, activeAngkatan));
    }

    public List<Object[]> getDetailAbsensiSiswaBulanan(int siswaId, int tahun, int bulan) {
        return db.getDetailAbsensiSiswaBulanan(siswaId, tahun, bulan);
    }

    // ===== GURU / AUTH =====

    public List<Guru> getDaftarGuru() { return db.getAllGuru(); }
    public Guru getGuruByUsername(String username) { return db.getGuruByUsername(username); }

    public boolean tambahGuru(Guru g) {
        if (db.usernameExists(g.getUsername())) return false;
        return db.insertGuru(g.getNip(), g.getNamaLengkap(), g.getTempatLahir(), g.getTanggalLahir(), g.getTempatTanggalLahir(),
                g.getJenisKelamin(), g.getAlamat(), g.getAgama(), g.getRole(), g.getKelasAmpu(),
                g.getUsername(), g.getPassword());
    }

    public boolean updateGuru(String oldUsername, Guru g) {
        if (!oldUsername.equals(g.getUsername()) && db.usernameExists(g.getUsername())) return false;
        return db.updateGuru(oldUsername, g.getNip(), g.getNamaLengkap(), g.getTempatLahir(), g.getTanggalLahir(), g.getTempatTanggalLahir(),
                g.getJenisKelamin(), g.getAlamat(), g.getAgama(), g.getRole(), g.getKelasAmpu(),
                g.getUsername(), g.getPassword());
    }

    public boolean hapusGuru(String username) {
        return db.deleteGuru(username);
    }

    public Guru login(String username, String password) {
        Guru g = db.loginGuru(username, password);
        if (g != null) guruLogin = g;
        return g;
    }

    public boolean registerGuru(String username, String password,
                                 String namaLengkap, String kelompok) {
        if (db.usernameExists(username)) return false;
        return db.insertGuru(username, password, namaLengkap, kelompok);
    }

    public Guru  getGuruLogin()  { return guruLogin; }
    public void  logout()        { guruLogin = null; }
    public boolean isLoggedIn()  { return guruLogin != null; }

    public void closeDB() { db.close(); }

    private List<Siswa> filterSiswaByKelasAmpu(List<Siswa> source) {
        if (!isGuruRoleLogin()) return source;
        Set<String> allowed = getKelasAmpuLoginSet();
        if (allowed.isEmpty()) return new ArrayList<>();
        List<Siswa> out = new ArrayList<>();
        for (Siswa s : source) {
            if (allowed.contains(s.getKelompok())) out.add(s);
        }
        return out;
    }

    private List<Object[]> filterLaporanByKelasAmpu(List<Object[]> source) {
        if (!isGuruRoleLogin()) return source;
        Set<String> allowed = getKelasAmpuLoginSet();
        if (allowed.isEmpty()) return new ArrayList<>();
        List<Object[]> out = new ArrayList<>();
        for (Object[] row : source) {
            if (row == null || row.length < 3) continue;
            String kelas = row[2] == null ? "" : row[2].toString();
            if (allowed.contains(kelas)) out.add(row);
        }
        return out;
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
}
