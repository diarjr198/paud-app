package com.mycompany.paud;

public class Kelas {
    private int id;
    private String nama;

    // Menjalankan inisialisasi objek Kelas.
    public Kelas(int id, String nama) {
        this.id = id;
        this.nama = nama;
    }

    // Mengambil nilai properti yang dibutuhkan.
    public int getId() { return id; }
    // Mengubah nilai properti sesuai input.
    public void setId(int id) { this.id = id; }
    // Mengambil nilai properti yang dibutuhkan.
    public String getNama() { return nama; }
    // Mengubah nilai properti sesuai input.
    public void setNama(String nama) { this.nama = nama; }

    @Override
    // Menangani proses: to string.
    public String toString() { return nama; }
}
