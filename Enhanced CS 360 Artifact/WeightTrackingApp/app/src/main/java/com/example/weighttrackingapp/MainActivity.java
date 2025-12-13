package com.example.weighttrackingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "WeightTrackerPrefs";

    // UI Components
    private ImageButton btnProfile, btnNotifications;
    private ImageButton btnEditGoal;
    private TextView tvCurrentWeightDisplay, tvStartingWeight, tvCurrentWeight, tvRemainingWeight, tvProgressPercentage;
    private ProgressBar circularProgressBar, progressBarHorizontal;
    private TextView tvViewAll;
    private RecyclerView recyclerViewEntries;
    private View layoutDashboard, layoutSettings;

    // Data
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private String currentUsername;
    private DailyEntryAdapter entryAdapter;
    private List<WeightEntry> weightEntries;

    // Notification Helper
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "MainActivity onCreate started");

        try {
            // Initialize database and preferences
            initializeData();

            // Check if user is logged in
            if (!isUserLoggedIn()) {
                redirectToLogin();
                return;
            }

            // Initialize notification helper
            notificationHelper = new NotificationHelper(this);

            // Initialize UI components
            initializeViews();

            // Set up click listeners
            setupClickListeners();

            // Set up RecyclerView
            setupRecyclerView();

            // Load and display data
            loadDashboardData();

            // Check for push notification permission
            checkPushNotificationPermission();

            Log.d(TAG, "MainActivity onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            showToast("Error initializing dashboard: " + e.getMessage());
        }
    }

    private void initializeData() {
        try {
            databaseHelper = new DatabaseHelper(this);
            sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            currentUsername = sharedPreferences.getString("username", "");

            Log.d(TAG, "Current username: " + currentUsername);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing data: ", e);
            throw e;
        }
    }

    private boolean isUserLoggedIn() {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        boolean hasUsername = currentUsername != null && !currentUsername.trim().isEmpty();

        Log.d(TAG, "Is logged in: " + isLoggedIn + ", Has username: " + hasUsername);
        return isLoggedIn && hasUsername;
    }

    private void initializeViews() {
        try {
            // Header buttons
            btnProfile = findViewById(R.id.btnProfile);
            btnNotifications = findViewById(R.id.btnNotifications);

            // Goal weight section
            btnEditGoal = findViewById(R.id.btnEditGoal);
            tvCurrentWeightDisplay = findViewById(R.id.tvGoalWeightDisplay);
            tvStartingWeight = findViewById(R.id.tvStartingWeight);
            tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
            tvRemainingWeight = findViewById(R.id.tvRemainingWeight);
            tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
            circularProgressBar = findViewById(R.id.circularProgressBar);
            progressBarHorizontal = findViewById(R.id.progressBarHorizontal);

            // Daily entries section
            tvViewAll = findViewById(R.id.tvViewAll);
            recyclerViewEntries = findViewById(R.id.recyclerViewEntries);

            // Bottom navigation
            layoutDashboard = findViewById(R.id.btnDashboard);
            layoutSettings = findViewById(R.id.btnSettings);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: ", e);
            throw new RuntimeException("Failed to initialize views. Check if layout file has all required IDs.", e);
        }
    }

    private void setupClickListeners() {
        try {
            // Header buttons
            btnProfile.setOnClickListener(v -> showProfilePopup());
            btnNotifications.setOnClickListener(v -> showNotificationsPopup());

            // Goal weight edit
            btnEditGoal.setOnClickListener(v -> showEditWeightOptions());

            // View all entries - Navigate to DailyEntriesActivity
            tvViewAll.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, DailyEntriesActivity.class);
                startActivity(intent);
            });

            // Bottom navigation
            layoutDashboard.setOnClickListener(v -> {
                showToast("Refreshing dashboard...");
                loadDashboardData();
            });

            // Add entry button
            View layoutAdd = findViewById(R.id.btnAdd);
            if (layoutAdd != null) {
                layoutAdd.setOnClickListener(v -> showAddWeightDialog());
            }

            // Settings button - Navigate to SettingsActivity
            layoutSettings.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            });

            Log.d(TAG, "Click listeners set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: ", e);
        }
    }

    private void setupRecyclerView() {
        try {
            weightEntries = new ArrayList<>();
            entryAdapter = new DailyEntryAdapter(this, weightEntries);

            // Set edit click listener
            entryAdapter.setOnEditClickListener(position -> {
                if (position >= 0 && position < weightEntries.size()) {
                    WeightEntry entry = weightEntries.get(position);
                    showEditEntryDialog(entry);
                }
            });

            // Set delete click listener
            entryAdapter.setOnDeleteClickListener(position -> {
                if (position >= 0 && position < weightEntries.size()) {
                    WeightEntry entry = weightEntries.get(position);
                    deleteWeightEntry(entry, position);
                }
            });

            recyclerViewEntries.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewEntries.setAdapter(entryAdapter);
            recyclerViewEntries.setNestedScrollingEnabled(false);

            Log.d(TAG, "RecyclerView set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: ", e);
        }
    }

    private void loadDashboardData() {
        try {
            Log.d(TAG, "Loading dashboard data for user: " + currentUsername);

            // Get user's weight data
            double startingWeight = databaseHelper.getStartingWeight(currentUsername);
            double currentWeight = databaseHelper.getCurrentWeight(currentUsername);
            double goalWeight = databaseHelper.getGoalWeight(currentUsername);

            Log.d(TAG, "Starting weight: " + startingWeight + ", Current weight: " + currentWeight + ", Goal weight: " + goalWeight);

            // Update goal weight display
            if (goalWeight > 0) {
                tvCurrentWeightDisplay.setText(String.format("%.1f", goalWeight));
            } else {
                tvCurrentWeightDisplay.setText("0.0");
            }

            // Update starting weight display
            if (startingWeight > 0) {
                tvStartingWeight.setText(String.format("%.1f kg", startingWeight));
            } else {
                tvStartingWeight.setText("-- kg");
            }

            // Update current weight display
            if (currentWeight > 0) {
                tvCurrentWeight.setText(String.format("%.1f kg", currentWeight));
            } else {
                tvCurrentWeight.setText("0.0 kg");
            }

            // Calculate remaining weight and progress
            updateProgressDisplays(startingWeight, currentWeight, goalWeight);

            // Load recent weight entries
            loadRecentEntries();

            // Check for goal achievements and milestones
            checkGoalAchievementsAndMilestones(startingWeight, currentWeight, goalWeight);

            Log.d(TAG, "Dashboard data loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error loading dashboard data: ", e);
            showToast("Error loading data: " + e.getMessage());

            // Set default values to prevent crashes
            setDefaultValues();
        }
    }

    private void setDefaultValues() {
        try {
            tvCurrentWeightDisplay.setText("0.0");
            tvStartingWeight.setText("-- kg");
            tvCurrentWeight.setText("0.0 kg");
            tvRemainingWeight.setText("Set your weights");
            tvProgressPercentage.setText("0% complete");
            circularProgressBar.setProgress(0);
            progressBarHorizontal.setProgress(0);
        } catch (Exception e) {
            Log.e(TAG, "Error setting default values: ", e);
        }
    }

    private void updateProgressDisplays(double startingWeight, double currentWeight, double goalWeight) {
        try {
            if (goalWeight <= 0 || currentWeight <= 0) {
                tvRemainingWeight.setText("Set your weights");
                tvProgressPercentage.setText("0% complete");
                circularProgressBar.setProgress(0);
                progressBarHorizontal.setProgress(0);
                return;
            }

            double remainingWeight = currentWeight - goalWeight;

            if (remainingWeight <= 0) {
                // Goal achieved!
                tvRemainingWeight.setText("Goal achieved!");
                tvRemainingWeight.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                tvProgressPercentage.setText("100% complete");
                tvProgressPercentage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                circularProgressBar.setProgress(100);
                progressBarHorizontal.setProgress(100);
            } else {
                if (startingWeight <= 0 || startingWeight <= goalWeight) {
                    // No starting weight set or invalid starting weight
                    tvRemainingWeight.setText(String.format("%.1f kg", remainingWeight));
                    tvRemainingWeight.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    tvProgressPercentage.setText("Set starting weight");
                    tvProgressPercentage.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    circularProgressBar.setProgress(0);
                    progressBarHorizontal.setProgress(0);
                } else {
                    // Calculate percentage based on starting weight
                    double totalWeightToLose = startingWeight - goalWeight;
                    double weightLost = startingWeight - currentWeight;

                    int progressPercentage = Math.max(0, Math.min(100, (int) ((weightLost / totalWeightToLose) * 100)));

                    tvRemainingWeight.setText(String.format("%.1f kg", remainingWeight));
                    tvRemainingWeight.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    tvProgressPercentage.setText(String.format("%d%% complete", progressPercentage));
                    tvProgressPercentage.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    circularProgressBar.setProgress(progressPercentage);
                    progressBarHorizontal.setProgress(progressPercentage);

                    Log.d(TAG, "Progress calculation - Starting: " + startingWeight + "kg, Current: " + currentWeight + "kg, Goal: " + goalWeight + "kg, Progress: " + progressPercentage + "%");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating progress displays: ", e);
        }
    }

    private void loadRecentEntries() {
        try {
            // Get recent weight entries from database
            List<WeightEntry> entries = databaseHelper.getRecentWeightEntries(currentUsername, 5);

            Log.d(TAG, "Loaded " + entries.size() + " recent entries");

            weightEntries.clear();
            weightEntries.addAll(entries);

            if (entryAdapter != null) {
                entryAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading recent entries: ", e);
            // Add some sample data for testing
            addSampleData();
        }
    }

    private void addSampleData() {
        try {
            // Add sample weight entries for testing
            weightEntries.clear();

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());
            String currentTime = timeFormat.format(new Date());

            // Sample entries
            weightEntries.add(new WeightEntry(1, 1, 75.2, "Today, " + currentDate, currentTime, ""));
            weightEntries.add(new WeightEntry(2, 1, 75.5, "Yesterday", "9:30 AM", ""));
            weightEntries.add(new WeightEntry(3, 1, 75.8, "2 days ago", "9:45 AM", ""));

            if (entryAdapter != null) {
                entryAdapter.notifyDataSetChanged();
            }

            Log.d(TAG, "Sample data added");
        } catch (Exception e) {
            Log.e(TAG, "Error adding sample data: ", e);
        }
    }

    //Check for push notification permission
    private void checkPushNotificationPermission() {
        try {
            // Use user-specific settings preferences
            SharedPreferences settingsPrefs = getSharedPreferences("SettingsPrefs_" + currentUsername, MODE_PRIVATE);

            // Create user-specific key for permission prompt
            String userPermissionKey = "has_asked_push_permission_" + currentUsername;
            boolean hasAskedForPermission = settingsPrefs.getBoolean(userPermissionKey, false);

            Log.d(TAG, "Push permission check for user " + currentUsername + " - Asked before: " + hasAskedForPermission);

            // Show permission request for each new user, regardless of device permission
            if (!hasAskedForPermission) {
                // Mark that we've asked this user for permission
                SharedPreferences.Editor editor = settingsPrefs.edit();
                editor.putBoolean(userPermissionKey, true);
                editor.apply();

                // Show permission request after a short delay to let the UI settle
                findViewById(android.R.id.content).postDelayed(() -> {
                    showPushPermissionDialog();
                }, 1000);

                Log.d(TAG, "Showing push permission dialog for new user: " + currentUsername);
            } else {
                Log.d(TAG, "User " + currentUsername + " has already been asked for push permission");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking push notification permission: ", e);
        }
    }

    //Show push notification permission dialog
    private void showPushPermissionDialog() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Enable Push Notifications")
                    .setMessage("Get instant alerts when you reach your weight goals and daily reminders to stay on track!")
                    .setPositiveButton("Enable", (dialog, which) -> {
                        // Navigate to push permission activity
                        Intent intent = new Intent(MainActivity.this, PushPermissionActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("Maybe Later", (dialog, which) -> {
                        Log.d(TAG, "User chose to skip push notifications for now");
                    })
                    .setCancelable(true)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing push permission dialog: ", e);
        }
    }

    //Check for goal achievements and milestones
    private void checkGoalAchievementsAndMilestones(double startingWeight, double currentWeight, double goalWeight) {
        try {
            if (notificationHelper != null) {
                // Check for goal achievement
                notificationHelper.checkAndNotifyGoalAchievement(currentUsername, databaseHelper);

                // Check for milestone achievements
                notificationHelper.checkAndNotifyMilestone(currentUsername, databaseHelper);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking achievements and milestones: ", e);
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
                                weightEntries.remove(position);
                                entryAdapter.notifyItemRemoved(position);
                                showToast("Entry deleted");

                                // Update current weight if needed
                                updateCurrentWeightIfNeeded();
                            } else {
                                showToast("Failed to delete entry");
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
            // Update current weight with the most recent entry by actual date/time
            databaseHelper.updateCurrentWeightFromMostRecent(currentUsername);
            loadDashboardData();
        } catch (Exception e) {
            Log.e(TAG, "Error updating current weight: ", e);
        }
    }

    private void showProfilePopup() {
        try {
            View popupView = LayoutInflater.from(this).inflate(R.layout.popup_profile_menu, null);
            PopupWindow popupWindow = new PopupWindow(popupView,
                    (int) (280 * getResources().getDisplayMetrics().density),
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);

            // Set username in popup
            TextView tvUsername = popupView.findViewById(R.id.tvUsername);
            if (tvUsername != null) {
                tvUsername.setText(currentUsername);
            }

            // Set click listeners
            View settingsLayout = popupView.findViewById(R.id.layoutSettings);
            if (settingsLayout != null) {
                settingsLayout.setOnClickListener(v -> {
                    popupWindow.dismiss();
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                });
            }

            View logoutLayout = popupView.findViewById(R.id.layoutLogout);
            if (logoutLayout != null) {
                logoutLayout.setOnClickListener(v -> {
                    popupWindow.dismiss();
                    showLogoutConfirmation();
                });
            }

            // Show popup
            popupWindow.showAsDropDown(btnProfile, -200, 10);
        } catch (Exception e) {
            Log.e(TAG, "Error showing profile popup: ", e);
            showToast("Error showing profile menu");
        }
    }

    private void showNotificationsPopup() {
        try {
            View popupView = LayoutInflater.from(this).inflate(R.layout.popup_notifications, null);
            PopupWindow popupWindow = new PopupWindow(popupView,
                    (int) (320 * getResources().getDisplayMetrics().density),
                    (int) (400 * getResources().getDisplayMetrics().density), true);

            // Set up close button
            View closeButton = popupView.findViewById(R.id.btnCloseNotifications);
            if (closeButton != null) {
                closeButton.setOnClickListener(v -> popupWindow.dismiss());
            }

            // Get notification history from user-specific preferences
            SharedPreferences notificationPrefs = getSharedPreferences("NotificationHistory_" + currentUsername, MODE_PRIVATE);
            String notificationHistory = notificationPrefs.getString("notifications", "[]");

            Log.d(TAG, "Notification history: " + notificationHistory);

            View emptyLayout = popupView.findViewById(R.id.layoutEmptyNotifications);
            RecyclerView recyclerView = popupView.findViewById(R.id.recyclerViewNotifications);

            try {
                org.json.JSONArray notifications = new org.json.JSONArray(notificationHistory);

                if (notifications.length() == 0) {
                    // Show empty notifications message
                    if (emptyLayout != null) {
                        emptyLayout.setVisibility(View.VISIBLE);
                    }
                    if (recyclerView != null) {
                        recyclerView.setVisibility(View.GONE);
                    }
                    Log.d(TAG, "No notifications found - showing empty state");
                } else {
                    // Show notifications list
                    if (emptyLayout != null) {
                        emptyLayout.setVisibility(View.GONE);
                    }
                    if (recyclerView != null) {
                        recyclerView.setVisibility(View.VISIBLE);

                        // Create simple text-based notification items for testing
                        android.widget.LinearLayout notificationContainer = new android.widget.LinearLayout(this);
                        notificationContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
                        notificationContainer.setPadding(16, 16, 16, 16);

                        for (int i = 0; i < notifications.length(); i++) {
                            org.json.JSONObject notification = notifications.getJSONObject(i);

                            android.widget.TextView notificationView = new android.widget.TextView(this);
                            String title = notification.getString("title");
                            String message = notification.getString("message");
                            String displayTime = notification.getString("displayTime");

                            notificationView.setText(title + "\n" + message + "\n" + displayTime);
                            notificationView.setPadding(0, 0, 0, 24);
                            notificationView.setTextSize(14);
                            notificationView.setTextColor(android.graphics.Color.BLACK);

                            notificationContainer.addView(notificationView);
                        }

                        // Clear RecyclerView and add our container
                        if (recyclerView.getParent() instanceof android.view.ViewGroup) {
                            android.view.ViewGroup parent = (android.view.ViewGroup) recyclerView.getParent();
                            parent.removeView(recyclerView);

                            android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
                            scrollView.addView(notificationContainer);
                            parent.addView(scrollView);
                        }
                    }
                    Log.d(TAG, "Showing " + notifications.length() + " notifications");
                }
            } catch (org.json.JSONException e) {
                Log.e(TAG, "Error parsing notification history JSON: ", e);
                // Show empty state on JSON error
                if (emptyLayout != null) {
                    emptyLayout.setVisibility(View.VISIBLE);
                }
                if (recyclerView != null) {
                    recyclerView.setVisibility(View.GONE);
                }
            }

            // Show popup
            popupWindow.showAsDropDown(btnNotifications, -250, 10);
        } catch (Exception e) {
            Log.e(TAG, "Error showing notifications popup: ", e);
            showToast("Error showing notifications");
        }
    }

    private void showEditWeightOptions() {
        try {
            EditWeightDialog dialog = new EditWeightDialog(this, currentUsername, databaseHelper);
            dialog.setOnWeightUpdatedListener(() -> {
                // Refresh dashboard when weights are updated
                loadDashboardData();
            });
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing edit weight dialog: ", e);
            showToast("Error opening edit dialog");
        }
    }

    private void showEditEntryDialog(WeightEntry entry) {
        try {
            EditEntryDialog dialog = new EditEntryDialog(this, entry, currentUsername, databaseHelper);
            dialog.setOnEntryUpdatedListener(() -> {
                // Refresh dashboard when entry is updated
                loadDashboardData();
            });
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing edit entry dialog: ", e);
            showToast("Error opening edit dialog");
        }
    }

    // Add entry functionality
    public void showAddWeightDialog() {
        try {
            AddWeightDialog dialog = new AddWeightDialog(this, currentUsername, databaseHelper);
            dialog.setOnEntryAddedListener(() -> {
                // Refresh dashboard when new entry is added
                loadDashboardData();
            });
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing add weight dialog: ", e);
            showToast("Error opening add entry dialog");
        }
    }

    private void showLogoutConfirmation() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        // Clear login state
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", false);
                        editor.remove("username");
                        editor.apply();

                        // Redirect to login
                        redirectToLogin();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing logout confirmation: ", e);
        }
    }

    private void redirectToLogin() {
        try {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error redirecting to login: ", e);
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
            loadDashboardData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: ", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Realm handles connection lifecycle automatically - no close needed
    }
}