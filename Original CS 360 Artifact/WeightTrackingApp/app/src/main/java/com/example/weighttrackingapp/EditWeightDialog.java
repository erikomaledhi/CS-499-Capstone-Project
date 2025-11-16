package com.example.weighttrackingapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditWeightDialog extends Dialog {

    private static final String TAG = "EditWeightDialog";

    // UI Components
    private ImageButton btnCloseDialog;
    private TextInputLayout tilStartingWeight, tilCurrentWeight, tilGoalWeight;
    private TextInputEditText etStartingWeight, etCurrentWeight, etGoalWeight;
    private TextView tvWeightDifference, tvProgressInfo;
    private MaterialButton btnCancel, btnSave;

    // Data
    private String username;
    private DatabaseHelper databaseHelper;
    private OnWeightUpdatedListener listener;

    // Interface for callback
    public interface OnWeightUpdatedListener {
        void onWeightUpdated();
    }

    public EditWeightDialog(@NonNull Context context, String username, DatabaseHelper databaseHelper) {
        super(context);
        this.username = username;
        this.databaseHelper = databaseHelper;
    }

    public void setOnWeightUpdatedListener(OnWeightUpdatedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_weight);

        try {
            // Initialize views
            initializeViews();

            // Set up click listeners
            setupClickListeners();

            // Load current data
            loadCurrentWeights();

            // Set up real-time calculation
            setupWeightCalculation();

            Log.d(TAG, "EditWeightDialog created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating EditWeightDialog: ", e);
            showToast("Error opening edit dialog");
            dismiss();
        }
    }

    private void initializeViews() {
        btnCloseDialog = findViewById(R.id.btnCloseDialog);

        //find starting weight fields
        tilStartingWeight = findViewById(R.id.tilStartingWeight);
        etStartingWeight = findViewById(R.id.etStartingWeight);

        tilCurrentWeight = findViewById(R.id.tilCurrentWeight);
        tilGoalWeight = findViewById(R.id.tilGoalWeight);
        etCurrentWeight = findViewById(R.id.etCurrentWeight);
        etGoalWeight = findViewById(R.id.etGoalWeight);
        tvWeightDifference = findViewById(R.id.tvWeightDifference);

        //find progress info
        tvProgressInfo = findViewById(R.id.tvProgressInfo);

        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupClickListeners() {
        btnCloseDialog.setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveWeights());
    }

    private void loadCurrentWeights() {
        try {
            double startingWeight = databaseHelper.getStartingWeight(username);
            double currentWeight = databaseHelper.getCurrentWeight(username);
            double goalWeight = databaseHelper.getGoalWeight(username);

            // Only set starting weight if the field exists
            if (etStartingWeight != null && startingWeight > 0) {
                etStartingWeight.setText(String.valueOf(startingWeight));
            }

            if (currentWeight > 0) {
                etCurrentWeight.setText(String.valueOf(currentWeight));
            }

            if (goalWeight > 0) {
                etGoalWeight.setText(String.valueOf(goalWeight));
            }

            updateWeightCalculations();

            Log.d(TAG, "Loaded weights - Starting: " + startingWeight + ", Current: " + currentWeight + ", Goal: " + goalWeight);
        } catch (Exception e) {
            Log.e(TAG, "Error loading current weights: ", e);
            showToast("Error loading current weights");
        }
    }

    private void setupWeightCalculation() {
        try {
            // Add text change listeners for real-time calculation
            if (etStartingWeight != null) {
                etStartingWeight.addTextChangedListener(new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(android.text.Editable s) {
                        updateWeightCalculations();
                    }
                });
            }

            etCurrentWeight.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    updateWeightCalculations();
                }
            });

            etGoalWeight.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    updateWeightCalculations();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up weight calculation: ", e);
        }
    }

    private void updateWeightCalculations() {
        try {
            String currentWeightStr = etCurrentWeight.getText().toString().trim();
            String goalWeightStr = etGoalWeight.getText().toString().trim();
            String startingWeightStr = "";

            if (etStartingWeight != null) {
                startingWeightStr = etStartingWeight.getText().toString().trim();
            }

            if (!TextUtils.isEmpty(currentWeightStr) && !TextUtils.isEmpty(goalWeightStr)) {
                try {
                    double currentWeight = Double.parseDouble(currentWeightStr);
                    double goalWeight = Double.parseDouble(goalWeightStr);
                    double remainingWeight = currentWeight - goalWeight;

                    if (remainingWeight <= 0) {
                        tvWeightDifference.setText("Goal achieved!");
                        tvWeightDifference.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
                        if (tvProgressInfo != null) {
                            tvProgressInfo.setText("100% complete");
                            tvProgressInfo.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
                        }
                    } else {
                        tvWeightDifference.setText(String.format("%.1f kg", remainingWeight));
                        tvWeightDifference.setTextColor(getContext().getResources().getColor(android.R.color.holo_orange_dark));

                        // Calculate progress if starting weight is available
                        if (!TextUtils.isEmpty(startingWeightStr) && tvProgressInfo != null) {
                            try {
                                double startingWeight = Double.parseDouble(startingWeightStr);
                                double totalToLose = startingWeight - goalWeight;
                                double lostSoFar = startingWeight - currentWeight;
                                int progressPercentage = 0;

                                if (totalToLose > 0) {
                                    progressPercentage = Math.max(0, Math.min(100, (int) ((lostSoFar / totalToLose) * 100)));
                                }

                                tvProgressInfo.setText(String.format("%d%% complete (%.1f kg lost)", progressPercentage, lostSoFar));
                                tvProgressInfo.setTextColor(getContext().getResources().getColor(android.R.color.holo_blue_dark));
                            } catch (NumberFormatException e) {
                                if (tvProgressInfo != null) {
                                    tvProgressInfo.setText("");
                                }
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    tvWeightDifference.setText("Enter valid weights");
                    tvWeightDifference.setTextColor(getContext().getResources().getColor(android.R.color.darker_gray));
                    if (tvProgressInfo != null) {
                        tvProgressInfo.setText("");
                    }
                }
            } else {
                tvWeightDifference.setText("Enter weights");
                tvWeightDifference.setTextColor(getContext().getResources().getColor(android.R.color.darker_gray));
                if (tvProgressInfo != null) {
                    tvProgressInfo.setText("");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating weight calculations: ", e);
        }
    }

    private void saveWeights() {
        try {
            // Clear any previous errors
            if (tilStartingWeight != null) {
                tilStartingWeight.setError(null);
            }
            tilCurrentWeight.setError(null);
            tilGoalWeight.setError(null);

            String startingWeightStr = "";
            if (etStartingWeight != null) {
                startingWeightStr = etStartingWeight.getText().toString().trim();
            }

            String currentWeightStr = etCurrentWeight.getText().toString().trim();
            String goalWeightStr = etGoalWeight.getText().toString().trim();

            // Validate inputs
            if (!validateInputs(startingWeightStr, currentWeightStr, goalWeightStr)) {
                return;
            }

            double startingWeight = 0;
            if (!TextUtils.isEmpty(startingWeightStr)) {
                startingWeight = Double.parseDouble(startingWeightStr);
            }

            double currentWeight = Double.parseDouble(currentWeightStr);
            double goalWeight = Double.parseDouble(goalWeightStr);

            // Validate weight values
            if (!validateWeightValues(startingWeight, currentWeight, goalWeight)) {
                return;
            }

            // Save to database
            boolean startingUpdated = true;
            if (startingWeight > 0) {
                startingUpdated = databaseHelper.updateStartingWeight(username, startingWeight);
            }

            boolean currentUpdated = databaseHelper.updateCurrentWeight(username, currentWeight);
            boolean goalUpdated = databaseHelper.updateGoalWeight(username, goalWeight);

            if (startingUpdated && currentUpdated && goalUpdated) {
                showToast("Weights updated successfully!");

                // Notify listener
                if (listener != null) {
                    listener.onWeightUpdated();
                }

                dismiss();
                Log.d(TAG, "Weights saved successfully");
            } else {
                showToast("Failed to update weights. Please try again.");
                Log.e(TAG, "Failed to update weights in database");
            }

        } catch (NumberFormatException e) {
            showToast("Please enter valid weight values");
            Log.e(TAG, "Number format error: ", e);
        } catch (Exception e) {
            showToast("Error saving weights: " + e.getMessage());
            Log.e(TAG, "Error saving weights: ", e);
        }
    }

    private boolean validateInputs(String startingWeightStr, String currentWeightStr, String goalWeightStr) {
        boolean isValid = true;

        // Validate starting weight
        if (!TextUtils.isEmpty(startingWeightStr) && tilStartingWeight != null) {
            try {
                double weight = Double.parseDouble(startingWeightStr);
                if (weight <= 0) {
                    tilStartingWeight.setError("Weight must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilStartingWeight.setError("Enter a valid number");
                isValid = false;
            }
        }

        // Validate current weight
        if (TextUtils.isEmpty(currentWeightStr)) {
            tilCurrentWeight.setError("Current weight is required");
            isValid = false;
        } else {
            try {
                double weight = Double.parseDouble(currentWeightStr);
                if (weight <= 0) {
                    tilCurrentWeight.setError("Weight must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilCurrentWeight.setError("Enter a valid number");
                isValid = false;
            }
        }

        // Validate goal weight
        if (TextUtils.isEmpty(goalWeightStr)) {
            tilGoalWeight.setError("Goal weight is required");
            isValid = false;
        } else {
            try {
                double weight = Double.parseDouble(goalWeightStr);
                if (weight <= 0) {
                    tilGoalWeight.setError("Weight must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilGoalWeight.setError("Enter a valid number");
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean validateWeightValues(double startingWeight, double currentWeight, double goalWeight) {
        // Check for reasonable weight ranges (20kg to 300kg)
        if (startingWeight > 0 && (startingWeight < 20 || startingWeight > 300)) {
            if (tilStartingWeight != null) {
                tilStartingWeight.setError("Starting weight should be between 20-300 kg");
            }
            return false;
        }

        if (currentWeight < 20 || currentWeight > 300) {
            tilCurrentWeight.setError("Current weight should be between 20-300 kg");
            return false;
        }

        if (goalWeight < 20 || goalWeight > 300) {
            tilGoalWeight.setError("Goal weight should be between 20-300 kg");
            return false;
        }

        // Logical validation (only if starting weight is provided)
        if (startingWeight > 0) {
            if (goalWeight > startingWeight) {
                tilGoalWeight.setError("Goal weight should be less than starting weight");
                return false;
            }

            if (currentWeight > startingWeight) {
                tilCurrentWeight.setError("Current weight should not exceed starting weight");
                return false;
            }
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