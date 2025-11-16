package com.example.weighttrackingapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

public class PushPermissionActivity extends AppCompatActivity {

    private static final String TAG = "PushPermissionActivity";
    private static final String PREFS_NAME = "WeightTrackerPrefs";

    // UI Components
    private ImageButton btnBack;
    private MaterialButton btnEnablePush, btnSkip, btnTryAgain, btnContinue, btnContinueWithoutPush;
    private LinearLayout layoutSuccess, layoutDenied;
    private ScrollView layoutInitialRequest;

    // Data
    private SharedPreferences sharedPreferences, settingsPreferences;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private boolean isFromSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_permission);

        Log.d(TAG, "PushPermissionActivity onCreate started");

        try {
            // Initialize data
            initializeData();

            // Initialize UI components
            initializeViews();

            // Set up permission launcher
            setupPermissionLauncher();

            // Set up click listeners
            setupClickListeners();

            // Always start with the initial request layout
            showInitialRequestLayout();

            Log.d(TAG, "PushPermissionActivity onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            showToast("Error initializing permission screen: " + e.getMessage());
            finish();
        }
    }

    private void initializeData() {
        try {
            sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            // Get current username
            String currentUsername = sharedPreferences.getString("username", "");

            // Use user-specific settings preferences
            settingsPreferences = getSharedPreferences("SettingsPrefs_" + currentUsername, MODE_PRIVATE);

            // Check if this activity was opened from settings
            isFromSettings = getIntent().getBooleanExtra("from_settings", false);

            Log.d(TAG, "Data initialized. From settings: " + isFromSettings + ", User: " + currentUsername);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing data: ", e);
            throw e;
        }
    }

    private void initializeViews() {
        try {
            // Header
            btnBack = findViewById(R.id.btnBack);

            // Main layout containers
            layoutInitialRequest = findViewById(R.id.scrollView2); // The main content with enable/skip buttons
            layoutSuccess = findViewById(R.id.layoutSuccess);
            layoutDenied = findViewById(R.id.layoutDenied);

            // Action buttons from initial request
            btnEnablePush = findViewById(R.id.btnEnablePush);
            btnSkip = findViewById(R.id.btnSkip);

            // Success layout button
            btnContinue = findViewById(R.id.btnContinue);

            // Denied layout buttons
            btnTryAgain = findViewById(R.id.btnTryAgain);
            btnContinueWithoutPush = findViewById(R.id.btnContinueWithoutPush);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: ", e);
            throw new RuntimeException("Failed to initialize views. Check layout file.", e);
        }
    }

    private void setupPermissionLauncher() {
        try {
            requestPermissionLauncher = registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        Log.d(TAG, "Permission result received: " + isGranted);
                        handlePermissionResult(isGranted);
                    }
            );

            Log.d(TAG, "Permission launcher set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up permission launcher: ", e);
        }
    }

    private void setupClickListeners() {
        try {
            // Back button
            btnBack.setOnClickListener(v -> {
                Log.d(TAG, "Back button clicked");
                handleBackNavigation();
            });

            // Enable push notifications
            btnEnablePush.setOnClickListener(v -> {
                Log.d(TAG, "Enable push notifications button clicked");
                requestNotificationPermission();
            });

            // Skip button
            btnSkip.setOnClickListener(v -> {
                Log.d(TAG, "Skip button clicked");
                // User declined - show denied layout
                showDeniedLayout();
            });

            // Try again button
            btnTryAgain.setOnClickListener(v -> {
                Log.d(TAG, "Try again button clicked");
                showInitialRequestLayout();
            });

            // Continue button
            btnContinue.setOnClickListener(v -> {
                Log.d(TAG, "Continue button clicked");
                handleBackNavigation();
            });

            // Continue without push button
            btnContinueWithoutPush.setOnClickListener(v -> {
                Log.d(TAG, "Continue without push button clicked");
                savePushNotificationPreference(false);
                handleBackNavigation();
            });

            Log.d(TAG, "Click listeners set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: ", e);
        }
    }

    private void showInitialRequestLayout() {
        try {
            layoutInitialRequest.setVisibility(View.VISIBLE);
            layoutSuccess.setVisibility(View.GONE);
            layoutDenied.setVisibility(View.GONE);

            Log.d(TAG, "Showing initial request layout");
        } catch (Exception e) {
            Log.e(TAG, "Error showing initial request layout: ", e);
        }
    }

    private void showSuccessLayout() {
        try {
            // Show success layout
            layoutInitialRequest.setVisibility(View.GONE);
            layoutSuccess.setVisibility(View.VISIBLE);
            layoutDenied.setVisibility(View.GONE);

            Log.d(TAG, "Showing success layout");
        } catch (Exception e) {
            Log.e(TAG, "Error showing success layout: ", e);
        }
    }

    private void showDeniedLayout() {
        try {
            // Show denied layout
            layoutInitialRequest.setVisibility(View.GONE);
            layoutSuccess.setVisibility(View.GONE);
            layoutDenied.setVisibility(View.VISIBLE);

            Log.d(TAG, "Showing denied layout");
        } catch (Exception e) {
            Log.e(TAG, "Error showing denied layout: ", e);
        }
    }

    private void requestNotificationPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.d(TAG, "Requesting POST_NOTIFICATIONS permission");
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                Log.d(TAG, "Pre-Android 13 device - granting permission automatically");
                handlePermissionResult(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error requesting notification permission: ", e);
            showToast("Error requesting permission");
            handlePermissionResult(false);
        }
    }

    private void handlePermissionResult(boolean isGranted) {
        try {
            Log.d(TAG, "Handling permission result: " + isGranted);

            if (isGranted) {
                // Permission granted - show success layout
                savePushNotificationPreference(true);
                showSuccessLayout();
                showToast("Push notifications enabled!");

                // Schedule welcome notification
                scheduleWelcomeNotification();

            } else {
                // Permission denied - show denied layout
                savePushNotificationPreference(false);
                showDeniedLayout();
                showToast("Push notifications disabled");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling permission result: ", e);
            showDeniedLayout();
        }
    }

    private void savePushNotificationPreference(boolean enabled) {
        try {
            SharedPreferences.Editor editor = settingsPreferences.edit();
            editor.putBoolean("push_notifications_permission", enabled);
            editor.putBoolean("push_notifications", enabled); // Also save the settings preference
            editor.apply();

            Log.d(TAG, "Push notification preference saved: " + enabled);
        } catch (Exception e) {
            Log.e(TAG, "Error saving push notification preference: ", e);
        }
    }

    private void scheduleWelcomeNotification() {
        try {
            // Create a welcome notification to show that push notifications are working
            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.showWelcomeNotification();

            Log.d(TAG, "Welcome notification scheduled");
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling welcome notification: ", e);
        }
    }

    private void handleBackNavigation() {
        try {
            if (isFromSettings) {
                // If opened from settings, just finish this activity
                finish();
            } else {
                // If part of onboarding flow, navigate to main activity
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling back navigation: ", e);
            finish();
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
    public void onBackPressed() {
        Log.d(TAG, "Hardware back button pressed");
        handleBackNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "onResume called");
            // Don't check permission status on resume - keep the current layout
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: ", e);
        }
    }
}