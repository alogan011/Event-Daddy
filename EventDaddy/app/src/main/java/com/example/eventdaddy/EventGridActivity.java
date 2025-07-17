package com.example.eventdaddy;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class EventGridActivity extends AppCompatActivity {

    private int userId; // Logged-in user's ID
    private DatabaseHelper dbHelper;
    private TableLayout eventTable;
    private Button addEventButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_grid);

        // Retrieve userId from intent
        userId = getIntent().getIntExtra("userId", -1);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Find views
        initializeViews();

        // Load existing events for this user
        loadUserEvents();

        // Set up add event button click behavior
        setupAddButtonListener();

        // Set up logout functionality
        setupLogoutButton();
    }

    // Finds and assigns UI elements to variables
    private void initializeViews() {
        eventTable = findViewById(R.id.eventTable);
        addEventButton = findViewById(R.id.addEventButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    // Loads event data from the database and adds rows to the UI
    private void loadUserEvents() {
        loadEventsFromDatabase(eventTable);
    }

    // Handles logic when user clicks Add Event button
    private void setupAddButtonListener() {
        addEventButton.setOnClickListener(v -> openAddEventDialog(eventTable));
    }

    // Logs user out by clearing shared preferences and returning to login screen
    private void setupLogoutButton() {
        logoutButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("EventDaddyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isLoggedIn", false); // Mark user as logged out
            editor.remove("userId"); // Remove user-specific data
            editor.apply();

            // Return to login screen
            Intent intent = new Intent(EventGridActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Retrieves events from the DB and adds each as a new row in the table layout
    private void loadEventsFromDatabase(TableLayout tableLayout) {
        tableLayout.removeAllViews(); // Clear table to avoid duplication

        Cursor cursor = dbHelper.getUserEvents(userId); // Get events for the user

        if (cursor.moveToFirst()) {
            do {
                String eventName = cursor.getString(cursor.getColumnIndex("event_name"));
                String eventDate = cursor.getString(cursor.getColumnIndex("event_date"));
                int eventId = cursor.getInt(cursor.getColumnIndex("event_id"));

                // Add a row to display this event
                addEventRow(tableLayout, eventName, eventDate, eventId);
            } while (cursor.moveToNext());
        }

        cursor.close(); // Always close the cursor
    }

    // Displays a popup dialog to allow user to input a new event
    // Refactored to break up dialog creation and logic for clarity
    private void openAddEventDialog(TableLayout tableLayout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Event");

        LinearLayout dialogLayout = buildDialogLayout();
        EditText eventNameInput = (EditText) dialogLayout.getChildAt(0);
        EditText eventDateInput = (EditText) dialogLayout.getChildAt(1);

        builder.setView(dialogLayout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String eventName = eventNameInput.getText().toString().trim();
            String eventDate = eventDateInput.getText().toString().trim();

            if (!eventName.isEmpty() && !eventDate.isEmpty()) {
                if (dbHelper.addEvent(eventName, eventDate, "10:00 AM", userId)) {
                    loadEventsFromDatabase(tableLayout); // Reload the table
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Builds and returns the layout for the Add Event dialog
    private LinearLayout buildDialogLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        EditText eventNameInput = new EditText(this);
        eventNameInput.setHint("Event Name");

        EditText eventDateInput = new EditText(this);
        eventDateInput.setHint("Event Date (YYYY-MM-DD)");

        layout.addView(eventNameInput);
        layout.addView(eventDateInput);

        return layout;
    }



    // Adds a single event row with name, date, and delete button
    private void addEventRow(TableLayout tableLayout, String eventNameText, String eventDateText, int eventId) {
        TableRow newRow = new TableRow(this);

        // Event Name Column
        TextView eventName = new TextView(this);
        eventName.setText(eventNameText);
        eventName.setPadding(16, 16, 16, 16);
        eventName.setMaxLines(2);
        eventName.setEllipsize(android.text.TextUtils.TruncateAt.END);
        eventName.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1)); // 1/3 width

        // Event Date Column
        TextView eventDate = new TextView(this);
        eventDate.setText(eventDateText);
        eventDate.setPadding(16, 16, 16, 16);
        eventDate.setMaxLines(1);
        eventDate.setEllipsize(android.text.TextUtils.TruncateAt.END);
        eventDate.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1)); // 1/3 width

        // Delete Button Column
        Button deleteButton = new Button(this);
        deleteButton.setText("DELETE");
        deleteButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));//White text
        deleteButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.custom_red));//Red background
        deleteButton.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        deleteButton.setOnClickListener(v -> {
            // Remove from DB and UI
            if (dbHelper.deleteEvent(eventId)) {
                tableLayout.removeView(newRow);
            }
        });

        // Add all views to the row
        newRow.addView(eventName);
        newRow.addView(eventDate);
        newRow.addView(deleteButton);

        // Add row to the table
        tableLayout.addView(newRow);
    }
}
