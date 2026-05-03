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

    // Menjalankan inisialisasi objek Siswa.
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

    // Mengambil nilai properti yang dibutuhkan.
    public int getId() { return id; }
    // Mengubah nilai properti sesuai input.
    public void setId(int id) { this.id = id; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getNama() { return nama; }
    // Mengubah nilai properti sesuai input.
    public void setNama(String nama) { this.nama = nama; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getKelompok() { return kelompok; }
    // Mengubah nilai properti sesuai input.
    public void setKelompok(String kelompok) { this.kelompok = kelompok; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getNamaWali() { return namaWali; }
    // Mengubah nilai properti sesuai input.
    public void setNamaWali(String namaWali) { this.namaWali = namaWali; }
    // Backward-compatible alias
    public String getNamaOrangTua() { return namaWali; }
    // Mengubah nilai properti sesuai input.
    public void setNamaOrangTua(String namaOrangTua) { this.namaWali = namaOrangTua; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getTanggalLahir() { return tanggalLahir; }
    // Mengubah nilai properti sesuai input.
    public void setTanggalLahir(String tanggalLahir) { this.tanggalLahir = tanggalLahir; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getNpd() { return npd; }
    // Mengubah nilai properti sesuai input.
    public void setNpd(String npd) { this.npd = npd; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getNisn() { return nisn; }
    // Mengubah nilai properti sesuai input.
    public void setNisn(String nisn) { this.nisn = nisn; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getTempatLahir() { return tempatLahir; }
    // Mengubah nilai properti sesuai input.
    public void setTempatLahir(String tempatLahir) { this.tempatLahir = tempatLahir; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getAlamat() { return alamat; }
    // Mengubah nilai properti sesuai input.
    public void setAlamat(String alamat) { this.alamat = alamat; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getJenisKelamin() { return jenisKelamin; }
    // Mengubah nilai properti sesuai input.
    public void setJenisKelamin(String jenisKelamin) { this.jenisKelamin = jenisKelamin; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getAgama() { return agama; }
    // Mengubah nilai properti sesuai input.
    public void setAgama(String agama) { this.agama = agama; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getKontakWali() { return kontakWali; }
    // Mengubah nilai properti sesuai input.
    public void setKontakWali(String kontakWali) { this.kontakWali = kontakWali; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getAngkatan() { return angkatan; }
    // Mengubah nilai properti sesuai input.
    public void setAngkatan(String angkatan) { this.angkatan = angkatan; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getStatusAbsensi() { return statusAbsensi; }
    // Mengubah nilai properti sesuai input.
    public void setStatusAbsensi(String statusAbsensi) { this.statusAbsensi = statusAbsensi; }

    @Override
    // Menangani proses: to string.
    public String toString() { return nama; }
}
