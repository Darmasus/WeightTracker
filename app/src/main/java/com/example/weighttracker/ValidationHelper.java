package com.example.weighttracker;

public class ValidationHelper {

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidWeight(double weight) {
        return weight >= 50 && weight <= 1000;
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}