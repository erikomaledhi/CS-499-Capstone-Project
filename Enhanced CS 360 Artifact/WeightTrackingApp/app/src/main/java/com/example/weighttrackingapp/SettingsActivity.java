package com.example.weighttrackingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final String PREFS_NAME = "WeightTrackerPrefs";
    private static final String SETTINGS_PREFS = "SettingsPrefs";

    // UI Components
    private ImageButton btnBack;
    private View layoutCurrentWeight, layoutGoalWeight;
    private TextView tvCurrentWeightValue, tvGoalWeightValue;
    private MaterialSwitch switchPushNotifications, switchSMSNotifications;
    private View layoutNotificationTime;
    private TextView tvReminderTime;
    private View layoutLogout, layoutDeleteAccount;

    // Data
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences, settingsPreferences;
    private String currentUsername;
    private NotificationHelper notificationHelper;
    private ReminderScheduler reminderScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Log.d(TAG, "SettingsActivity onCreate started");

        try {
            // Initialize data
            initializeData();

            // Initialize UI components
            initializeViews();

            // Set up click listeners
            setupClickListeners();

            // Load current settings
            loadCurrentSettings();

            Log.d(TAG, "SettingsActivity onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            showToast("Error loading settings: " + e.getMessage());
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

            // Use user-specific settings preferences
            settingsPreferences = getSharedPreferences("SettingsPrefs_" + currentUsername, MODE_PRIVATE);

            notificationHelper = new NotificationHelper(this);
            reminderScheduler = new ReminderScheduler(this);

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

            // Personal Settings
            layoutCurrentWeight = findViewById(R.id.layoutCurrentWeight);
            layoutGoalWeight = findViewById(R.id.layoutGoalWeight);
            tvCurrentWeightValue = findViewById(R.id.tvCurrentWeightValue);
            tvGoalWeightValue = findViewById(R.id.tvGoalWeightValue);

            // Notifications
            switchPushNotifications = findViewById(R.id.switchPushNotifications);
            switchSMSNotifications = findViewById(R.id.switchSMSNotifications);
            layoutNotificationTime = findViewById(R.id.layoutNotificationTime);
            tvReminderTime = findViewById(R.id.tvReminderTime);

            // Account
            layoutLogout = findViewById(R.id.layoutLogout);
            layoutDeleteAccount = findViewById(R.id.layoutDeleteAccount);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: ", e);
            throw new RuntimeException("Failed to initialize views. Check layout file.", e);
        }
    }

    private void setupClickListeners() {
        try {
            // Header
            btnBack.setOnClickListener(v -> {
                Log.d(TAG, "Back button clicked");
                finish();
            });

            // Personal Settings
            layoutCurrentWeight.setOnClickListener(v -> showEditCurrentWeight());
            layoutGoalWeight.setOnClickListener(v -> showEditGoalWeight());

            // Notification Time
            layoutNotificationTime.setOnClickListener(v -> showReminderTimePicker());

            // Account Actions
            layoutLogout.setOnClickListener(v -> showLogoutConfirmation());
            layoutDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());

            Log.d(TAG, "Click listeners set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: ", e);
        }
    }

    private void loadCurrentSettings() {
        try {
            // Load weight values
            double currentWeight = databaseHelper.getCurrentWeight(currentUsername);
            double goalWeight = databaseHelper.getGoalWeight(currentUsername);

            // Safe setText with null checks
            if (tvCurrentWeightValue != null) {
                tvCurrentWeightValue.setText(String.format("%.1f kg", currentWeight));
            }
            if (tvGoalWeightValue != null) {
                tvGoalWeightValue.setText(String.format("%.1f kg", goalWeight));
            }

            // Load notification settings
            boolean pushPermissionGranted = settingsPreferences.getBoolean("push_notifications_permission", false);
            boolean pushEnabled = settingsPreferences.getBoolean("push_notifications", true);
            boolean smsEnabled = settingsPreferences.getBoolean("sms_notifications", false);

            // Set switches WITHOUT triggering listeners
            if (switchPushNotifications != null) {
                switchPushNotifications.setOnCheckedChangeListener(null); // Remove listener temporarily
                switchPushNotifications.setChecked(pushPermissionGranted && pushEnabled);
                // Re-add listener after setting value
                switchPushNotifications.setOnCheckedChangeListener((button, isChecked) -> {
                    handlePushNotificationToggle(isChecked);
                });
            }
            if (switchSMSNotifications != null) {
                switchSMSNotifications.setOnCheckedChangeListener(null); // Remove listener temporarily
                switchSMSNotifications.setChecked(smsEnabled);
                // Re-add listener after setting value
                switchSMSNotifications.setOnCheckedChangeListener((button, isChecked) -> {
                    saveSMSNotificationSetting(isChecked);
                    updateNotificationTimeVisibility();
                });
            }

            // Load reminder time
            String reminderTime = settingsPreferences.getString("reminder_time", "9:00 AM");
            if (tvReminderTime != null) {
                tvReminderTime.setText(reminderTime);
            }

            // Update notification time visibility
            updateNotificationTimeVisibility();

            Log.d(TAG, "Settings loaded successfully. Push permission: " + pushPermissionGranted + ", Push enabled: " + pushEnabled);
        } catch (Exception e) {
            Log.e(TAG, "Error loading settings: ", e);

            // Set default values with null checks
            if (tvCurrentWeightValue != null) {
                tvCurrentWeightValue.setText("0.0 kg");
            }
            if (tvGoalWeightValue != null) {
                tvGoalWeightValue.setText("0.0 kg");
            }
            if (tvReminderTime != null) {
                tvReminderTime.setText("9:00 AM");
            }
        }
    }

    private void updateNotificationTimeVisibility() {
        try {
            boolean anyNotificationEnabled = (switchPushNotifications != null && switchPushNotifications.isChecked()) ||
                    (switchSMSNotifications != null && switchSMSNotifications.isChecked());

            if (layoutNotificationTime != null) {
                layoutNotificationTime.setVisibility(anyNotificationEnabled ? View.VISIBLE : View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating notification time visibility: ", e);
        }
    }

    private void handlePushNotificationToggle(boolean isChecked) {
        try {
            boolean pushPermissionGranted = settingsPreferences.getBoolean("push_notifications_permission", false);

            Log.d(TAG, "Push notification toggle - Checked: " + isChecked + ", Permission granted: " + pushPermissionGranted);

            if (isChecked) {
                if (!pushPermissionGranted) {
                    // User wants to enable but doesn't have permission
                    if (switchPushNotifications != null) {
                        switchPushNotifications.setChecked(false); // Reset switch
                    }
                    showPushPermissionRequiredDialog();
                } else {
                    // User has permission and wants to enable
                    savePushNotificationSetting(true);
                    updateNotificationTimeVisibility();
                    showToast("Push notifications enabled");

                    // Schedule daily reminder
                    reminderScheduler.scheduleDailyReminder();

                    // Send a test notification to show it's working
                    scheduleTestNotification();
                }
            } else {
                // User wants to disable
                savePushNotificationSetting(false);
                updateNotificationTimeVisibility();
                showToast("Push notifications disabled");

                // Cancel daily reminder
                reminderScheduler.cancelDailyReminder();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling push notification toggle: ", e);
            if (switchPushNotifications != null) {
                switchPushNotifications.setChecked(false); // Reset to safe state
            }
        }
    }

    private void showPushPermissionRequiredDialog() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("To enable push notifications, you need to grant notification permission first.")
                    .setPositiveButton("Grant Permission", (dialog, which) -> {
                        // Navigate to push permission activity
                        Intent intent = new Intent(SettingsActivity.this, PushPermissionActivity.class);
                        intent.putExtra("from_settings", true);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing push permission required dialog: ", e);
        }
    }

    private void scheduleTestNotification() {
        try {
            // Show a daily reminder as a test instead of motivational message
            if (notificationHelper != null) {
                findViewById(android.R.id.content).postDelayed(() -> {
                    notificationHelper.showDailyReminder();
                }, 2000); // 2 second delay
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling test notification: ", e);
        }
    }

    private void showEditCurrentWeight() {
        try {
            EditWeightDialog dialog = new EditWeightDialog(this, currentUsername, databaseHelper);
            dialog.setOnWeightUpdatedListener(() -> {
                // Refresh weight values when updated
                loadCurrentSettings();
            });
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing edit current weight dialog: ", e);
            showToast("Error opening edit dialog");
        }
    }

    private void showEditGoalWeight() {
        try {
            EditWeightDialog dialog = new EditWeightDialog(this, currentUsername, databaseHelper);
            dialog.setOnWeightUpdatedListener(() -> {
                // Refresh weight values when updated
                loadCurrentSettings();
            });
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing edit goal weight dialog: ", e);
            showToast("Error opening edit dialog");
        }
    }

    private void savePushNotificationSetting(boolean enabled) {
        try {
            SharedPreferences.Editor editor = settingsPreferences.edit();
            editor.putBoolean("push_notifications", enabled);
            editor.apply();

            Log.d(TAG, "Push notification setting saved: " + enabled);
        } catch (Exception e) {
            Log.e(TAG, "Error saving push notification setting: ", e);
        }
    }

    private void saveSMSNotificationSetting(boolean enabled) {
        try {
            SharedPreferences.Editor editor = settingsPreferences.edit();
            editor.putBoolean("sms_notifications", enabled);
            editor.apply();

            String message = enabled ? "SMS notifications enabled" : "SMS notifications disabled";
            showToast(message);

            Log.d(TAG, "SMS notification setting saved: " + enabled);
        } catch (Exception e) {
            Log.e(TAG, "Error saving SMS notification setting: ", e);
        }
    }

    private void showReminderTimePicker() {
        try {
            // Get current time setting
            String currentTime = settingsPreferences.getString("reminder_time", "9:00 AM");

            // Parse current time
            int hour = 9;
            int minute = 0;

            try {
                if (currentTime.contains(":")) {
                    String[] parts = currentTime.replace(" AM", "").replace(" PM", "").split(":");
                    hour = Integer.parseInt(parts[0]);
                    minute = Integer.parseInt(parts[1]);

                    if (currentTime.contains("PM") && hour != 12) {
                        hour += 12;
                    } else if (currentTime.contains("AM") && hour == 12) {
                        hour = 0;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing current time: ", e);
            }

            android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                    this,
                    (view, hourOfDay, minuteOfHour) -> {
                        // Format time to 12-hour format
                        String ampm = hourOfDay >= 12 ? "PM" : "AM";
                        int displayHour = hourOfDay;
                        if (hourOfDay == 0) {
                            displayHour = 12;
                        } else if (hourOfDay > 12) {
                            displayHour = hourOfDay - 12;
                        }

                        String formattedTime = String.format("%d:%02d %s", displayHour, minuteOfHour, ampm);

                        // Save the time
                        SharedPreferences.Editor editor = settingsPreferences.edit();
                        editor.putString("reminder_time", formattedTime);
                        editor.apply();

                        // Update display
                        if (tvReminderTime != null) {
                            tvReminderTime.setText(formattedTime);
                        }

                        showToast("Reminder time updated");

                        // Reschedule reminder with new time
                        reminderScheduler.rescheduleReminder();

                        Log.d(TAG, "Reminder time saved: " + formattedTime);
                    },
                    hour,
                    minute,
                    false // 12-hour format
            );

            timePickerDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing reminder time picker: ", e);
            showToast("Error opening time picker");
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
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                        Log.d(TAG, "User logged out successfully");
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing logout confirmation: ", e);
        }
    }

    private void showDeleteAccountConfirmation() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.\n\nAll your weight data and settings will be permanently lost.")
                    .setPositiveButton("Delete Account", (dialog, which) -> {
                        // Show final confirmation
                        showFinalDeleteConfirmation();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing delete account confirmation: ", e);
        }
    }

    private void showFinalDeleteConfirmation() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Final Confirmation")
                    .setMessage("This is your final warning. Deleting your account will:\n\n• Permanently delete all your weight entries\n• Remove all your personal data\n• Cannot be undone\n\nType 'DELETE' to confirm:")
                    .setPositiveButton("I'M SURE - DELETE", (dialog, which) -> {
                        deleteUserAccount();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing final delete confirmation: ", e);
        }
    }

    private void deleteUserAccount() {
        try {
            Log.d(TAG, "Starting account deletion for user: " + currentUsername);

            // Get user ID
            int userId = databaseHelper.getUserId(currentUsername);
            if (userId == -1) {
                showToast("Error: User not found");
                Log.e(TAG, "User not found when trying to delete: " + currentUsername);
                return;
            }

            // Delete all user's weight entries
            boolean entriesDeleted = databaseHelper.deleteAllUserEntries(userId);
            Log.d(TAG, "Weight entries deletion result: " + entriesDeleted);

            // Delete user account
            boolean userDeleted = databaseHelper.deleteUser(userId);
            Log.d(TAG, "User account deletion result: " + userDeleted);

            if (userDeleted) {
                // Clear all preferences
                clearAllUserPreferences();

                // Cancel any scheduled notifications
                reminderScheduler.cancelDailyReminder();

                showToast("Account deleted successfully");

                // Redirect to login
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

                Log.d(TAG, "Account deletion completed successfully");
            } else {
                showToast("Failed to delete account. Please try again.");
                Log.e(TAG, "Failed to delete user account from database");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error deleting user account: ", e);
            showToast("Error deleting account: " + e.getMessage());
        }
    }

    private void clearAllUserPreferences() {
        try {
            // Clear main preferences (login state)
            SharedPreferences.Editor mainEditor = sharedPreferences.edit();
            mainEditor.clear();
            mainEditor.apply();

            // Clear THIS user's entire settings preferences file
            SharedPreferences.Editor settingsEditor = settingsPreferences.edit();
            settingsEditor.clear();
            settingsEditor.apply();

            // Clear user-specific notification history
            SharedPreferences notificationPrefs = getSharedPreferences("NotificationHistory_" + currentUsername, MODE_PRIVATE);
            SharedPreferences.Editor notificationEditor = notificationPrefs.edit();
            notificationEditor.clear();
            notificationEditor.apply();

            Log.d(TAG, "All preferences cleared for user: " + currentUsername);
        } catch (Exception e) {
            Log.e(TAG, "Error clearing user preferences: ", e);
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
            // Refresh settings when returning to this activity
            loadCurrentSettings();
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