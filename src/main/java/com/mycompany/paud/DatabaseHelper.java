/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.paud;

/**
 *
 * @author diarjr198
 */
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper - Mengelola koneksi SQLite dan semua operasi database.
 *
 * Membutuhkan: sqlite-jdbc-*.jar di classpath.
 * Download: https://github.com/xerial/sqlite-jdbc/releases
 * Letakkan .jar di folder lib/ dalam project NetBeans,
 * lalu klik kanan project > Properties > Libraries > Add JAR/Folder.
 *
 * File database akan dibuat otomatis di: paud_absensi.db
 */
public class DatabaseHelper {

    private static DatabaseHelper instance;
    private Connection connection;

    // Nama file database (akan dibuat di folder project)
    private static final String DB_FILE = "paud_absensi.db";
    private static final String DB_URL  = "jdbc:sqlite:" + DB_FILE;

    private DatabaseHelper() {
        connect();
        createTables();
        insertDefaultData();
    }

    public static DatabaseHelper getInstance() {
        if (instance == null) instance = new DatabaseHelper();
        return instance;
    }

    // ===================== KONEKSI =====================

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            // Aktifkan foreign keys
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            System.out.println("[DB] Terhubung ke: " + DB_FILE);
        } catch (ClassNotFoundException e) {
            showDBError("Driver SQLite tidak ditemukan!\n\n" +
                "Silakan download sqlite-jdbc dari:\n" +
                "https://github.com/xerial/sqlite-jdbc/releases\n\n" +
                "Lalu tambahkan .jar ke Libraries project NetBeans.");
        } catch (SQLException e) {
            showDBError("Gagal terhubung ke database:\n" + e.getMessage());
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Koneksi ditutup.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===================== CREATE TABLES =====================

    private void createTables() {
        if (!isConnected()) return;
        String[] sqls = {
            // Tabel kelas
            "CREATE TABLE IF NOT EXISTS kelas (" +
            "  id             INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  nama           TEXT NOT NULL UNIQUE" +
            ")",

            // Tabel guru
            "CREATE TABLE IF NOT EXISTS guru (" +
            "  username       TEXT PRIMARY KEY," +
            "  password       TEXT NOT NULL," +
            "  nama_lengkap   TEXT NOT NULL," +
            "  nip            TEXT DEFAULT ''," +
            "  tempat_lahir   TEXT DEFAULT ''," +
            "  tanggal_lahir  TEXT DEFAULT ''," +
            "  ttl            TEXT DEFAULT ''," +
            "  jenis_kelamin  TEXT DEFAULT ''," +
            "  alamat         TEXT DEFAULT ''," +
            "  agama          TEXT DEFAULT ''," +
            "  role           TEXT DEFAULT 'Guru'," +
            "  kelompok_ampu  TEXT NOT NULL DEFAULT 'Semua'" +
            ")",

            // Tabel siswa
            "CREATE TABLE IF NOT EXISTS siswa (" +
            "  id             INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  nama           TEXT NOT NULL," +
            "  kelompok       TEXT NOT NULL," +
            "  nama_orang_tua TEXT NOT NULL," +
            "  tanggal_lahir  TEXT DEFAULT ''," +
            "  npd            TEXT DEFAULT ''," +
            "  nisn           TEXT DEFAULT ''," +
            "  tempat_lahir   TEXT DEFAULT ''," +
            "  alamat         TEXT DEFAULT ''," +
            "  jenis_kelamin  TEXT DEFAULT ''," +
            "  agama          TEXT DEFAULT ''," +
            "  kontak_wali    TEXT DEFAULT ''," +
            "  angkatan       TEXT DEFAULT ''" +
            ")",

            // Tabel absensi harian
            "CREATE TABLE IF NOT EXISTS absensi (" +
            "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  siswa_id    INTEGER NOT NULL," +
            "  tanggal     TEXT NOT NULL," +
            "  status      TEXT NOT NULL DEFAULT '-'," +
            "  FOREIGN KEY (siswa_id) REFERENCES siswa(id) ON DELETE CASCADE," +
            "  UNIQUE(siswa_id, tanggal)" +
            ")",

            // Tabel pengaturan aplikasi
            "CREATE TABLE IF NOT EXISTS app_settings (" +
            "  kunci       TEXT PRIMARY KEY," +
            "  nilai       TEXT NOT NULL DEFAULT ''" +
            ")"
        };

        try (Statement st = connection.createStatement()) {
            for (String sql : sqls) st.execute(sql);
            ensureGuruColumns(st);
            ensureSiswaColumns(st);
            System.out.println("[DB] Tabel berhasil dibuat/diverifikasi.");
        } catch (SQLException e) {
            System.err.println("[DB] Error createTables: " + e.getMessage());
        }
    }

    private void ensureGuruColumns(Statement st) throws SQLException {
        addColumnIfMissing(st, "guru", "nip", "TEXT DEFAULT ''");
        addColumnIfMissing(st, "guru", "tempat_lahir", "TEXT DEFAULT ''");
        addColumnIfMissing(st, "guru", "tanggal_lahir", "TEXT DEFAULT ''");
        addColumnIfMissing(st, "guru", "ttl", "TEXT DEFAULT ''");
        addColumnIfMissing(st, "guru", "jenis_kelamin", "TEXT DEFAULT ''");
        addColumnIfMissing(st, "guru", "alamat", "TEXT DEFAULT ''");
        addColumnIfMissing(st, "guru", "agama", "TEXT DEFAULT ''");
        addColumnIfMissing(st, "guru", "role", "TEXT DEFAULT 'Guru'");
    }

    private void ensureSiswaColumns(Statement st) throws SQLException {
        addColumnIfMissing(st, "siswa", "npd", "TEXT DEFAULT ''");
        addColumnIfMissing(st, "siswa", "nisn", "TEXT DEFAULT ''");
        addColumnIfMissing(st, "siswa", "tempat_lahir", "TEXT DEFAULT ''");
        addColumnIfMissing(st, "siswa", "alamat", "TEXT DEFAULT ''");
        addColumnIfMissing(st, "siswa", "jenis_kelamin", "TEXT DEFAULT ''");
        addColumnIfMissing(st, "siswa", "agama", "TEXT DEFAULT ''");
        addColumnIfMissing(st, "siswa", "kontak_wali", "TEXT DEFAULT ''");
        addColumnIfMissing(st, "siswa", "angkatan", "TEXT DEFAULT ''");
    }

    private void addColumnIfMissing(Statement st, String table, String column, String def) throws SQLException {
        if (!columnExists(table, column)) {
            st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + def);
            System.out.println("[DB] Kolom ditambahkan: " + table + "." + column);
        }
    }

    private boolean columnExists(String table, String column) throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) return true;
            }
        }
        return false;
    }

    // ===================== DEFAULT DATA =====================

    private void insertDefaultData() {
        if (!isConnected()) return;
        // Hanya insert jika tabel masih kosong
        try {
            if (countTable("kelas") == 0) {
                insertKelas("A");
                insertKelas("B");
                System.out.println("[DB] Default kelas dimasukkan.");
            }

            // Default guru
            if (countTable("guru") == 0) {
                String[][] gurus = {
                    {"admin",   "admin123",  "Administrator",  "Semua"},
                    {"bu_siti", "siti123",   "Bu Siti Rahayu", "A"},
                    {"bu_dewi", "dewi123",   "Bu Dewi Lestari","B"}
                };
                for (String[] g : gurus) insertGuru(g[0], g[1], g[2], g[3]);
                System.out.println("[DB] Default guru dimasukkan.");
            }
            ensureDefaultGuruRoles();

            // Default siswa
            if (countTable("siswa") == 0) {
                String[][] siswas = {
                    {"Andi",   "A", "Alaral Laman",   "10/03/2020"},
                    {"Budi",   "A", "Nama Orang",     "15/04/2020"},
                    {"Chika",  "A", "Apna Darru",     "22/01/2020"},
                    {"Doni",   "A", "Nama Orang",     "08/07/2020"},
                    {"Erest",  "A", "Nama Dirang",    "03/09/2020"},
                    {"Fajar",  "A", "Rudy Lahadi",    "17/02/2020"},
                    {"Gita",   "A", "Amar Kaisen",    "25/06/2020"},
                    {"Hani",   "A", "Kasti Lamtan",   "11/11/2019"},
                    {"Ilham",  "A", "Komi Orang",     "30/08/2020"},
                    {"Joko",   "A", "Kama Orang",     "05/05/2020"},
                    {"Rina",   "B", "Siti Aminah",    "27/07/2021"},
                    {"Sari",   "B", "Agus Wijaya",    "09/01/2021"},
                    {"Tono",   "B", "Dewi Lestari",   "18/10/2021"},
                    {"Udin",   "B", "Hendra Gunawan", "23/04/2021"},
                    {"Vina",   "B", "Sri Wahyuni",    "01/06/2021"},
                    {"Widi",   "B", "Joko Prasetyo",  "16/08/2021"},
                    {"Xena",   "B", "Eko Susanto",    "07/02/2021"},
                    {"Yuni",   "B", "Retno W.",       "12/11/2021"},
                    {"Zaki",   "B", "Budi Santoso",   "20/03/2021"},
                    {"Ani",    "B", "Kami Orang",     "14/05/2021"}
                };
                for (String[] s : siswas) insertSiswa(s[0], s[1], s[2], s[3]);
                System.out.println("[DB] Default siswa dimasukkan.");
            }
            ensureDefaultSettings();
        } catch (Exception e) {
            System.err.println("[DB] Error insertDefaultData: " + e.getMessage());
        }
    }

    private void ensureDefaultSettings() {
        upsertSettingIfAbsent("nama_sekolah", "TUNAS HARAPAN");
    }

    private void upsertSettingIfAbsent(String key, String value) {
        if (!isConnected()) return;
        String sql = "INSERT INTO app_settings(kunci, nilai) VALUES(?, ?) " +
                     "ON CONFLICT(kunci) DO NOTHING";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value == null ? "" : value);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error upsertSettingIfAbsent: " + e.getMessage());
        }
    }

    private int countTable(String table) throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void ensureDefaultGuruRoles() {
        if (!isConnected()) return;
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("UPDATE guru SET role='Administrator' WHERE lower(username)='admin'");
            st.executeUpdate("UPDATE guru SET role='Guru' WHERE role IS NULL OR trim(role)=''");
        } catch (SQLException e) {
            System.err.println("[DB] Error ensureDefaultGuruRoles: " + e.getMessage());
        }
    }

    // ===================== KELAS CRUD =====================

    public List<Kelas> getAllKelas() {
        List<Kelas> list = new ArrayList<>();
        if (!isConnected()) return list;
        String sql = "SELECT id, nama FROM kelas ORDER BY nama";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Kelas(rs.getInt("id"), rs.getString("nama")));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error getAllKelas: " + e.getMessage());
        }
        return list;
    }

    public Kelas getKelasById(int id) {
        if (!isConnected()) return null;
        String sql = "SELECT id, nama FROM kelas WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Kelas(rs.getInt("id"), rs.getString("nama"));
        } catch (SQLException e) {
            System.err.println("[DB] Error getKelasById: " + e.getMessage());
        }
        return null;
    }

    public boolean kelasExists(String nama, Integer excludeId) {
        if (!isConnected()) return false;
        String sql = (excludeId == null)
                ? "SELECT COUNT(*) FROM kelas WHERE lower(nama)=lower(?)"
                : "SELECT COUNT(*) FROM kelas WHERE lower(nama)=lower(?) AND id<>?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nama);
            if (excludeId != null) ps.setInt(2, excludeId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public int insertKelas(String nama) {
        if (!isConnected()) return -1;
        String sql = "INSERT INTO kelas(nama) VALUES(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nama);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        } catch (SQLException e) {
            System.err.println("[DB] Error insertKelas: " + e.getMessage());
            return -1;
        }
    }

    public boolean updateKelas(int id, String nama) {
        if (!isConnected()) return false;
        String sql = "UPDATE kelas SET nama=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nama);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Error updateKelas: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteKelas(int id) {
        if (!isConnected()) return false;
        String sql = "DELETE FROM kelas WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Error deleteKelas: " + e.getMessage());
            return false;
        }
    }

    // ===================== GURU CRUD =====================

    public Guru loginGuru(String username, String password) {
        if (!isConnected()) return null;
        String sql = "SELECT * FROM guru WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString("password");
                    if (PasswordUtil.verifyPassword(password, stored)) {
                        if (!PasswordUtil.isHashed(stored)) {
                            upgradePasswordHash(username, password);
                        }
                        return mapGuru(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error loginGuru: " + e.getMessage());
        }
        return null;
    }

    public boolean usernameExists(String username) {
        if (!isConnected()) return false;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM guru WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean insertGuru(String username, String password,
                              String namaLengkap, String kelompok) {
        String role = "admin".equalsIgnoreCase(username) ? "Administrator" : "Guru";
        return insertGuru("", namaLengkap, "", "", "", "", "", "", role, kelompok, username, password);
    }

    public boolean insertGuru(String nip, String namaLengkap, String tempatLahir, String tanggalLahir, String ttl, String jenisKelamin,
                              String alamat, String agama, String role, String kelasAmpu,
                              String username, String password) {
        if (!isConnected()) return false;
        String sql = "INSERT INTO guru(username,password,nama_lengkap,nip,tempat_lahir,tanggal_lahir,ttl,jenis_kelamin,alamat,agama,role,kelompok_ampu) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            String hashed = PasswordUtil.isHashed(password) ? password : PasswordUtil.hashPassword(password);
            ps.setString(1, username);
            ps.setString(2, hashed);
            ps.setString(3, namaLengkap);
            ps.setString(4, nip);
            ps.setString(5, tempatLahir);
            ps.setString(6, tanggalLahir);
            ps.setString(7, ttl);
            ps.setString(8, jenisKelamin);
            ps.setString(9, alamat);
            ps.setString(10, agama);
            ps.setString(11, role);
            ps.setString(12, kelasAmpu);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[DB] Error insertGuru: " + e.getMessage());
            return false;
        }
    }

    public List<Guru> getAllGuru() {
        List<Guru> list = new ArrayList<>();
        if (!isConnected()) return list;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM guru ORDER BY nama_lengkap")) {
            while (rs.next()) {
                list.add(mapGuru(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error getAllGuru: " + e.getMessage());
        }
        return list;
    }

    public Guru getGuruByUsername(String username) {
        if (!isConnected()) return null;
        String sql = "SELECT * FROM guru WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapGuru(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error getGuruByUsername: " + e.getMessage());
        }
        return null;
    }

    public boolean updateGuru(String oldUsername, String nip, String namaLengkap, String tempatLahir, String tanggalLahir, String ttl, String jenisKelamin,
                              String alamat, String agama, String role, String kelasAmpu,
                              String username, String password) {
        if (!isConnected()) return false;
        String sql = "UPDATE guru SET username=?, password=?, nama_lengkap=?, nip=?, tempat_lahir=?, tanggal_lahir=?, ttl=?, jenis_kelamin=?, alamat=?, agama=?, role=?, kelompok_ampu=? " +
                     "WHERE username=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String hashed = PasswordUtil.isHashed(password) ? password : PasswordUtil.hashPassword(password);
            ps.setString(1, username);
            ps.setString(2, hashed);
            ps.setString(3, namaLengkap);
            ps.setString(4, nip);
            ps.setString(5, tempatLahir);
            ps.setString(6, tanggalLahir);
            ps.setString(7, ttl);
            ps.setString(8, jenisKelamin);
            ps.setString(9, alamat);
            ps.setString(10, agama);
            ps.setString(11, role);
            ps.setString(12, kelasAmpu);
            ps.setString(13, oldUsername);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Error updateGuru: " + e.getMessage());
            return false;
        }
    }

    private void upgradePasswordHash(String username, String plainPassword) {
        String sql = "UPDATE guru SET password=? WHERE username=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hashPassword(plainPassword));
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error upgradePasswordHash: " + e.getMessage());
        }
    }

    private Guru mapGuru(ResultSet rs) throws SQLException {
        String tempatLahir = rs.getString("tempat_lahir");
        String tanggalLahir = rs.getString("tanggal_lahir");
        if ((tempatLahir == null || tempatLahir.isEmpty()) && (tanggalLahir == null || tanggalLahir.isEmpty())) {
            String ttl = rs.getString("ttl");
            if (ttl != null && !ttl.isEmpty()) {
                String[] p = ttl.split(",", 2);
                if (p.length == 2) {
                    tempatLahir = p[0].trim();
                    tanggalLahir = p[1].trim();
                } else {
                    tempatLahir = ttl.trim();
                    tanggalLahir = "";
                }
            }
        }
        return new Guru(
            rs.getString("nip"),
            rs.getString("nama_lengkap"),
            tempatLahir == null ? "" : tempatLahir,
            tanggalLahir == null ? "" : tanggalLahir,
            rs.getString("jenis_kelamin"),
            rs.getString("alamat"),
            rs.getString("agama"),
            rs.getString("kelompok_ampu"),
            normalizeRole(rs.getString("role")),
            rs.getString("username"),
            rs.getString("password")
        );
    }

    private String normalizeRole(String role) {
        if (role == null || role.trim().isEmpty()) return "Guru";
        if ("administrator".equalsIgnoreCase(role)) return "Administrator";
        return "Guru";
    }

    public boolean deleteGuru(String username) {
        if (!isConnected()) return false;
        String sql = "DELETE FROM guru WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Error deleteGuru: " + e.getMessage());
            return false;
        }
    }

    // ===================== SISWA CRUD =====================

    public List<Siswa> getAllSiswa() {
        return getAllSiswa(LocalDate.now(), null);
    }

    public List<Siswa> getAllSiswa(LocalDate tanggal) {
        return getAllSiswa(tanggal, null);
    }

    public List<Siswa> getAllSiswa(LocalDate tanggal, String angkatan) {
        List<Siswa> list = new ArrayList<>();
        if (!isConnected()) return list;
        boolean useAngkatan = angkatan != null && !angkatan.trim().isEmpty();
        String sql = "SELECT s.*, COALESCE(a.status, '-') as status_absensi " +
                     "FROM siswa s " +
                     "LEFT JOIN absensi a ON s.id = a.siswa_id AND a.tanggal = ? " +
                     (useAngkatan ? "WHERE s.angkatan = ? " : "") +
                     "ORDER BY s.kelompok, s.nama";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tanggal.toString());
            if (useAngkatan) ps.setString(2, angkatan);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Siswa s = new Siswa(
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("kelompok"),
                    rs.getString("nama_orang_tua"),
                    rs.getString("tanggal_lahir"),
                    rs.getString("npd"),
                    rs.getString("nisn"),
                    rs.getString("tempat_lahir"),
                    rs.getString("alamat"),
                    rs.getString("jenis_kelamin"),
                    rs.getString("agama"),
                    rs.getString("kontak_wali"),
                    rs.getString("angkatan")
                );
                s.setStatusAbsensi(rs.getString("status_absensi"));
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error getAllSiswa: " + e.getMessage());
        }
        return list;
    }

    public Siswa getSiswaById(int id) {
        if (!isConnected()) return null;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM siswa WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Siswa(rs.getInt("id"), rs.getString("nama"),
                    rs.getString("kelompok"), rs.getString("nama_orang_tua"),
                    rs.getString("tanggal_lahir"),
                    rs.getString("npd"),
                    rs.getString("nisn"),
                    rs.getString("tempat_lahir"),
                    rs.getString("alamat"),
                    rs.getString("jenis_kelamin"),
                    rs.getString("agama"),
                    rs.getString("kontak_wali"),
                    rs.getString("angkatan"));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error getSiswaById: " + e.getMessage());
        }
        return null;
    }

    public boolean isDuplicateSiswa(String nama, String namaOrangTua, int excludeId) {
        if (!isConnected()) return false;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM siswa WHERE LOWER(nama)=LOWER(?) " +
                "AND LOWER(nama_orang_tua)=LOWER(?) AND id != ?")) {
            ps.setString(1, nama);
            ps.setString(2, namaOrangTua);
            ps.setInt(3, excludeId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public int insertSiswa(String nama, String kelompok,
                            String namaOrangTua, String tanggalLahir) {
        return insertSiswa(nama, kelompok, namaOrangTua, tanggalLahir, "", "", "", "", "", "", "", "");
    }

    public int insertSiswa(String nama, String kelompok, String namaWali, String tanggalLahir,
                           String npd, String nisn, String tempatLahir,
                           String jenisKelamin, String kontakWali, String angkatan) {
        return insertSiswa(nama, kelompok, namaWali, tanggalLahir,
                           npd, nisn, tempatLahir, "", jenisKelamin, "", kontakWali, angkatan);
    }

    public int insertSiswa(String nama, String kelompok, String namaWali, String tanggalLahir,
                           String npd, String nisn, String tempatLahir, String alamat,
                           String jenisKelamin, String agama, String kontakWali, String angkatan) {
        if (!isConnected()) return -1;
        String sql = "INSERT INTO siswa(" +
                     "nama,kelompok,nama_orang_tua,tanggal_lahir,npd,nisn,tempat_lahir,alamat,jenis_kelamin,agama,kontak_wali,angkatan" +
                     ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nama);
            ps.setString(2, kelompok);
            ps.setString(3, namaWali);
            ps.setString(4, tanggalLahir);
            ps.setString(5, npd);
            ps.setString(6, nisn);
            ps.setString(7, tempatLahir);
            ps.setString(8, alamat);
            ps.setString(9, jenisKelamin);
            ps.setString(10, agama);
            ps.setString(11, kontakWali);
            ps.setString(12, angkatan);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        } catch (SQLException e) {
            System.err.println("[DB] Error insertSiswa: " + e.getMessage());
            return -1;
        }
    }

    public boolean updateSiswa(int id, String nama, String kelompok,
                                String namaOrangTua, String tanggalLahir) {
        return updateSiswa(id, nama, kelompok, namaOrangTua, tanggalLahir, "", "", "", "", "", "", "", "");
    }

    public boolean updateSiswa(int id, String nama, String kelompok, String namaWali, String tanggalLahir,
                               String npd, String nisn, String tempatLahir,
                               String jenisKelamin, String kontakWali, String angkatan) {
        return updateSiswa(id, nama, kelompok, namaWali, tanggalLahir,
                           npd, nisn, tempatLahir, "", jenisKelamin, "", kontakWali, angkatan);
    }

    public boolean updateSiswa(int id, String nama, String kelompok, String namaWali, String tanggalLahir,
                               String npd, String nisn, String tempatLahir, String alamat,
                               String jenisKelamin, String agama, String kontakWali, String angkatan) {
        if (!isConnected()) return false;
        String sql = "UPDATE siswa SET " +
                     "nama=?,kelompok=?,nama_orang_tua=?,tanggal_lahir=?," +
                     "npd=?,nisn=?,tempat_lahir=?,alamat=?,jenis_kelamin=?,agama=?,kontak_wali=?,angkatan=? " +
                     "WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nama);
            ps.setString(2, kelompok);
            ps.setString(3, namaWali);
            ps.setString(4, tanggalLahir);
            ps.setString(5, npd);
            ps.setString(6, nisn);
            ps.setString(7, tempatLahir);
            ps.setString(8, alamat);
            ps.setString(9, jenisKelamin);
            ps.setString(10, agama);
            ps.setString(11, kontakWali);
            ps.setString(12, angkatan);
            ps.setInt(13, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Error updateSiswa: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSiswa(int id) {
        if (!isConnected()) return false;
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM siswa WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Error deleteSiswa: " + e.getMessage());
            return false;
        }
    }

    public int countSiswa() {
        return countSiswa(null);
    }

    public int countSiswa(String angkatan) {
        if (!isConnected()) return 0;
        boolean useAngkatan = angkatan != null && !angkatan.trim().isEmpty();
        String sql = useAngkatan ? "SELECT COUNT(*) FROM siswa WHERE angkatan=?" : "SELECT COUNT(*) FROM siswa";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (useAngkatan) ps.setString(1, angkatan);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    public int countSiswaByKelompok(String kelompok) {
        if (!isConnected()) return 0;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM siswa WHERE kelompok = ?")) {
            ps.setString(1, kelompok);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    // ===================== ABSENSI CRUD =====================

    /** Simpan/update status absensi hari ini */
    public boolean simpanAbsensi(int siswaId, String status) {
        return simpanAbsensi(siswaId, status, LocalDate.now());
    }

    public boolean simpanAbsensi(int siswaId, String status, LocalDate tanggal) {
        if (!isConnected()) return false;
        // UPSERT: insert atau update jika sudah ada untuk tanggal terpilih
        String sql = "INSERT INTO absensi(siswa_id, tanggal, status) " +
                     "VALUES(?, ?, ?) " +
                     "ON CONFLICT(siswa_id, tanggal) DO UPDATE SET status = excluded.status";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, siswaId);
            ps.setString(2, tanggal.toString());
            ps.setString(3, status);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[DB] Error simpanAbsensi: " + e.getMessage());
            return false;
        }
    }

    public int hapusAbsensi(LocalDate tanggal, String kelas) {
        if (!isConnected()) return 0;
        boolean byKelas = kelas != null && !kelas.trim().isEmpty();
        String sql = byKelas
                ? "DELETE FROM absensi WHERE tanggal=? AND siswa_id IN (SELECT id FROM siswa WHERE kelompok=?)"
                : "DELETE FROM absensi WHERE tanggal=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tanggal.toString());
            if (byKelas) ps.setString(2, kelas);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error hapusAbsensi: " + e.getMessage());
            return 0;
        }
    }

    /** Ambil status absensi hari ini untuk satu siswa */
    public String getStatusAbsensiHariIni(int siswaId) {
        if (!isConnected()) return "-";
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT status FROM absensi WHERE siswa_id=? AND tanggal=date('now','localtime')")) {
            ps.setInt(1, siswaId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("status") : "-";
        } catch (SQLException e) {
            return "-";
        }
    }

    /** Hitung jumlah siswa berdasarkan status absensi hari ini */
    public int countAbsensiHariIni(String status) {
        return countAbsensiHariIni(status, null);
    }

    public int countAbsensiHariIni(String status, String angkatan) {
        if (!isConnected()) return 0;
        boolean useAngkatan = angkatan != null && !angkatan.trim().isEmpty();
        String sql = useAngkatan
                ? "SELECT COUNT(*) FROM absensi a JOIN siswa s ON s.id=a.siswa_id WHERE a.tanggal=date('now','localtime') AND a.status=? AND s.angkatan=?"
                : "SELECT COUNT(*) FROM absensi WHERE tanggal=date('now','localtime') AND status=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            if (useAngkatan) ps.setString(2, angkatan);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    /** Hitung siswa yang belum diabsen hari ini */
    public int countBelumAbsen() {
        return countBelumAbsen(LocalDate.now(), null);
    }

    public int countBelumAbsen(LocalDate tanggal) {
        return countBelumAbsen(tanggal, null);
    }

    public int countBelumAbsen(LocalDate tanggal, String angkatan) {
        if (!isConnected()) return countSiswa(angkatan);
        boolean useAngkatan = angkatan != null && !angkatan.trim().isEmpty();
        String sql = useAngkatan
                ? "SELECT COUNT(*) FROM siswa WHERE angkatan=? AND id NOT IN (SELECT siswa_id FROM absensi WHERE tanggal=?)"
                : "SELECT COUNT(*) FROM siswa WHERE id NOT IN (SELECT siswa_id FROM absensi WHERE tanggal=?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (useAngkatan) {
                ps.setString(1, angkatan);
                ps.setString(2, tanggal.toString());
            } else {
                ps.setString(1, tanggal.toString());
            }
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return countSiswa(angkatan);
        }
    }

    /** Rekap absensi per siswa untuk laporan bulanan */
    public List<Object[]> getLaporanBulan(int tahun, int bulan) {
        return getLaporanBulanFiltered(tahun, bulan, null, null, null);
    }

    /**
     * Rekap absensi per siswa untuk laporan bulanan dengan filter harian/status.
     * @param tahun Tahun laporan
     * @param bulan Bulan laporan (1-12)
     * @param hari  Hari dalam bulan (1-31), null untuk semua hari
     * @param status Status absensi (Hadir/Izin/Sakit/Alpa), null untuk semua status
     */
    public List<Object[]> getLaporanBulanFiltered(int tahun, int bulan, Integer hari, String status) {
        return getLaporanBulanFiltered(tahun, bulan, hari, status, null);
    }

    public List<Object[]> getLaporanBulanFiltered(int tahun, int bulan, Integer hari, String status, String kelas) {
        return getLaporanBulanFiltered(tahun, bulan, hari, status, kelas, null);
    }

    public List<Object[]> getLaporanBulanFiltered(int tahun, int bulan, Integer hari, String status, String kelas, String angkatan) {
        List<Object[]> list = new ArrayList<>();
        if (!isConnected()) return list;

        String bulanStr = String.format("%04d-%02d", tahun, bulan);
        String tanggalFilter = (hari != null)
                ? String.format("%04d-%02d-%02d", tahun, bulan, hari)
                : null;
        boolean useStatus = status != null && !status.trim().isEmpty();
        boolean useKelas = kelas != null && !kelas.trim().isEmpty();
        boolean useAngkatan = angkatan != null && !angkatan.trim().isEmpty();

        StringBuilder sql = new StringBuilder(
            "SELECT s.id, s.nama, s.kelompok, " +
            "  SUM(CASE WHEN a.status='Hadir' THEN 1 ELSE 0 END) as hadir, " +
            "  SUM(CASE WHEN a.status='Izin'  THEN 1 ELSE 0 END) as izin, " +
            "  SUM(CASE WHEN a.status='Sakit' THEN 1 ELSE 0 END) as sakit, " +
            "  SUM(CASE WHEN a.status='Alpa'  THEN 1 ELSE 0 END) as alpa, " +
            "  COUNT(a.id) as total " +
            "FROM siswa s " +
            "LEFT JOIN absensi a ON s.id = a.siswa_id " +
            "  AND strftime('%Y-%m', a.tanggal) = ? ");

        List<Object> params = new ArrayList<>();
        params.add(bulanStr);

        if (hari != null || useStatus || useKelas || useAngkatan) {
            sql.append("WHERE EXISTS (SELECT 1 FROM absensi af WHERE af.siswa_id = s.id ");
            if (hari != null) {
                sql.append("AND af.tanggal = ? ");
                params.add(tanggalFilter);
            } else {
                sql.append("AND strftime('%Y-%m', af.tanggal) = ? ");
                params.add(bulanStr);
            }
            if (useStatus) {
                sql.append("AND af.status = ? ");
                params.add(status);
            }
            sql.append(") ");
            if (useKelas) {
                sql.append("AND s.kelompok = ? ");
                params.add(kelas);
            }
            if (useAngkatan) {
                sql.append("AND s.angkatan = ? ");
                params.add(angkatan);
            }
        }

        sql.append("GROUP BY s.id ORDER BY s.kelompok, s.nama");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("kelompok"),
                    rs.getInt("hadir"),
                    rs.getInt("izin"),
                    rs.getInt("sakit"),
                    rs.getInt("alpa"),
                    rs.getInt("total")
                });
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error getLaporanBulanFiltered: " + e.getMessage());
        }
        return list;
    }

    public List<String> getDaftarAngkatan() {
        List<String> list = new ArrayList<>();
        if (!isConnected()) return list;
        String sql = "SELECT DISTINCT angkatan FROM siswa WHERE trim(angkatan)<>'' ORDER BY CAST(angkatan AS INTEGER)";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException e) {
            System.err.println("[DB] Error getDaftarAngkatan: " + e.getMessage());
        }
        return list;
    }

    public String getSetting(String key, String defaultValue) {
        if (!isConnected()) return defaultValue;
        String sql = "SELECT nilai FROM app_settings WHERE kunci=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String v = rs.getString(1);
                return (v == null || v.trim().isEmpty()) ? defaultValue : v.trim();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error getSetting: " + e.getMessage());
        }
        return defaultValue;
    }

    public boolean setSetting(String key, String value) {
        if (!isConnected()) return false;
        String sql = "INSERT INTO app_settings(kunci, nilai) VALUES(?, ?) " +
                     "ON CONFLICT(kunci) DO UPDATE SET nilai=excluded.nilai";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value == null ? "" : value.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Error setSetting: " + e.getMessage());
            return false;
        }
    }

    public List<Object[]> getDetailAbsensiSiswaBulanan(int siswaId, int tahun, int bulan) {
        List<Object[]> list = new ArrayList<>();
        if (!isConnected()) return list;
        String bulanStr = String.format("%04d-%02d", tahun, bulan);
        String sql = "SELECT tanggal, status FROM absensi " +
                     "WHERE siswa_id=? AND strftime('%Y-%m', tanggal)=? " +
                     "ORDER BY tanggal ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, siswaId);
            ps.setString(2, bulanStr);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{rs.getString("tanggal"), rs.getString("status")});
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error getDetailAbsensiSiswaBulanan: " + e.getMessage());
        }
        return list;
    }

    // ===================== UTILS =====================

    private void showDBError(String msg) {
        System.err.println("[DB ERROR] " + msg);
        javax.swing.JOptionPane.showMessageDialog(null,
            msg, "Error Database", javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    public String getDbFile() { return DB_FILE; }
}
