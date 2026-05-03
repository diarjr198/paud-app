package com.mycompany.paud;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {
    private static final String ALGO = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final String PREFIX = "pbkdf2$";

    // Menjalankan inisialisasi objek PasswordUtil.
    private PasswordUtil() {}

    // Mengubah password mentah menjadi hash aman.
    public static String hashPassword(String plain) {
        if (plain == null) plain = "";
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(plain.toCharArray(), salt, ITERATIONS, KEY_BITS);
        return PREFIX + ITERATIONS + "$" +
                Base64.getEncoder().encodeToString(salt) + "$" +
                Base64.getEncoder().encodeToString(hash);
    }

    // Memeriksa kecocokan password dengan hash tersimpan.
    public static boolean verifyPassword(String plain, String stored) {
        if (plain == null || stored == null || stored.isEmpty()) return false;
        if (!isHashed(stored)) {
            return stored.equals(plain);
        }

        String[] p = stored.split("\\$");
        if (p.length != 4) return false;

        int iterations;
        try {
            iterations = Integer.parseInt(p[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        byte[] salt;
        byte[] expected;
        try {
            salt = Base64.getDecoder().decode(p[2]);
            expected = Base64.getDecoder().decode(p[3]);
        } catch (IllegalArgumentException e) {
            return false;
        }

        byte[] actual = pbkdf2(plain.toCharArray(), salt, iterations, expected.length * 8);
        return constantTimeEquals(actual, expected);
    }

    // Mengecek apakah nilai sudah berupa hash password.
    public static boolean isHashed(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    // Menangani proses: pbkdf2.
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGO);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Password hashing error", e);
        }
    }

    // Menangani proses: constant time equals.
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
