package com.example.eventdaddy;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.EditText;
import android.content.SharedPreferences;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class EventGridActivity extends AppCompatActivity {

    private int userId; // Logged-in user's ID
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_grid);

        // Retrieve userId from intent
        userId = getIntent().getIntExtra("userId", -1);
        dbHelper = new DatabaseHelper(this);

        // Find views
        TableLayout eventTable = findViewById(R.id.eventTable);
        Button addEventButton = findViewById(R.id.addEventButton);

        // Load events for the logged-in user
        loadEventsFromDatabase(eventTable);

        // Handle Add Event button click
        addEventButton.setOnClickListener(v -> openAddEventDialog(eventTable));
    }

    private void loadEventsFromDatabase(TableLayout tableLayout) {
        tableLayout.removeAllViews(); // Clear the table before loading

        // Fetch events for the logged-in user
        Cursor cursor = dbHelper.getUserEvents(userId);

        if (cursor.moveToFirst()) {
            do {
                String eventName = cursor.getString(cursor.getColumnIndex("event_name"));
                String eventDate = cursor.getString(cursor.getColumnIndex("event_date"));
                int eventId = cursor.getInt(cursor.getColumnIndex("event_id"));

                // Add the row for this event
                addEventRow(tableLayout, eventName, eventDate, eventId);
            } while (cursor.moveToNext());
        }

        cursor.close(); // Close the cursor to avoid memory leaks
    }

    private void openAddEventDialog(TableLayout tableLayout) {
        // Create a dialog for user input
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add New Event");

        // Create a layout for the dialog
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);

        // Input fields for event name and date
        EditText eventNameInput = new EditText(this);
        eventNameInput.setHint("Event Name");
        dialogLayout.addView(eventNameInput);

        EditText eventDateInput = new EditText(this);
        eventDateInput.setHint("Event Date (YYYY-MM-DD)");
        dialogLayout.addView(eventDateInput);

        builder.setView(dialogLayout);

        // Set dialog buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            String eventName = eventNameInput.getText().toString().trim();
            String eventDate = eventDateInput.getText().toString().trim();

            if (!eventName.isEmpty() && !eventDate.isEmpty()) {
                // Save the event to the database for the logged-in user
                if (dbHelper.addEvent(eventName, eventDate, "10:00 AM", userId)) { // Default time is "10:00 AM"
                    loadEventsFromDatabase(tableLayout); // Reload the table
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void addEventRow(TableLayout tableLayout, String eventNameText, String eventDateText, int eventId) {
        TableRow newRow = new TableRow(this);

        // Event Name TextView
        TextView eventName = new TextView(this);
        eventName.setText(eventNameText);
        eventName.setPadding(16, 16, 16, 16);
        eventName.setMaxLines(2); // Limit to 2 lines if text is long
        eventName.setEllipsize(android.text.TextUtils.TruncateAt.END); // Add "..." if the text exceeds space
        eventName.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1)); // Weight 1

        // Event Date TextView
        TextView eventDate = new TextView(this);
        eventDate.setText(eventDateText);
        eventDate.setPadding(16, 16, 16, 16);
        eventDate.setMaxLines(1); // Limit to 1 line
        eventDate.setEllipsize(android.text.TextUtils.TruncateAt.END); // Add "..." if text is long
        eventDate.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1)); // Weight 1

        // Delete Button
        Button deleteButton = new Button(this);
        deleteButton.setText("DELETE");
        deleteButton.setTextColor(ContextCompat.getColor(this, android.R.color.white)); // White text
        deleteButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.custom_red)); // Red background
        deleteButton.setOnClickListener(v -> {
            // Remove the event from the database
            if (dbHelper.deleteEvent(eventId)) {
                tableLayout.removeView(newRow); // Remove the row from the table
            }
        });
        deleteButton.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

        // Add views to the row
        newRow.addView(eventName);
        newRow.addView(eventDate);
        newRow.addView(deleteButton);

        // Add the row to the table layout
        tableLayout.addView(newRow);

        //Log out button
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("EventDaddyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isLoggedIn", false); // Mark as logged out
            editor.remove("userId"); // Clear userId
            editor.apply();

            // Navigate back to the MainActivity
            Intent intent = new Intent(EventGridActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish();
        });


    }
}


