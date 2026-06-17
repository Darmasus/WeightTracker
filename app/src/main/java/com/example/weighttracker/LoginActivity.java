package com.example.weighttracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText editUsername;
    private EditText editPassword;
    private DatabaseHelper dbHelper;
    private TextView textLoginStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        textLoginStatus = findViewById(R.id.textLoginStatus);

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnLogin.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (ValidationHelper.isBlank(username) || ValidationHelper.isBlank(password)) {
                showStatus("Please enter a username and password.");
                return;
            }

            String hashedPassword = SecurityHelper.hashPassword(password);
            if (hashedPassword == null) {
                showStatus("Unable to process password securely.");
                return;
            }

            boolean ok = dbHelper.validateUser(username, hashedPassword);
            if (ok) {
                openDashboard(username);
            } else {
                showStatus("Login failed. Check your username and password, or create an account.");
            }
        });

        btnCreateAccount.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (ValidationHelper.isBlank(username) || ValidationHelper.isBlank(password)) {
                showStatus("Please enter a username and password.");
                return;
            }

            if (!ValidationHelper.isValidPassword(password)) {
                showStatus("Password must be at least 6 characters long.");
                return;
            }

            String hashedPassword = SecurityHelper.hashPassword(password);
            if (hashedPassword == null) {
                showStatus("Unable to create account securely.");
                return;
            }

            boolean created = dbHelper.createUser(username, hashedPassword);
            if (created) {
                Toast.makeText(LoginActivity.this, "Account created.", Toast.LENGTH_SHORT).show();
                openDashboard(username);
            } else {
                showStatus("Account creation failed. That username may already exist.");
            }
        });
    }

    private void openDashboard(String username) {
        Toast.makeText(LoginActivity.this, "Welcome, " + username + "!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
        finish();
    }

    private void showStatus(String msg) {
        textLoginStatus.setText(msg);
        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}