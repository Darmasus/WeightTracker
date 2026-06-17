package com.example.weighttracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weight_tracker.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_USER = "users";
    private static final String COL_USER_ID = "_id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";

    private static final String TABLE_WEIGHT = "weights";
    private static final String COL_WEIGHT_ID = "_id";
    private static final String COL_WEIGHT_USERNAME = "username";
    private static final String COL_WEIGHT_DATE = "date_text";
    private static final String COL_WEIGHT_VALUE = "weight";

    private static final String TABLE_SETTINGS = "user_settings";
    private static final String COL_SETTINGS_USERNAME = "username";
    private static final String COL_SETTINGS_PHONE = "phone";
    private static final String COL_SETTINGS_GOAL = "goal_weight";

    public static class UserSettings {
        public final String username;
        public final String phoneNumber;
        public final Double goalWeight;

        public UserSettings(String username, String phoneNumber, Double goalWeight) {
            this.username = username;
            this.phoneNumber = phoneNumber;
            this.goalWeight = goalWeight;
        }
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUser = "CREATE TABLE " + TABLE_USER + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COL_PASSWORD + " TEXT NOT NULL" +
                ")";
        db.execSQL(createUser);

        String createWeight = "CREATE TABLE " + TABLE_WEIGHT + " (" +
                COL_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_WEIGHT_USERNAME + " TEXT NOT NULL, " +
                COL_WEIGHT_DATE + " TEXT NOT NULL, " +
                COL_WEIGHT_VALUE + " REAL NOT NULL" +
                ")";
        db.execSQL(createWeight);

        String createSettings = "CREATE TABLE " + TABLE_SETTINGS + " (" +
                COL_SETTINGS_USERNAME + " TEXT PRIMARY KEY, " +
                COL_SETTINGS_PHONE + " TEXT, " +
                COL_SETTINGS_GOAL + " REAL" +
                ")";
        db.execSQL(createSettings);

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_weight_username ON " + TABLE_WEIGHT + " (" + COL_WEIGHT_USERNAME + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_weight_date ON " + TABLE_WEIGHT + " (" + COL_WEIGHT_DATE + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_weight_username_date ON " + TABLE_WEIGHT + " (" + COL_WEIGHT_USERNAME + ", " + COL_WEIGHT_DATE + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        onCreate(db);
    }

    // Creates a new user account using the password value already prepared by the login screen.
    public boolean createUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USER, null, values);
        return result != -1;
    }

    // Validates a user login by checking the provided username and prepared password value.
    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USER,
                new String[]{COL_USER_ID},
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, password},
                null,
                null,
                null
        );

        boolean ok = cursor.moveToFirst();
        cursor.close();
        return ok;
    }

    // Inserts a new weight record for the current user.
    public long insertWeight(String username, String dateText, double weight) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WEIGHT_USERNAME, username);
        values.put(COL_WEIGHT_DATE, dateText);
        values.put(COL_WEIGHT_VALUE, weight);
        return db.insert(TABLE_WEIGHT, null, values);
    }

    // Updates an existing weight record.
    public int updateWeight(long id, String username, String dateText, double weight) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WEIGHT_USERNAME, username);
        values.put(COL_WEIGHT_DATE, dateText);
        values.put(COL_WEIGHT_VALUE, weight);
        return db.update(TABLE_WEIGHT, values, COL_WEIGHT_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Deletes a weight record by its database id.
    public int deleteWeight(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_WEIGHT, COL_WEIGHT_ID + "=?", new String[]{String.valueOf(id)});
    }

    public boolean weightEntryExists(String username, String dateText) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_WEIGHT,
                new String[]{COL_WEIGHT_ID},
                COL_WEIGHT_USERNAME + "=? AND " + COL_WEIGHT_DATE + "=?",
                new String[]{username, dateText},
                null,
                null,
                null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();

        return exists;
    }
    public List<com.example.weighttracker.WeightEntry> getWeightsForUser(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                TABLE_WEIGHT,
                new String[]{COL_WEIGHT_ID, COL_WEIGHT_DATE, COL_WEIGHT_VALUE},
                COL_WEIGHT_USERNAME + "=?",
                new String[]{username},
                null,
                null,
                COL_WEIGHT_DATE + " DESC"
        );

        List<com.example.weighttracker.WeightEntry> items = new ArrayList<>();
        while (c.moveToNext()) {
            long id = c.getLong(0);
            String dateText = c.getString(1);
            double w = c.getDouble(2);
            items.add(new com.example.weighttracker.WeightEntry(id, dateText, w));
        }
        c.close();
        return items;
    }

    public Map<String, Double> getWeeklyAverages(String username) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT substr(" + COL_WEIGHT_DATE + ", 1, 7) AS week, AVG(" + COL_WEIGHT_VALUE + ") " +
                        "FROM " + TABLE_WEIGHT + " WHERE " + COL_WEIGHT_USERNAME + "=? " +
                        "GROUP BY week ORDER BY week ASC",
                new String[]{username}
        );

        Map<String, Double> averages = new LinkedHashMap<>();

        while (cursor.moveToNext()) {
            String week = cursor.getString(0);
            double average = cursor.getDouble(1);

            averages.put(week, average);
        }

        cursor.close();

        return averages;
    }

    public void upsertUserSettings(String username, String phoneNumber, Double goalWeight) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SETTINGS_USERNAME, username);
        values.put(COL_SETTINGS_PHONE, phoneNumber);
        if (goalWeight == null) {
            values.putNull(COL_SETTINGS_GOAL);
        } else {
            values.put(COL_SETTINGS_GOAL, goalWeight);
        }

        int updated = db.update(
                TABLE_SETTINGS,
                values,
                COL_SETTINGS_USERNAME + "=?",
                new String[]{username}
        );
        if (updated == 0) {
            db.insert(TABLE_SETTINGS, null, values);
        }
    }

    public UserSettings getUserSettings(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                TABLE_SETTINGS,
                new String[]{COL_SETTINGS_PHONE, COL_SETTINGS_GOAL},
                COL_SETTINGS_USERNAME + "=?",
                new String[]{username},
                null,
                null,
                null
        );

        if (!c.moveToFirst()) {
            c.close();
            return null;
        }

        String phone = c.isNull(0) ? null : c.getString(0);
        Double goal = c.isNull(1) ? null : c.getDouble(1);
        c.close();
        return new UserSettings(username, phone, goal);
    }
}