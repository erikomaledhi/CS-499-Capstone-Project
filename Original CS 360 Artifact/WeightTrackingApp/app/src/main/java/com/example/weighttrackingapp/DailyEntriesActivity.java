package com.example.weighttrackingapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DailyEntriesActivity extends AppCompatActivity {

    private static final String TAG = "DailyEntriesActivity";
    private static final String PREFS_NAME = "WeightTrackerPrefs";

    // UI Components
    private ImageButton btnBack;
    private TextView tvTotalEntries, tvWeightLost, tvStreak;
    private RecyclerView recyclerViewAllEntries;
    private FloatingActionButton fabAddEntry;

    // Data
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private String currentUsername;
    private DailyEntryAdapter entryAdapter;
    private List<WeightEntry> allEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_entries);

        Log.d(TAG, "DailyEntriesActivity onCreate started");

        try {
            // Initialize data
            initializeData();

            // Initialize UI components
            initializeViews();

            // Set up click listeners
            setupClickListeners();

            // Set up RecyclerView
            setupRecyclerView();

            // Load all entries
            loadAllEntries();

            // Update statistics
            updateStatistics();

            Log.d(TAG, "DailyEntriesActivity onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            showToast("Error loading entries: " + e.getMessage());
            finish();
        }
    }

    private void initializeData() {
        try {
            databaseHelper = new DatabaseHelper(this);
            sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            currentUsername = sharedPreferences.getString("username", "");

            if (currentUsername.isEmpty()) {
                throw new RuntimeException("No logged in user found");
            }

            Log.d(TAG, "Data initialized for user: " + currentUsername);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing data: ", e);
            throw e;
        }
    }

    private void initializeViews() {
        try {
            // Header
            btnBack = findViewById(R.id.btnBack);

            // Statistics
            tvTotalEntries = findViewById(R.id.tvTotalEntries);

            // RecyclerView
            recyclerViewAllEntries = findViewById(R.id.recyclerViewAllEntries);

            // FAB
            fabAddEntry = findViewById(R.id.fabAddEntry);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: ", e);
            throw new RuntimeException("Failed to initialize views. Check layout file.", e);
        }
    }

    private void setupClickListeners() {
        try {
            // Back button
            btnBack.setOnClickListener(v -> {
                Log.d(TAG, "Back button clicked");
                finish();
            });

            // Add entry FAB
            fabAddEntry.setOnClickListener(v -> {
                Log.d(TAG, "Add entry FAB clicked");
                showAddWeightDialog();
            });

            Log.d(TAG, "Click listeners set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: ", e);
        }
    }

    private void setupRecyclerView() {
        try {
            allEntries = new ArrayList<>();
            entryAdapter = new DailyEntryAdapter(this, allEntries);

            // Set delete click listener
            entryAdapter.setOnDeleteClickListener(position -> {
                if (position >= 0 && position < allEntries.size()) {
                    WeightEntry entry = allEntries.get(position);
                    deleteWeightEntry(entry, position);
                }
            });

            recyclerViewAllEntries.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewAllEntries.setAdapter(entryAdapter);

            Log.d(TAG, "RecyclerView set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: ", e);
        }
    }

    private void loadAllEntries() {
        try {
            Log.d(TAG, "Loading all entries for user: " + currentUsername);

            // Get all weight entries from database
            List<WeightEntry> entries = databaseHelper.getAllWeightEntries(currentUsername);

            Log.d(TAG, "Loaded " + entries.size() + " entries from database");

            allEntries.clear();
            allEntries.addAll(entries);

            if (entryAdapter != null) {
                entryAdapter.notifyDataSetChanged();
            }

            // If no entries, add some sample data for demonstration
            if (allEntries.isEmpty()) {
                addSampleEntries();
            }

            Log.d(TAG, "All entries loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error loading all entries: ", e);
            showToast("Error loading entries");

            // Add sample data as fallback
            addSampleEntries();
        }
    }

    private void addSampleEntries() {
        try {
            allEntries.clear();

            // Sample entries for demonstration
            allEntries.add(new WeightEntry(1, 1, 75.2, "Today, August 16", "9:30 AM", ""));
            allEntries.add(new WeightEntry(2, 1, 75.5, "Yesterday", "9:15 AM", ""));
            allEntries.add(new WeightEntry(3, 1, 75.8, "August 14", "9:45 AM", ""));
            allEntries.add(new WeightEntry(4, 1, 76.1, "August 13", "9:20 AM", ""));
            allEntries.add(new WeightEntry(5, 1, 76.3, "August 12", "9:10 AM", ""));
            allEntries.add(new WeightEntry(6, 1, 76.5, "August 11", "9:35 AM", ""));
            allEntries.add(new WeightEntry(7, 1, 76.8, "August 10", "9:25 AM", ""));
            allEntries.add(new WeightEntry(8, 1, 77.0, "August 9", "9:40 AM", ""));
            allEntries.add(new WeightEntry(9, 1, 77.3, "August 8", "9:15 AM", ""));
            allEntries.add(new WeightEntry(10, 1, 77.5, "August 7", "9:30 AM", ""));

            if (entryAdapter != null) {
                entryAdapter.notifyDataSetChanged();
            }

            Log.d(TAG, "Sample entries added");
        } catch (Exception e) {
            Log.e(TAG, "Error adding sample entries: ", e);
        }
    }

    private void updateStatistics() {
        try {
            // Total entries
            int totalEntries = allEntries.size();
            tvTotalEntries.setText(String.valueOf(totalEntries));

            Log.d(TAG, "Statistics updated - Total entries: " + totalEntries);
        } catch (Exception e) {
            Log.e(TAG, "Error updating statistics: ", e);

            // Set default values
            tvTotalEntries.setText("0");
        }
    }

    private void deleteWeightEntry(WeightEntry entry, int position) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Entry")
                    .setMessage("Are you sure you want to delete this weight entry?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        try {
                            boolean deleted = databaseHelper.deleteWeightEntry(entry.getId());
                            if (deleted) {
                                allEntries.remove(position);
                                entryAdapter.notifyItemRemoved(position);

                                // Update statistics
                                updateStatistics();

                                // Update current weight if this was the most recent entry
                                updateCurrentWeightIfNeeded();

                                showToast("Entry deleted");
                                Log.d(TAG, "Entry deleted successfully");
                            } else {
                                showToast("Failed to delete entry");
                                Log.e(TAG, "Failed to delete entry from database");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error deleting entry: ", e);
                            showToast("Error deleting entry");
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing delete dialog: ", e);
        }
    }

    private void updateCurrentWeightIfNeeded() {
        try {
            if (!allEntries.isEmpty()) {
                // Update current weight to the most recent entry
                WeightEntry mostRecent = allEntries.get(0); // Entries are sorted by date DESC
                databaseHelper.updateCurrentWeight(currentUsername, mostRecent.getWeight());
                Log.d(TAG, "Current weight updated to: " + mostRecent.getWeight());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating current weight: ", e);
        }
    }

    private void showAddWeightDialog() {
        try {
            AddWeightDialog dialog = new AddWeightDialog(this, currentUsername, databaseHelper);
            dialog.setOnEntryAddedListener(() -> {
                // Refresh entries when new entry is added
                loadAllEntries();
                updateStatistics();
            });
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing add weight dialog: ", e);
            showToast("Error opening add entry dialog");
        }
    }

    private void showToast(String message) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast: ", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "onResume called");
            // Refresh data when returning to this activity
            loadAllEntries();
            updateStatistics();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: ", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (databaseHelper != null) {
                databaseHelper.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: ", e);
        }
    }
}