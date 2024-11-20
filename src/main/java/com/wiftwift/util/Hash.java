package com.wiftwift.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Hash {

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        Base64.Encoder enc = Base64.getEncoder();
        return enc.encodeToString(salt);
    }

    public static String hash(String password, String salt) {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 128);
        SecretKeyFactory f;
        try {
            f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = f.generateSecret(spec).getEncoded();
            Base64.Encoder enc = Base64.getEncoder();
            return enc.encodeToString(hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean check(String password, String hash, String salt) {
        return hash(password, salt).equals(hash);
    }

    public static String generateHash() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[50];
        random.nextBytes(bytes);
        Base64.Encoder enc = Base64.getEncoder();
        return enc.encodeToString(bytes);
    }

}
