package com.example.eventdaddy;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

public class EventGridActivity extends AppCompatActivity {

    private int userId;                        // Currently logged-in user's ID
    private DatabaseHelper dbHelper;          // DB helper instance
    private EditText filterInput;             // Search/filter text field
    private TableLayout eventTable;           // Layout to display event rows
    private Button addEventButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_grid);

        // Retrieve userId from previous screen (MainActivity)
        userId = getIntent().getIntExtra("userId", -1);
        dbHelper = new DatabaseHelper(this);  // Initialize DB helper

        // Link layout views
        initializeViews();

        // Setup filtering
        filterInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndDisplayEvents(s.toString().trim());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Load current user’s events
        loadUserEvents();

        // Set up button click actions
        setupAddButtonListener();
        setupLogoutButton();
    }

    // Connect layout views to variables
    private void initializeViews() {
        eventTable = findViewById(R.id.eventTable);
        addEventButton = findViewById(R.id.addEventButton);
        logoutButton = findViewById(R.id.logoutButton);
        filterInput = findViewById(R.id.filterInput);
    }

    // Load all events for this user and display in table
    private void loadUserEvents() {
        loadEventsFromDatabase(eventTable);
    }

    // Set up add event button
    private void setupAddButtonListener() {
        addEventButton.setOnClickListener(v -> openAddEventDialog(eventTable));
    }

    // Clears login session and returns to login screen
    private void setupLogoutButton() {
        logoutButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("EventDaddyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.remove("userId");
            editor.apply();

            // Redirect to login
            Intent intent = new Intent(EventGridActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Fetch events from DB, sort them, and display
    private void loadEventsFromDatabase(TableLayout tableLayout) {
        tableLayout.removeAllViews(); // Clear any old rows

        List<Event> eventList = dbHelper.getUserEventsList(userId);

        // Sort events by date + time ascending
        eventList.sort((e1, e2) -> {
            String dateTime1 = e1.getDate() + " " + e1.getTime();
            String dateTime2 = e2.getDate() + " " + e2.getTime();
            return dateTime1.compareTo(dateTime2);
        });

        for (Event event : eventList) {
            addEventRow(tableLayout, event.getName(), event.getDate(), event.getId());
        }
    }

    // Displays a dialog box to enter event name and date
    private void openAddEventDialog(TableLayout tableLayout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Event");

        LinearLayout dialogLayout = buildDialogLayout();  // Create layout
        EditText eventNameInput = (EditText) dialogLayout.getChildAt(0);
        EditText eventDateInput = (EditText) dialogLayout.getChildAt(1);

        builder.setView(dialogLayout);

        // Handle Add button
        builder.setPositiveButton("Add", (dialog, which) -> {
            String eventName = eventNameInput.getText().toString().trim();
            String eventDate = eventDateInput.getText().toString().trim();

            if (!eventName.isEmpty() && !eventDate.isEmpty()) {
                if (dbHelper.addEvent(eventName, eventDate, "10:00 AM", userId)) {
                    loadEventsFromDatabase(tableLayout);  // Refresh
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Creates vertical layout for the Add Event dialog
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

    // Adds a single row to the event table
    private void addEventRow(TableLayout tableLayout, String eventNameText, String eventDateText, int eventId) {
        TableRow newRow = new TableRow(this);

        // Event name column
        TextView eventName = new TextView(this);
        eventName.setText(eventNameText);
        eventName.setPadding(16, 16, 16, 16);
        eventName.setMaxLines(2);
        eventName.setEllipsize(android.text.TextUtils.TruncateAt.END);
        eventName.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

        // Event date column
        TextView eventDate = new TextView(this);
        eventDate.setText(eventDateText);
        eventDate.setPadding(16, 16, 16, 16);
        eventDate.setMaxLines(1);
        eventDate.setEllipsize(android.text.TextUtils.TruncateAt.END);
        eventDate.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

        // Delete button
        Button deleteButton = new Button(this);
        deleteButton.setText("DELETE");
        deleteButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        deleteButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.custom_red));
        deleteButton.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        deleteButton.setOnClickListener(v -> {
            if (dbHelper.deleteEvent(eventId)) {
                tableLayout.removeView(newRow);  // Remove row from UI
            }
        });

        // Add columns to row
        newRow.addView(eventName);
        newRow.addView(eventDate);
        newRow.addView(deleteButton);

        // Add row to table layout
        tableLayout.addView(newRow);
    }

    // Filters events based on user input in search field
    private void filterAndDisplayEvents(String query) {
        eventTable.removeAllViews();  // Clear table

        List<Event> eventList = dbHelper.getUserEventsList(userId);

        for (Event event : eventList) {
            String name = event.getName().toLowerCase();
            String date = event.getDate().toLowerCase();

            if (name.contains(query.toLowerCase()) || date.contains(query.toLowerCase())) {
                addEventRow(eventTable, event.getName(), event.getDate(), event.getId());
            }
        }
    }
}
