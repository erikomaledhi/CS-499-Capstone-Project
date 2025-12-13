package com.example.weighttrackingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // UI Components
    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin, btnCreateAccount;
    private View loadingOverlay;

    // Database Helper
    private DatabaseHelper databaseHelper;

    // SharedPreferences for storing login state
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "WeightTrackerPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d(TAG, "LoginActivity onCreate started");

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize UI components
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        Log.d(TAG, "LoginActivity onCreate completed");
    }

    private void initializeViews() {
        // Text Input Layouts
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);

        // Text Input EditTexts
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        // Buttons
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // Loading overlay
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        btnCreateAccount.setOnClickListener(v -> handleCreateAccount());
    }

    private void handleLogin() {
        // Clear any previous errors
        clearErrors();

        // Get input values
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(username, password)) {
            return;
        }

        // Show loading
        showLoading(true);

        // Check if user exists and password is correct
        if (databaseHelper.validateUser(username, password)) {
            // Login successful
            saveLoginState(username);
            showToast("Login successful!");
            Log.d(TAG, "Login successful for user: " + username);
            navigateToMainActivity();
        } else {
            // Login failed
            showLoading(false);
            showToast("Invalid username or password");
            tilPassword.setError("Please check your credentials");
            Log.d(TAG, "Login failed for user: " + username);
        }
    }

    private void handleCreateAccount() {
        // Clear any previous errors
        clearErrors();

        // Get input values
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(username, password)) {
            return;
        }

        // Show loading
        showLoading(true);

        // Check if username already exists
        if (databaseHelper.userExists(username)) {
            // Username already exists
            showLoading(false);
            showToast("Username already exists");
            tilUsername.setError("This username is already taken");
            Log.d(TAG, "Account creation failed - username exists: " + username);
        } else {
            // Create new user
            long userId = databaseHelper.createUser(username, password);
            if (userId != -1) {
                // Account created successfully
                saveLoginState(username);
                showToast("Account created successfully!");
                Log.d(TAG, "Account created successfully for user: " + username);
                navigateToMainActivity();
            } else {
                // Account creation failed
                showLoading(false);
                showToast("Failed to create account. Please try again.");
                Log.e(TAG, "Failed to create account for user: " + username);
            }
        }
    }

    private boolean validateInputs(String username, String password) {
        boolean isValid = true;

        // Validate username
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username is required");
            isValid = false;
        } else if (username.length() < 3) {
            tilUsername.setError("Username must be at least 3 characters");
            isValid = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        tilUsername.setError(null);
        tilPassword.setError(null);
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnCreateAccount.setEnabled(!show);
    }

    private void saveLoginState(String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
        Log.d(TAG, "Login state saved for user: " + username);
    }

    private boolean isUserLoggedIn() {
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        boolean hasValidSession = isLoggedIn && !username.isEmpty();

        Log.d(TAG, "User login check - isLoggedIn: " + isLoggedIn + ", hasUsername: " + !username.isEmpty());
        return hasValidSession;
    }

    public static String getLoggedInUsername(SharedPreferences prefs) {
        return prefs.getString(KEY_USERNAME, "");
    }

    public static void logout(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_USERNAME);
        editor.apply();
    }

    private void navigateToMainActivity() {
        try {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Log.d(TAG, "Navigated to Main Activity");
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to main activity: ", e);
            showToast("Error opening main application");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Realm handles connection lifecycle automatically - no close needed
    }
}