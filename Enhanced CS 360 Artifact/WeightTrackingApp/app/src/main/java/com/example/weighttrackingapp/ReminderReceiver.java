package com.example.weighttrackingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "ReminderReceiver triggered - checking if should send notification");

        try {
            // Check if notifications are still enabled
            SharedPreferences settingsPrefs = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
            boolean pushEnabled = settingsPrefs.getBoolean("push_notifications", false);
            boolean pushPermission = settingsPrefs.getBoolean("push_notifications_permission", false);

            if (!pushEnabled || !pushPermission) {
                Log.d(TAG, "Notifications disabled - skipping reminder");
                return;
            }

            // Send the daily reminder notification
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.showDailyReminder();

            Log.d(TAG, "Daily reminder notification sent successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in ReminderReceiver: ", e);
        }
    }
}