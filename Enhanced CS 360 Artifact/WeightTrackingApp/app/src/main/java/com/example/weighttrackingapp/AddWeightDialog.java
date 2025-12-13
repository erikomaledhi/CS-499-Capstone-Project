package com.example.weighttrackingapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddWeightDialog extends Dialog {

    private static final String TAG = "AddWeightDialog";

    // UI Components
    private ImageButton btnCloseDialog;
    private TextInputLayout tilWeight, tilDate, tilTime;
    private TextInputEditText etWeight, etDate, etTime;
    private MaterialButton btnCancel, btnAddEntry;

    // Data
    private String username;
    private DatabaseHelper databaseHelper;
    private OnEntryAddedListener listener;
    private Calendar selectedDate;
    private Calendar selectedTime;

    //Notification helper
    private NotificationHelper notificationHelper;

    // Interface for callback
    public interface OnEntryAddedListener {
        void onEntryAdded();
    }

    public AddWeightDialog(@NonNull Context context, String username, DatabaseHelper databaseHelper) {
        super(context);
        this.username = username;
        this.databaseHelper = databaseHelper;
        this.selectedDate = Calendar.getInstance();
        this.selectedTime = Calendar.getInstance();
        this.notificationHelper = new NotificationHelper(context);
    }

    public void setOnEntryAddedListener(OnEntryAddedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_weight_entry);

        try {
            // Initialize views
            initializeViews();

            // Set up click listeners
            setupClickListeners();

            // Set default date and time
            setDefaultDateTime();

            Log.d(TAG, "AddWeightDialog created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating AddWeightDialog: ", e);
            showToast("Error opening add entry dialog");
            dismiss();
        }
    }

    private void initializeViews() {
        btnCloseDialog = findViewById(R.id.btnCloseDialog);
        tilWeight = findViewById(R.id.tilWeight);
        tilDate = findViewById(R.id.tilDate);
        tilTime = findViewById(R.id.tilTime);
        etWeight = findViewById(R.id.etWeight);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        btnCancel = findViewById(R.id.btnCancel);
        btnAddEntry = findViewById(R.id.btnAddEntry);
    }

    private void setupClickListeners() {
        btnCloseDialog.setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());
        btnAddEntry.setOnClickListener(v -> addWeightEntry());

        // Date picker
        etDate.setOnClickListener(v -> showDatePicker());
        tilDate.setEndIconOnClickListener(v -> showDatePicker());

        // Time picker
        etTime.setOnClickListener(v -> showTimePicker());
        tilTime.setEndIconOnClickListener(v -> showTimePicker());
    }

    private void setDefaultDateTime() {
        try {
            // Set current date and time as default
            updateDateDisplay();
            updateTimeDisplay();
        } catch (Exception e) {
            Log.e(TAG, "Error setting default date/time: ", e);
        }
    }

    private void showDatePicker() {
        try {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateDisplay();
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );

            // Don't allow future dates
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing date picker: ", e);
            showToast("Error opening date picker");
        }
    }

    private void showTimePicker() {
        try {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getContext(),
                    (view, hourOfDay, minute) -> {
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute);
                        updateTimeDisplay();
                    },
                    selectedTime.get(Calendar.HOUR_OF_DAY),
                    selectedTime.get(Calendar.MINUTE),
                    false // 12-hour format
            );
            timePickerDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing time picker: ", e);
            showToast("Error opening time picker");
        }
    }

    private void updateDateDisplay() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
            String formattedDate = dateFormat.format(selectedDate.getTime());

            // Check if it's today
            Calendar today = Calendar.getInstance();
            if (isSameDay(selectedDate, today)) {
                formattedDate = "Today, " + new SimpleDateFormat("MMMM d", Locale.getDefault()).format(selectedDate.getTime());
            } else {
                Calendar yesterday = Calendar.getInstance();
                yesterday.add(Calendar.DAY_OF_YEAR, -1);
                if (isSameDay(selectedDate, yesterday)) {
                    formattedDate = "Yesterday";
                }
            }

            etDate.setText(formattedDate);
        } catch (Exception e) {
            Log.e(TAG, "Error updating date display: ", e);
        }
    }

    private void updateTimeDisplay() {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            String formattedTime = timeFormat.format(selectedTime.getTime());
            etTime.setText(formattedTime);
        } catch (Exception e) {
            Log.e(TAG, "Error updating time display: ", e);
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void addWeightEntry() {
        try {
            Log.d(TAG, "Starting to add weight entry for user: " + username);

            // Clear any previous errors
            tilWeight.setError(null);

            String weightStr = etWeight.getText().toString().trim();
            String dateStr = etDate.getText().toString().trim();
            String timeStr = etTime.getText().toString().trim();

            Log.d(TAG, "Input values - Weight: " + weightStr + ", Date: " + dateStr + ", Time: " + timeStr);

            // Validate inputs
            if (!validateInputs(weightStr, dateStr, timeStr)) {
                Log.e(TAG, "Input validation failed");
                return;
            }

            double weight = Double.parseDouble(weightStr);
            Log.d(TAG, "Parsed weight: " + weight);

            // Validate weight value (this may show confirmation dialog for large changes)
            ValidationResult validationResult = validateWeightValue(weight);
            if (validationResult == ValidationResult.INVALID) {
                Log.e(TAG, "Weight value validation failed - invalid range");
                return;
            } else if (validationResult == ValidationResult.PENDING_CONFIRMATION) {
                Log.d(TAG, "Weight validation pending user confirmation for large change");
                return; // Wait for user confirmation via dialog
            }
            // If validationResult == ValidationResult.VALID, continue with normal flow

            // Get user ID
            int userId = databaseHelper.getUserId(username);
            Log.d(TAG, "Retrieved user ID: " + userId);

            if (userId == -1) {
                Log.e(TAG, "User not found in database: " + username);
                showToast("Error: User not found");
                return;
            }

            // Get previous weights for comparison
            double previousCurrentWeight = databaseHelper.getCurrentWeight(username);
            double goalWeight = databaseHelper.getGoalWeight(username);
            double startingWeight = databaseHelper.getStartingWeight(username);

            Log.d(TAG, "Previous weights - Current: " + previousCurrentWeight + ", Goal: " + goalWeight + ", Starting: " + startingWeight);

            // Create standardized date string for database storage
            SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dbDateString = dbDateFormat.format(selectedDate.getTime());

            // Create standardized time string for database storage
            SimpleDateFormat dbTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String dbTimeString = dbTimeFormat.format(selectedTime.getTime());

            Log.d(TAG, "Database date format: " + dbDateString + ", Database time format: " + dbTimeString);

            // Create weight entry with database-friendly formats
            WeightEntry newEntry = new WeightEntry(userId, weight, dbDateString, dbTimeString, "");
            Log.d(TAG, "Created WeightEntry: " + newEntry.toString());

            // Add to database
            long entryId = databaseHelper.addWeightEntry(newEntry);
            Log.d(TAG, "Database insertion result - Entry ID: " + entryId);

            if (entryId != -1) {
                // Update current weight with the most recent entry by actual date/time
                boolean weightUpdated = databaseHelper.updateCurrentWeightFromMostRecent(username);
                Log.d(TAG, "Current weight update result: " + weightUpdated);

                // Check for achievements and send notifications
                checkAndSendNotifications(weight, previousCurrentWeight, goalWeight, startingWeight);

                showToast("Weight entry added successfully!");

                // Notify listener
                if (listener != null) {
                    listener.onEntryAdded();
                }

                dismiss();
                Log.d(TAG, "Weight entry added successfully with ID: " + entryId);
            } else {
                showToast("Failed to add weight entry. Please try again.");
                Log.e(TAG, "Failed to add weight entry to database - returned -1");
            }

        } catch (NumberFormatException e) {
            Log.e(TAG, "Number format error: ", e);
            showToast("Please enter a valid weight value");
        } catch (Exception e) {
            Log.e(TAG, "Error adding weight entry: ", e);
            showToast("Error adding weight entry: " + e.getMessage());
        }
    }

    //Check for achievements and send appropriate notifications
    private void checkAndSendNotifications(double newWeight, double previousWeight, double goalWeight, double startingWeight) {
        try {
            Log.d(TAG, "Checking for achievements - New: " + newWeight + ", Previous: " + previousWeight + ", Goal: " + goalWeight + ", Starting: " + startingWeight);

            // Check for goal achievement
            if (goalWeight > 0 && newWeight <= goalWeight && previousWeight > goalWeight) {
                // User just achieved their goal!
                Log.d(TAG, "Goal achievement detected!");
                notificationHelper.showGoalAchievedNotification(goalWeight);
            }

            // Check for milestone achievements (weight loss milestones)
            if (startingWeight > 0 && newWeight < previousWeight) {
                double previousWeightLost = startingWeight - previousWeight;
                double newWeightLost = startingWeight - newWeight;

                // Check if we crossed a 5kg milestone
                int previousMilestone = (int) (previousWeightLost / 5) * 5;
                int newMilestone = (int) (newWeightLost / 5) * 5;

                if (newMilestone > previousMilestone && newMilestone >= 5) {
                    // User crossed a new milestone
                    Log.d(TAG, "Milestone achievement detected: " + newMilestone + "kg lost");
                    notificationHelper.showMilestoneNotification(newMilestone, startingWeight, newWeight);
                }
            }

            // Check for weight loss progress (any weight loss)
            if (newWeight < previousWeight && previousWeight > 0) {
                double weightLost = previousWeight - newWeight;
                if (weightLost >= 0.5) { // At least 0.5kg lost
                    Log.d(TAG, "Weight loss progress detected: " + weightLost + "kg");
                }
            }

            // Send encouraging notification for consistent tracking
            if (shouldSendEncouragementNotification()) {
                Log.d(TAG, "Sending daily reminder for consistent tracking");
                notificationHelper.showDailyReminder();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error checking achievements and sending notifications: ", e);
        }
    }

    //Determine if we should send an encouragement notification
    private boolean shouldSendEncouragementNotification() {
        try {
            // Check if we should send encouragement
            int totalEntries = databaseHelper.getEntryCount(username);

            // Send encouragement every 10 entries
            boolean shouldEncourage = (totalEntries > 0) && (totalEntries % 10 == 0);

            Log.d(TAG, "Encouragement check - Total entries: " + totalEntries + ", Should encourage: " + shouldEncourage);
            return shouldEncourage;

        } catch (Exception e) {
            Log.e(TAG, "Error checking encouragement criteria: ", e);
            return false;
        }
    }

    private boolean validateInputs(String weightStr, String dateStr, String timeStr) {
        boolean isValid = true;

        // Validate weight
        if (TextUtils.isEmpty(weightStr)) {
            tilWeight.setError("Weight is required");
            isValid = false;
        } else {
            try {
                double weight = Double.parseDouble(weightStr);
                if (weight <= 0) {
                    tilWeight.setError("Weight must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilWeight.setError("Enter a valid number");
                isValid = false;
            }
        }

        // Date and time should always be set from the pickers
        if (TextUtils.isEmpty(dateStr)) {
            showToast("Please select a date");
            isValid = false;
        }

        if (TextUtils.isEmpty(timeStr)) {
            showToast("Please select a time");
            isValid = false;
        }

        return isValid;
    }

    // Enum for validation results
    private enum ValidationResult {
        VALID,              // Weight is valid, proceed normally
        INVALID,            // Weight is invalid, show error and stop
        PENDING_CONFIRMATION // Weight needs confirmation, dialog shown
    }

    private ValidationResult validateWeightValue(double weight) {
        // Check for reasonable weight range (20kg to 300kg)
        if (weight < 20 || weight > 300) {
            tilWeight.setError("Weight should be between 20-300 kg");
            return ValidationResult.INVALID;
        }

        // Check if the weight is significantly different from the last entry
        try {
            WeightEntry lastEntry = databaseHelper.getMostRecentWeightEntry(username);
            if (lastEntry != null) {
                double difference = Math.abs(weight - lastEntry.getWeight());
                if (difference > 10) { // More than 10kg difference
                    new androidx.appcompat.app.AlertDialog.Builder(getContext())
                            .setTitle("Confirm Weight")
                            .setMessage(String.format("This weight (%.1f kg) is significantly different from your last entry (%.1f kg). Are you sure this is correct?",
                                    weight, lastEntry.getWeight()))
                            .setPositiveButton("Yes, Add Entry", (dialog, which) -> {
                                // User confirmed, proceed with adding
                                proceedWithAdding(weight);
                            })
                            .setNegativeButton("No, Edit", null)
                            .show();
                    return ValidationResult.PENDING_CONFIRMATION;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking last entry: ", e);
            // Continue with adding if we can't check the last entry
        }

        return ValidationResult.VALID;
    }

    private void proceedWithAdding(double weight) {
        try {
            Log.d(TAG, "Proceeding with confirmed weight: " + weight);

            // Get user ID
            int userId = databaseHelper.getUserId(username);
            if (userId == -1) {
                showToast("Error: User not found");
                return;
            }

            // Get previous weights for notification comparison
            double previousCurrentWeight = databaseHelper.getCurrentWeight(username);
            double goalWeight = databaseHelper.getGoalWeight(username);
            double startingWeight = databaseHelper.getStartingWeight(username);

            // Create standardized date/time strings
            SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dbDateString = dbDateFormat.format(selectedDate.getTime());

            SimpleDateFormat dbTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String dbTimeString = dbTimeFormat.format(selectedTime.getTime());

            WeightEntry newEntry = new WeightEntry(userId, weight, dbDateString, dbTimeString, "");

            long entryId = databaseHelper.addWeightEntry(newEntry);

            if (entryId != -1) {
                // Update current weight with the most recent entry by actual date/time
                databaseHelper.updateCurrentWeightFromMostRecent(username);

                //Check for achievements and send notifications
                checkAndSendNotifications(weight, previousCurrentWeight, goalWeight, startingWeight);

                showToast("Weight entry added successfully!");

                if (listener != null) {
                    listener.onEntryAdded();
                }

                dismiss();
            } else {
                showToast("Failed to add weight entry. Please try again.");
            }
        } catch (Exception e) {
            showToast("Error adding weight entry: " + e.getMessage());
            Log.e(TAG, "Error in proceedWithAdding: ", e);
        }
    }

    private void showToast(String message) {
        try {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast: ", e);
        }
    }
}