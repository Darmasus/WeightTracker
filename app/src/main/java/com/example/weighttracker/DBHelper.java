package com.example.weighttracker;

import android.content.Context;

/**
 * Legacy class kept for backward compatibility.
 *
 * The app uses DatabaseHelper as the single source of truth.
 */
public class DBHelper extends DatabaseHelper {

    public DBHelper(Context context) {
        super(context);
    }
}