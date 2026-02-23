package com.example.weighttracker;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SmsSettingsActivity extends AppCompatActivity {

    private static final String TAG = "WeightTrackerSMS";
    private static final int REQ_SMS = 1001;

    private static final String ACTION_SMS_SENT = "com.example.weighttracker.SMS_SENT";

    private DatabaseHelper db;
    private String username;

    private TextView textSmsStatus;
    private EditText editPhone;
    private EditText editGoal;

    private final BroadcastReceiver smsSentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            int resultCode = getResultCode();
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "SmsSettings sent callback: RESULT_OK");
                Toast.makeText(context, "SMS send result: OK", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "SmsSettings sent callback: code=" + resultCode);
                Toast.makeText(context, "SMS send result code: " + resultCode, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_settings);

        db = new DatabaseHelper(this);

        username = getIntent().getStringExtra("username");
        if (username == null || username.trim().isEmpty()) {
            Toast.makeText(this, "Missing user session. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textSmsStatus = findViewById(R.id.textSmsStatus);
        editPhone = findViewById(R.id.editPhone);
        editGoal = findViewById(R.id.editGoal);

        Button btnRequestSms = findViewById(R.id.btnRequestSms);
        Button btnSaveSettings = findViewById(R.id.btnSaveSettings);
        Button btnTestSms = findViewById(R.id.btnTestSms);
        Button btnBack = findViewById(R.id.btnBack);

        loadExisting();
        updateStatus();

        btnRequestSms.setOnClickListener(v -> requestSmsPermission());
        btnSaveSettings.setOnClickListener(v -> saveSettings());
        btnTestSms.setOnClickListener(v -> sendTestSms());
        btnBack.setOnClickListener(v -> finish());
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

    private void loadExisting() {
        DatabaseHelper.UserSettings settings = db.getUserSettings(username);
        if (settings == null) {
            return;
        }
        if (settings.phoneNumber != null) {
            editPhone.setText(settings.phoneNumber);
        }
        if (settings.goalWeight != null) {
            editGoal.setText(String.valueOf(settings.goalWeight));
        }
    }

    private void saveSettings() {
        String phone = editPhone.getText().toString().trim();
        String goalText = editGoal.getText().toString().trim();

        Double goal = null;
        if (!goalText.isEmpty()) {
            try {
                goal = Double.parseDouble(goalText);
            } catch (Exception ex) {
                Toast.makeText(this, "Goal weight must be a number.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (goal <= 0) {
                Toast.makeText(this, "Goal weight must be greater than zero.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        db.upsertUserSettings(username, phone.isEmpty() ? null : phone, goal);
        Log.d(TAG, "Saved settings for " + username + ". phone=" + phone + " goal=" + goal);
        Toast.makeText(this, "Settings saved.", Toast.LENGTH_SHORT).show();
        updateStatus();
    }

    private void sendTestSms() {
        Log.d(TAG, "Test SMS requested by user " + username);

        if (!hasSmsPermission()) {
            Log.d(TAG, "Cannot send test SMS. SEND_SMS permission not granted.");
            Toast.makeText(this, "Permission not granted. Request permission first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String phone = editPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            Log.d(TAG, "Cannot send test SMS. Phone number is empty.");
            Toast.makeText(this, "Enter a phone number to send a test SMS.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String message = "Weight Tracker test alert for " + username + ".";

            PendingIntent sentPI = PendingIntent.getBroadcast(
                    this,
                    0,
                    new Intent(ACTION_SMS_SENT),
                    pendingIntentFlags()
            );

            SmsManager.getDefault().sendTextMessage(phone, null, message, sentPI, null);

            Log.d(TAG, "sendTextMessage called. phone=" + phone + " message=" + message);
            Toast.makeText(this, "Attempted to send test SMS. Check Logcat tag " + TAG + ".", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Log.d(TAG, "Exception while sending SMS: " + ex);
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

    private void requestSmsPermission() {
        if (hasSmsPermission()) {
            Log.d(TAG, "SEND_SMS already granted.");
            updateStatus();
            return;
        }

        Log.d(TAG, "Requesting SEND_SMS permission.");
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.SEND_SMS},
                REQ_SMS
        );
    }

    private void updateStatus() {
        if (hasSmsPermission()) {
            textSmsStatus.setText("SMS permission is granted. The app can send goal alerts if a goal and phone number are set.");
        } else {
            textSmsStatus.setText("SMS permission is not granted. The app will still work, but it will not send SMS alerts.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_SMS) {
            Log.d(TAG, "Permission result received for SEND_SMS. grantResults length=" + grantResults.length);
            updateStatus();
        }
    }
}