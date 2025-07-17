package com.example.eventdaddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is already logged in using SharedPreferences
        SharedPreferences prefs = getSharedPreferences("EventDaddyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        // If user is already logged in, skip login screen and go straight to EventGridActivity
        if (isLoggedIn) {
            Intent intent = new Intent(this, EventGridActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Prevent back navigation
            startActivity(intent);
            finish();
        }

        // Load the login screen layout
        setContentView(R.layout.activity_main);

        // Find references to UI elements
        EditText usernameField = findViewById(R.id.usernameField);
        EditText passwordField = findViewById(R.id.passwordField);
        Button loginButton = findViewById(R.id.loginButton);
        Button createAccountButton = findViewById(R.id.createAccountButton);

        // Set up Login button logic
        loginButton.setOnClickListener(v -> handleLogin(usernameField, passwordField, prefs));

        // Set up Create Account button logic
        createAccountButton.setOnClickListener(v -> handleAccountCreation(usernameField, passwordField));
    }

    // Handles login logic and transitions to the SMS permission screen if successful
    private void handleLogin(EditText usernameField, EditText passwordField, SharedPreferences prefs) {
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
        } else {
            DatabaseHelper dbHelper = new DatabaseHelper(this);

            // Check credentials in the database
            if (dbHelper.checkUser(username, password)) {
                int userId = dbHelper.getUserId(username);

                // Save login state and user ID
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.putInt("userId", userId);
                editor.apply();

                // Navigate to SMS permission screen
                Intent intent = new Intent(this, SmsPermissionActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handles new user account creation logic
    private void handleAccountCreation(EditText usernameField, EditText passwordField) {
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
        } else {
            DatabaseHelper dbHelper = new DatabaseHelper(this);

            // Attempt to add user to the database
            if (dbHelper.addUser(username, password)) {
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Account creation failed. Username might already exist.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
