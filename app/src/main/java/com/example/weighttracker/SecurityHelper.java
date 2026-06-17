package com.example.weighttracker;

import java.security.MessageDigest;

public class SecurityHelper {

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));

            StringBuilder hashedPassword = new StringBuilder();
            for (byte b : hash) {
                hashedPassword.append(String.format("%02x", b));
            }

            return hashedPassword.toString();
        } catch (Exception e) {
            return null;
        }
    }
}