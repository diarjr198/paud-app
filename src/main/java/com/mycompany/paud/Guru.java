/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.paud;

/**
 *
 * @author diarjr198
 */
public class Guru {
    private String nip;
    private String namaLengkap;
    private String tempatLahir;
    private String tanggalLahir;
    private String jenisKelamin;
    private String alamat;
    private String agama;
    private String kelasAmpu; // bisa lebih dari satu, contoh: A,B
    private String role; // Administrator atau Guru
    private String username;
    private String password;

    // Menjalankan inisialisasi objek Guru.
    public Guru(String username, String password, String namaLengkap, String kelompokAmpu) {
        this("", namaLengkap, "", "", "", "", "", kelompokAmpu,
             "admin".equalsIgnoreCase(username) ? "Administrator" : "Guru",
             username, password);
    }

    public Guru(String nip, String namaLengkap, String tempatLahir, String tanggalLahir, String jenisKelamin,
                String alamat, String agama, String kelasAmpu, String username, String password) {
        this(nip, namaLengkap, tempatLahir, tanggalLahir, jenisKelamin, alamat, agama, kelasAmpu, "Guru", username, password);
    }

    public Guru(String nip, String namaLengkap, String tempatLahir, String tanggalLahir, String jenisKelamin,
                String alamat, String agama, String kelasAmpu, String role, String username, String password) {
        this.nip = nip;
        this.namaLengkap = namaLengkap;
        this.tempatLahir = tempatLahir;
        this.tanggalLahir = tanggalLahir;
        this.jenisKelamin = jenisKelamin;
        this.alamat = alamat;
        this.agama = agama;
        this.kelasAmpu = kelasAmpu;
        this.role = role;
        this.username = username;
        this.password = password;
    }

    // Mengambil nilai properti yang dibutuhkan.
    public String getNip() { return nip; }
    // Mengubah nilai properti sesuai input.
    public void setNip(String nip) { this.nip = nip; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getTempatLahir() { return tempatLahir; }
    // Mengubah nilai properti sesuai input.
    public void setTempatLahir(String tempatLahir) { this.tempatLahir = tempatLahir; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getTanggalLahir() { return tanggalLahir; }
    // Mengubah nilai properti sesuai input.
    public void setTanggalLahir(String tanggalLahir) { this.tanggalLahir = tanggalLahir; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getJenisKelamin() { return jenisKelamin; }
    // Mengubah nilai properti sesuai input.
    public void setJenisKelamin(String jenisKelamin) { this.jenisKelamin = jenisKelamin; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getAlamat() { return alamat; }
    // Mengubah nilai properti sesuai input.
    public void setAlamat(String alamat) { this.alamat = alamat; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getAgama() { return agama; }
    // Mengubah nilai properti sesuai input.
    public void setAgama(String agama) { this.agama = agama; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getKelasAmpu() { return kelasAmpu; }
    // Mengubah nilai properti sesuai input.
    public void setKelasAmpu(String kelasAmpu) { this.kelasAmpu = kelasAmpu; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getRole() { return role; }
    // Mengubah nilai properti sesuai input.
    public void setRole(String role) { this.role = role; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getUsername()       { return username; }
    // Mengubah nilai properti sesuai input.
    public void setUsername(String username) { this.username = username; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getPassword()       { return password; }
    // Mengubah nilai properti sesuai input.
    public void   setPassword(String p) { this.password = p; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getNamaLengkap()    { return namaLengkap; }
    // Mengubah nilai properti sesuai input.
    public void   setNamaLengkap(String n) { this.namaLengkap = n; }
    // Backward-compatible aliases
    public String getKelompokAmpu()   { return kelasAmpu; }
    // Mengubah nilai properti sesuai input.
    public void   setKelompokAmpu(String k) { this.kelasAmpu = k; }
    // Menangani proses: get tempat tanggal lahir.
    public String getTempatTanggalLahir() {
        if ((tanggalLahir == null || tanggalLahir.isEmpty()) && (tempatLahir == null || tempatLahir.isEmpty())) return "";
        if (tanggalLahir == null || tanggalLahir.isEmpty()) return tempatLahir == null ? "" : tempatLahir;
        if (tempatLahir == null || tempatLahir.isEmpty()) return tanggalLahir;
        return tempatLahir + ", " + tanggalLahir;
    }
    // Menangani proses: set tempat tanggal lahir.
    public void setTempatTanggalLahir(String tempatTanggalLahir) {
        if (tempatTanggalLahir == null) {
            this.tempatLahir = "";
            this.tanggalLahir = "";
            return;
        }
        String[] p = tempatTanggalLahir.split(",", 2);
        if (p.length == 2) {
            this.tempatLahir = p[0].trim();
            this.tanggalLahir = p[1].trim();
        } else {
            this.tempatLahir = tempatTanggalLahir.trim();
            this.tanggalLahir = "";
        }
    }

    @Override
    // Menangani proses: to string.
    public String toString() { return namaLengkap + " (" + username + ")"; }
}
