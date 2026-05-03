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

    // Menjalankan inisialisasi objek DataStore.
    private DataStore() {
        db = DatabaseHelper.getInstance();
    }

    // Menangani proses: get instance.
    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    // Mengambil nilai properti yang dibutuhkan.
    public DatabaseHelper getDb() { return db; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getActiveAngkatan() { return activeAngkatan; }
    // Menangani proses: is guru role login.
    public boolean isGuruRoleLogin() {
        return guruLogin != null && "Guru".equalsIgnoreCase(guruLogin.getRole());
    }

    // Menangani proses: get kelas ampu login set.
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

    // Mengambil nilai properti yang dibutuhkan.
    public List<String> getDaftarAngkatan() { return db.getDaftarAngkatan(); }

    // Menangani proses: set active angkatan.
    public void setActiveAngkatan(String angkatan) {
        this.activeAngkatan = (angkatan == null || angkatan.trim().isEmpty()) ? null : angkatan.trim();
    }

    // Menangani proses: get nama sekolah.
    public String getNamaSekolah() {
        return db.getSetting("nama_sekolah", "TUNAS HARAPAN");
    }

    // Menangani proses: set nama sekolah.
    public boolean setNamaSekolah(String nama) {
        String v = (nama == null) ? "" : nama.trim();
        if (v.isEmpty()) return false;
        return db.setSetting("nama_sekolah", v);
    }

    // Menangani proses: get jam masuk.
    public String getJamMasuk() {
        return db.getSetting("jam_masuk", "07:30 WIB");
    }

    // Menangani proses: get jam pulang.
    public String getJamPulang() {
        return db.getSetting("jam_pulang", "10:30 WIB");
    }

    // Menangani proses: set jam masuk.
    public boolean setJamMasuk(String jam) {
        String v = (jam == null) ? "" : jam.trim();
        if (v.isEmpty()) return false;
        return db.setSetting("jam_masuk", v);
    }

    // Menangani proses: set jam pulang.
    public boolean setJamPulang(String jam) {
        String v = (jam == null) ? "" : jam.trim();
        if (v.isEmpty()) return false;
        return db.setSetting("jam_pulang", v);
    }

    // ===== KELAS =====
    public List<Kelas> getDaftarKelas() { return db.getAllKelas(); }
    // Mengambil nilai properti yang dibutuhkan.
    public Kelas getKelasById(int id) { return db.getKelasById(id); }

    // Menangani proses: tambah kelas.
    public boolean tambahKelas(String nama) {
        if (nama == null || nama.trim().isEmpty()) return false;
        if (db.kelasExists(nama.trim(), null)) return false;
        return db.insertKelas(nama.trim()) > 0;
    }

    // Menangani proses: update kelas.
    public boolean updateKelas(int id, String nama) {
        if (nama == null || nama.trim().isEmpty()) return false;
        if (db.kelasExists(nama.trim(), id)) return false;
        return db.updateKelas(id, nama.trim());
    }

    // Menangani proses: hapus kelas.
    public boolean hapusKelas(int id) {
        Kelas k = db.getKelasById(id);
        if (k == null) return false;
        String nama = k.getNama();

        boolean dipakaiSiswa = getDaftarSiswa().stream().anyMatch(s -> nama.equalsIgnoreCase(s.getKelompok()));
        boolean dipakaiGuru = getDaftarGuru().stream().anyMatch(g -> containsKelas(g.getKelasAmpu(), nama));
        if (dipakaiSiswa || dipakaiGuru) return false;
        return db.deleteKelas(id);
    }

    // Menangani proses: kelas sedang dipakai.
    public boolean kelasSedangDipakai(int id) {
        Kelas k = db.getKelasById(id);
        if (k == null) return false;
        String nama = k.getNama();
        boolean dipakaiSiswa = getDaftarSiswa().stream().anyMatch(s -> nama.equalsIgnoreCase(s.getKelompok()));
        boolean dipakaiGuru = getDaftarGuru().stream().anyMatch(g -> containsKelas(g.getKelasAmpu(), nama));
        return dipakaiSiswa || dipakaiGuru;
    }

    // Menangani proses: contains kelas.
    private boolean containsKelas(String csv, String kelas) {
        if (csv == null || csv.trim().isEmpty()) return false;
        String[] parts = csv.split(",");
        for (String p : parts) {
            if (kelas.equalsIgnoreCase(p.trim())) return true;
        }
        return false;
    }

    // ===== SISWA =====

    // Menangani proses: get daftar siswa.
    public List<Siswa> getDaftarSiswa() {
        return filterSiswaByKelasAmpu(db.getAllSiswa(LocalDate.now(), activeAngkatan));
    }

    // Menangani proses: get daftar siswa.
    public List<Siswa> getDaftarSiswa(LocalDate tanggal) {
        return filterSiswaByKelasAmpu(db.getAllSiswa(tanggal, activeAngkatan));
    }

    /**
     * Tambah siswa baru. Return false jika duplikat.
     */
    // Menangani proses: tambah siswa.
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

    // Menangani proses: hapus siswa.
    public boolean hapusSiswa(int id) {
        return db.deleteSiswa(id);
    }

    // Menangani proses: get siswa by id.
    public Siswa getSiswaById(int id) {
        return db.getSiswaById(id);
    }

    // Menangani proses: get total siswa.
    public int getTotalSiswa() {
        return getDaftarSiswa().size();
    }

    // Menangani proses: get count by status.
    public long getCountByStatus(String s) {
        long count = 0;
        for (Siswa siswa : getDaftarSiswa(LocalDate.now())) {
            String st = normalizeStatus(siswa.getStatusAbsensi());
            if (s.equalsIgnoreCase(st)) count++;
        }
        return count;
    }

    // Menangani proses: get belum diabsen.
    public long getBelumDiabsen() {
        return getBelumDiabsen(LocalDate.now());
    }

    // Menangani proses: get belum diabsen.
    public long getBelumDiabsen(LocalDate tanggal) {
        long count = 0;
        for (Siswa siswa : getDaftarSiswa(tanggal)) {
            String st = normalizeStatus(siswa.getStatusAbsensi());
            if ("Alpa".equals(st)) count++;
        }
        return count;
    }

    // ===== ABSENSI =====

    // Menangani proses: simpan absensi.
    public boolean simpanAbsensi(int siswaId, String status) {
        return db.simpanAbsensi(siswaId, status);
    }

    // Menangani proses: simpan absensi.
    public boolean simpanAbsensi(int siswaId, String status, LocalDate tanggal) {
        return db.simpanAbsensi(siswaId, status, tanggal);
    }

    // Menangani proses: hapus absensi.
    public int hapusAbsensi(LocalDate tanggal, String kelas) {
        return db.hapusAbsensi(tanggal, kelas);
    }

    // Menangani proses: get laporan bulan filtered.
    public List<Object[]> getLaporanBulanFiltered(int tahun, int bulan, Integer hari, String status) {
        return filterLaporanByKelasAmpu(
                db.getLaporanBulanFiltered(tahun, bulan, hari, status, null, activeAngkatan));
    }

    // Menangani proses: get laporan bulan filtered.
    public List<Object[]> getLaporanBulanFiltered(int tahun, int bulan, Integer hari, String status, String kelas) {
        return filterLaporanByKelasAmpu(
                db.getLaporanBulanFiltered(tahun, bulan, hari, status, kelas, activeAngkatan));
    }

    // Menangani proses: get laporan harian.
    public List<Object[]> getLaporanHarian(int tahun, int bulan, int hari, String status) {
        return db.getLaporanBulanFiltered(tahun, bulan, hari, status, null, activeAngkatan);
    }

    // Menangani proses: get laporan harian.
    public List<Object[]> getLaporanHarian(LocalDate tanggal, String status) {
        return filterLaporanByKelasAmpu(
                db.getLaporanBulanFiltered(tanggal.getYear(), tanggal.getMonthValue(), tanggal.getDayOfMonth(), status, null, activeAngkatan));
    }

    // Menangani proses: get detail absensi siswa bulanan.
    public List<Object[]> getDetailAbsensiSiswaBulanan(int siswaId, int tahun, int bulan) {
        return db.getDetailAbsensiSiswaBulanan(siswaId, tahun, bulan);
    }

    // ===== GURU / AUTH =====

    // Mengambil nilai properti yang dibutuhkan.
    public List<Guru> getDaftarGuru() { return db.getAllGuru(); }
    // Mengambil nilai properti yang dibutuhkan.
    public Guru getGuruByUsername(String username) { return db.getGuruByUsername(username); }

    // Menangani proses: tambah guru.
    public boolean tambahGuru(Guru g) {
        if (db.usernameExists(g.getUsername())) return false;
        return db.insertGuru(g.getNip(), g.getNamaLengkap(), g.getTempatLahir(), g.getTanggalLahir(), g.getTempatTanggalLahir(),
                g.getJenisKelamin(), g.getAlamat(), g.getAgama(), g.getRole(), g.getKelasAmpu(),
                g.getUsername(), g.getPassword());
    }

    // Menangani proses: update guru.
    public boolean updateGuru(String oldUsername, Guru g) {
        if (!oldUsername.equals(g.getUsername()) && db.usernameExists(g.getUsername())) return false;
        return db.updateGuru(oldUsername, g.getNip(), g.getNamaLengkap(), g.getTempatLahir(), g.getTanggalLahir(), g.getTempatTanggalLahir(),
                g.getJenisKelamin(), g.getAlamat(), g.getAgama(), g.getRole(), g.getKelasAmpu(),
                g.getUsername(), g.getPassword());
    }

    // Menangani proses: hapus guru.
    public boolean hapusGuru(String username) {
        return db.deleteGuru(username);
    }

    // Menangani proses: login.
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

    // Mengambil nilai properti yang dibutuhkan.
    public Guru  getGuruLogin()  { return guruLogin; }
    // Menangani proses: logout.
    public void  logout()        { guruLogin = null; }
    // Mengecek kondisi tertentu dan mengembalikan hasilnya.
    public boolean isLoggedIn()  { return guruLogin != null; }

    // Menangani proses: close db.
    public void closeDB() { db.close(); }

    // Menangani proses: filter siswa by kelas ampu.
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

    // Menangani proses: filter laporan by kelas ampu.
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

    // Menangani proses: normalize status.
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
