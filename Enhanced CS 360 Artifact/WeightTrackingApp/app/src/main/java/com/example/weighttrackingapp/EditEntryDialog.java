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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditEntryDialog extends Dialog {

    private static final String TAG = "EditEntryDialog";

    // UI Components
    private ImageButton btnCloseDialog;
    private TextInputLayout tilWeight, tilDate, tilTime;
    private TextInputEditText etWeight, etDate, etTime;
    private MaterialButton btnCancel, btnUpdateEntry;

    // Data
    private WeightEntry weightEntry;
    private String username;
    private DatabaseHelper databaseHelper;
    private OnEntryUpdatedListener listener;
    private Calendar selectedDate;
    private Calendar selectedTime;

    // Interface for callback
    public interface OnEntryUpdatedListener {
        void onEntryUpdated();
    }

    public EditEntryDialog(@NonNull Context context, WeightEntry weightEntry, String username, DatabaseHelper databaseHelper) {
        super(context);
        this.weightEntry = weightEntry;
        this.username = username;
        this.databaseHelper = databaseHelper;
        this.selectedDate = Calendar.getInstance();
        this.selectedTime = Calendar.getInstance();
    }

    public void setOnEntryUpdatedListener(OnEntryUpdatedListener listener) {
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

            // Load entry data
            loadEntryData();

            Log.d(TAG, "EditEntryDialog created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating EditEntryDialog: ", e);
            showToast("Error opening edit dialog");
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
        btnUpdateEntry = findViewById(R.id.btnAddEntry);

        // Change button text to "UPDATE"
        btnUpdateEntry.setText("UPDATE ENTRY");
    }

    private void setupClickListeners() {
        btnCloseDialog.setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());
        btnUpdateEntry.setOnClickListener(v -> updateWeightEntry());

        // Date picker
        etDate.setOnClickListener(v -> showDatePicker());
        tilDate.setEndIconOnClickListener(v -> showDatePicker());

        // Time picker
        etTime.setOnClickListener(v -> showTimePicker());
        tilTime.setEndIconOnClickListener(v -> showTimePicker());
    }

    private void loadEntryData() {
        try {
            // Load weight
            etWeight.setText(String.valueOf(weightEntry.getWeight()));

            // Parse and load date
            SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date entryDate = dbDateFormat.parse(weightEntry.getDate());
                selectedDate.setTime(entryDate);
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing entry date: ", e);
                // Use current date as fallback
                selectedDate = Calendar.getInstance();
            }

            // Parse and load time
            SimpleDateFormat dbTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            try {
                Date entryTime = dbTimeFormat.parse(weightEntry.getTime());
                selectedTime.setTime(entryTime);
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing entry time: ", e);
                // Use current time as fallback
                selectedTime = Calendar.getInstance();
            }

            // Update displays
            updateDateDisplay();
            updateTimeDisplay();

            Log.d(TAG, "Entry data loaded - Weight: " + weightEntry.getWeight() + ", Date: " + weightEntry.getDate() + ", Time: " + weightEntry.getTime());
        } catch (Exception e) {
            Log.e(TAG, "Error loading entry data: ", e);
            showToast("Error loading entry data");
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

    private void updateWeightEntry() {
        try {
            Log.d(TAG, "Starting to update weight entry for user: " + username);

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

            // Validate weight value
            if (!validateWeightValue(weight)) {
                Log.e(TAG, "Weight value validation failed");
                return;
            }

            // Create standardized date/time strings for database storage
            SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dbDateString = dbDateFormat.format(selectedDate.getTime());

            SimpleDateFormat dbTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String dbTimeString = dbTimeFormat.format(selectedTime.getTime());

            Log.d(TAG, "Database date format: " + dbDateString + ", Database time format: " + dbTimeString);

            // Update the entry
            boolean updated = databaseHelper.updateWeightEntry(weightEntry.getId(), weight, dbDateString, dbTimeString);
            Log.d(TAG, "Database update result: " + updated);

            if (updated) {
                // Update current weight with the most recent entry by actual date/time
                boolean weightUpdated = databaseHelper.updateCurrentWeightFromMostRecent(username);
                Log.d(TAG, "Current weight update result: " + weightUpdated);

                showToast("Weight entry updated successfully!");

                // Notify listener
                if (listener != null) {
                    listener.onEntryUpdated();
                }

                dismiss();
                Log.d(TAG, "Weight entry updated successfully");
            } else {
                showToast("Failed to update weight entry. Please try again.");
                Log.e(TAG, "Failed to update weight entry in database");
            }

        } catch (NumberFormatException e) {
            Log.e(TAG, "Number format error: ", e);
            showToast("Please enter a valid weight value");
        } catch (Exception e) {
            Log.e(TAG, "Error updating weight entry: ", e);
            showToast("Error updating weight entry: " + e.getMessage());
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

    private boolean validateWeightValue(double weight) {
        // Check for reasonable weight range (20kg to 300kg)
        if (weight < 20 || weight > 300) {
            tilWeight.setError("Weight should be between 20-300 kg");
            return false;
        }

        return true;
    }

    private void showToast(String message) {
        try {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast: ", e);
        }
    }
}