package com.example.eventdaddy;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SmsPermissionActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 1;
    private int userId; // To store the userId passed from MainActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        // Retrieve userId from intent
        userId = getIntent().getIntExtra("userId", -1);

        // Grant Permission Button
        Button grantPermissionButton = findViewById(R.id.grantPermissionButton);
        grantPermissionButton.setOnClickListener(v -> requestSmsPermission());

        // Continue Without Permission Button
        Button continueWithoutPermissionButton = findViewById(R.id.continueWithoutPermissionButton);
        continueWithoutPermissionButton.setOnClickListener(v -> {
            Toast.makeText(this, "You can continue without SMS notifications.", Toast.LENGTH_SHORT).show();
            navigateToEventGrid(userId); // Navigate without granting permissions
        });
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission already granted.", Toast.LENGTH_SHORT).show();
            navigateToEventGrid(userId); // Navigate after granting permissions
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS Permission Denied.", Toast.LENGTH_SHORT).show();
            }
            navigateToEventGrid(userId); // Navigate regardless of permission
        }
    }

    private void navigateToEventGrid(int userId) {
        Intent intent = new Intent(this, EventGridActivity.class);
        intent.putExtra("userId", userId); // Pass the userId to EventGridActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(intent);
        finish();
    }
}

