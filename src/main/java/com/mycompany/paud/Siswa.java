/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.paud;

/**
 *
 * @author diarjr198
 */
public class Siswa {
    private int id;
    private String nama;
    private String kelompok;
    private String namaWali;
    private String tanggalLahir;
    private String npd;
    private String nisn;
    private String tempatLahir;
    private String alamat;
    private String jenisKelamin;
    private String agama;
    private String kontakWali;
    private String angkatan;
    private String statusAbsensi; // Hadir, Izin, Sakit, Alpa, -

    public Siswa(int id, String nama, String kelompok, String namaWali, String tanggalLahir) {
        this(id, nama, kelompok, namaWali, tanggalLahir, "", "", "", "", "", "", "", "");
    }

    public Siswa(int id, String nama, String kelompok, String namaWali, String tanggalLahir,
                 String npd, String nisn, String tempatLahir, String jenisKelamin, String kontakWali) {
        this(id, nama, kelompok, namaWali, tanggalLahir, npd, nisn, tempatLahir, "", jenisKelamin, "", kontakWali, "");
    }

    public Siswa(int id, String nama, String kelompok, String namaWali, String tanggalLahir,
                 String npd, String nisn, String tempatLahir, String jenisKelamin, String kontakWali,
                 String angkatan) {
        this(id, nama, kelompok, namaWali, tanggalLahir,
             npd, nisn, tempatLahir, "", jenisKelamin, "", kontakWali, angkatan);
    }

    public Siswa(int id, String nama, String kelompok, String namaWali, String tanggalLahir,
                 String npd, String nisn, String tempatLahir, String alamat, String jenisKelamin, String agama,
                 String kontakWali, String angkatan) {
        this.id = id;
        this.nama = nama;
        this.kelompok = kelompok;
        this.namaWali = namaWali;
        this.tanggalLahir = tanggalLahir;
        this.npd = npd;
        this.nisn = nisn;
        this.tempatLahir = tempatLahir;
        this.alamat = alamat;
        this.jenisKelamin = jenisKelamin;
        this.agama = agama;
        this.kontakWali = kontakWali;
        this.angkatan = angkatan;
        this.statusAbsensi = "-";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getKelompok() { return kelompok; }
    public void setKelompok(String kelompok) { this.kelompok = kelompok; }
    public String getNamaWali() { return namaWali; }
    public void setNamaWali(String namaWali) { this.namaWali = namaWali; }
    // Backward-compatible alias
    public String getNamaOrangTua() { return namaWali; }
    public void setNamaOrangTua(String namaOrangTua) { this.namaWali = namaOrangTua; }
    public String getTanggalLahir() { return tanggalLahir; }
    public void setTanggalLahir(String tanggalLahir) { this.tanggalLahir = tanggalLahir; }
    public String getNpd() { return npd; }
    public void setNpd(String npd) { this.npd = npd; }
    public String getNisn() { return nisn; }
    public void setNisn(String nisn) { this.nisn = nisn; }
    public String getTempatLahir() { return tempatLahir; }
    public void setTempatLahir(String tempatLahir) { this.tempatLahir = tempatLahir; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public String getJenisKelamin() { return jenisKelamin; }
    public void setJenisKelamin(String jenisKelamin) { this.jenisKelamin = jenisKelamin; }
    public String getAgama() { return agama; }
    public void setAgama(String agama) { this.agama = agama; }
    public String getKontakWali() { return kontakWali; }
    public void setKontakWali(String kontakWali) { this.kontakWali = kontakWali; }
    public String getAngkatan() { return angkatan; }
    public void setAngkatan(String angkatan) { this.angkatan = angkatan; }
    public String getStatusAbsensi() { return statusAbsensi; }
    public void setStatusAbsensi(String statusAbsensi) { this.statusAbsensi = statusAbsensi; }

    @Override
    public String toString() { return nama; }
}
