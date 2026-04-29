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

    public String getNip() { return nip; }
    public void setNip(String nip) { this.nip = nip; }
    public String getTempatLahir() { return tempatLahir; }
    public void setTempatLahir(String tempatLahir) { this.tempatLahir = tempatLahir; }
    public String getTanggalLahir() { return tanggalLahir; }
    public void setTanggalLahir(String tanggalLahir) { this.tanggalLahir = tanggalLahir; }
    public String getJenisKelamin() { return jenisKelamin; }
    public void setJenisKelamin(String jenisKelamin) { this.jenisKelamin = jenisKelamin; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public String getAgama() { return agama; }
    public void setAgama(String agama) { this.agama = agama; }
    public String getKelasAmpu() { return kelasAmpu; }
    public void setKelasAmpu(String kelasAmpu) { this.kelasAmpu = kelasAmpu; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getUsername()       { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword()       { return password; }
    public void   setPassword(String p) { this.password = p; }
    public String getNamaLengkap()    { return namaLengkap; }
    public void   setNamaLengkap(String n) { this.namaLengkap = n; }
    // Backward-compatible aliases
    public String getKelompokAmpu()   { return kelasAmpu; }
    public void   setKelompokAmpu(String k) { this.kelasAmpu = k; }
    public String getTempatTanggalLahir() {
        if ((tanggalLahir == null || tanggalLahir.isEmpty()) && (tempatLahir == null || tempatLahir.isEmpty())) return "";
        if (tanggalLahir == null || tanggalLahir.isEmpty()) return tempatLahir == null ? "" : tempatLahir;
        if (tempatLahir == null || tempatLahir.isEmpty()) return tanggalLahir;
        return tempatLahir + ", " + tanggalLahir;
    }
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
    public String toString() { return namaLengkap + " (" + username + ")"; }
}
