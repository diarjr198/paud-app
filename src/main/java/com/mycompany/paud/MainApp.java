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

public class MainApp {
    // Menjadi titik awal jalannya aplikasi.
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tutup koneksi DB saat program ditutup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DataStore.getInstance().closeDB();
            System.out.println("[App] Database ditutup dengan aman.");
        }));

        SwingUtilities.invokeLater(() -> {
            // Cek apakah SQLite driver tersedia
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null,
                    "SQLite JDBC driver tidak ditemukan!\n\n" +
                    "Cara menambahkan:\n" +
                    "1. Download sqlite-jdbc-*.jar dari:\n" +
                    "   https://github.com/xerial/sqlite-jdbc/releases\n\n" +
                    "2. Di NetBeans:\n" +
                    "   Klik kanan Project > Properties > Libraries\n" +
                    "   > Add JAR/Folder > pilih file .jar\n\n" +
                    "3. Jalankan ulang program.",
                    "Setup Database", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}

