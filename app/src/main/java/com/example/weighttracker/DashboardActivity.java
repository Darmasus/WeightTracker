package com.example.weighttracker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "WeightTrackerSMS";
    private static final String ACTION_SMS_SENT = "com.example.weighttracker.SMS_SENT_FROM_DASHBOARD";
    private static final int GRID_COLS = 3;

    private DatabaseHelper db;
    private String username;

    private GridLayout gridWeights;
    private TextView textHeader;
    private TextView textGoalSummary;

    private final BroadcastReceiver smsSentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            int resultCode = getResultCode();
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Dashboard sent callback: RESULT_OK");
            } else {
                Log.d(TAG, "Dashboard sent callback: code=" + resultCode);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = new DatabaseHelper(this);

        username = getIntent().getStringExtra("username");
        if (username == null || username.trim().isEmpty()) {
            Toast.makeText(this, "Missing user session. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        textHeader = findViewById(R.id.textHeader);
        textGoalSummary = findViewById(R.id.textGoalSummary);
        gridWeights = findViewById(R.id.gridWeights);

        Button btnAddWeight = findViewById(R.id.btnAddWeight);
        Button btnSmsSettings = findViewById(R.id.btnSmsSettings);
        Button btnLogout = findViewById(R.id.btnLogout);

        textHeader.setText("Daily Weights for " + username);

        btnAddWeight.setOnClickListener(v -> OrEditDialog(null));
        openAdd
        btnSmsSettings.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SmsSettingsActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ACTION_SMS_SENT);
        ContextCompat.registerReceiver(
                this,
                smsSentReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(smsSentReceiver);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshGrid();
    }

    private void refreshGrid() {
        DatabaseHelper.UserSettings settings = db.getUserSettings(username);
        if (settings != null && settings.goalWeight != null) {
            textGoalSummary.setText("Goal weight: " + settings.goalWeight + "   Phone: " + safe(settings.phoneNumber));
        } else {
            textGoalSummary.setText("Goal weight: not set   Phone: not set");
        }

        if (gridWeights.getChildCount() > 0) {
            gridWeights.removeAllViews();
        }
        gridWeights.setColumnCount(GRID_COLS);

        addHeaderCell("Date");
        addHeaderCell("Weight");
        addHeaderCell("Actions");

        List<com.example.weighttracker.WeightEntry> items = db.getWeightsForUser(username);

        Map<String, Double> weeklyAverages = db.getWeeklyAverages(username);

        for (Map.Entry<String, Double> entry : weeklyAverages.entrySet()) {

            Log.d(TAG,
                    "Week: " + entry.getKey() +
                            " Average Weight: " + entry.getValue());
        }

        if (items.size() >= 2) {

            double newestWeight = items.get(0).weight;
            double oldestWeight = items.get(items.size() - 1).weight;

            double difference = newestWeight - oldestWeight;

            if (difference < 0) {
                Log.d(TAG, "User is trending downward.");
            } else if (difference > 0) {
                Log.d(TAG, "User is trending upward.");
            } else {
                Log.d(TAG, "Weight trend is stable.");
            }
        }

        if (items.isEmpty()) {
            addBodyCell("No entries yet", true);
            addBodyCell("", true);
            addBodyCell("", true);
            return;
        }

        for (com.example.weighttracker.WeightEntry e : items) {
            addBodyCell(e.dateText, false);

            TextView wCell = createBodyText(String.valueOf(e.weight), false);
            wCell.setOnClickListener(v -> openAddOrEditDialog(e));
            gridWeights.addView(wCell);

            Button deleteBtn = new Button(this);
            deleteBtn.setText("Delete");
            deleteBtn.setOnClickListener(v -> {
                db.deleteWeight(e.id);
                refreshGrid();
            });

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(dp(4), dp(4), dp(4), dp(4));
            deleteBtn.setLayoutParams(lp);
            gridWeights.addView(deleteBtn);
        }
    }

    private void openAddOrEditDialog(com.example.weighttracker.WeightEntry existing) {
        boolean isEdit = existing != null;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_weight_entry, null);
        EditText editDate = dialogView.findViewById(R.id.editDate);
        EditText editWeight = dialogView.findViewById(R.id.editWeight);

        editDate.setInputType(InputType.TYPE_CLASS_TEXT);
        editWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        if (isEdit) {
            editDate.setText(existing.dateText);
            editWeight.setText(String.valueOf(existing.weight));
        } else {
            editDate.setText(today());
        }

        String title = isEdit ? "Update Weight" : "Add Weight";
        String positive = isEdit ? "Update" : "Save";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(positive, (dialog, which) -> {
                    String dateText = editDate.getText().toString().trim();
                    String weightText = editWeight.getText().toString().trim();

                    if (ValidationHelper.isBlank(dateText) || ValidationHelper.isBlank(weightText)) {
                        Toast.makeText(this, "Please enter both a date and a weight.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double enteredWeight;
                    try {
                        enteredWeight = Double.parseDouble(weightText);
                    } catch (Exception ex) {
                        Toast.makeText(this, "Weight must be a number.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!ValidationHelper.isValidWeight(enteredWeight)) {
                        Toast.makeText(this, "Weight must be between 50 and 1000.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean savedSuccessfully;

                    if (isEdit) {
                        savedSuccessfully = db.updateWeight(existing.id, username, dateText, enteredWeight) > 0;
                    } else {
                        if (db.weightEntryExists(username, dateText)) {
                            Toast.makeText(this, "A weight entry already exists for this date.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        savedSuccessfully = db.insertWeight(username, dateText, enteredWeight) != -1;
                    }

                    if (!savedSuccessfully) {
                        Toast.makeText(this, "Unable to save weight entry.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAG, "Saved weight entry for " + username + ". date=" + dateText + " weight=" + enteredWeight);
                    maybeSendGoalReachedSms(enteredWeight);

                    refreshGrid();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void maybeSendGoalReachedSms(double latestWeight) {
        DatabaseHelper.UserSettings settings = db.getUserSettings(username);

        if (settings == null) {
            Log.d(TAG, "No settings for user. Not sending SMS.");
            return;
        }

        if (settings.goalWeight == null) {
            Log.d(TAG, "Goal not set. Not sending SMS.");
            return;
        }

        if (settings.phoneNumber == null || settings.phoneNumber.trim().isEmpty()) {
            Log.d(TAG, "Phone not set. Not sending SMS.");
            return;
        }

        Double goal = settings.goalWeight;
        if (latestWeight > goal) {
            Log.d(TAG, "Latest weight above goal. Not sending SMS.");
            return;
        }

        if (!hasSmsPermission()) {
            Log.d(TAG, "SEND_SMS not granted. Not sending SMS.");
            return;
        }

        try {
            String msg = "Weight Tracker alert: you reached your goal weight of " + goal + ". Latest: " + latestWeight + ".";

            PendingIntent sentPI = PendingIntent.getBroadcast(
                    this,
                    0,
                    new Intent(ACTION_SMS_SENT),
                    pendingIntentFlags()
            );

            SmsManager.getDefault().sendTextMessage(settings.phoneNumber, null, msg, sentPI, null);

            Log.d(TAG, "sendTextMessage called. phone=" + settings.phoneNumber + " msg=" + msg);
            Toast.makeText(this, "Attempted to send goal SMS. Check Logcat tag " + TAG + ".", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Log.d(TAG, "Exception sending goal SMS: " + ex);
            Toast.makeText(this, "Could not send SMS. Check device support and phone number.", Toast.LENGTH_SHORT).show();
        }
    }

    private int pendingIntentFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.FLAG_UPDATE_CURRENT;
    }

    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void addHeaderCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(8), dp(10), dp(8), dp(10));
        tv.setTextSize(16);
        tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        tv.setLayoutParams(lp);
        gridWeights.addView(tv);
    }

    private void addBodyCell(String text, boolean muted) {
        gridWeights.addView(createBodyText(text, muted));
    }

    private TextView createBodyText(String text, boolean muted) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(dp(8), dp(10), dp(8), dp(10));
        tv.setTextSize(14);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        if (muted) {
            tv.setAlpha(0.7f);
        }

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        tv.setLayoutParams(lp);
        return tv;
    }

    private String today() {
        return new SimpleDateFormat("yyyy MM dd", Locale.US).format(new Date());
    }

    private String safe(String s) {
        return s == null ? "not set" : s;
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }
}