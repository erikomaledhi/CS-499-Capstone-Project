package com.example.weighttrackingapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

public class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";
    private static final int REMINDER_REQUEST_CODE = 1001;

    private Context context;
    private SharedPreferences settingsPreferences;

    public ReminderScheduler(Context context) {
        this.context = context;

        // Get current username and use user-specific settings
        SharedPreferences mainPrefs = context.getSharedPreferences("WeightTrackerPrefs", Context.MODE_PRIVATE);
        String currentUsername = mainPrefs.getString("username", "");

        this.settingsPreferences = context.getSharedPreferences("SettingsPrefs_" + currentUsername, Context.MODE_PRIVATE);
    }

    /**
     * Schedule daily reminder at the time set in preferences
     */
    public void scheduleDailyReminder() {
        try {
            // Check if notifications are enabled
            boolean pushEnabled = settingsPreferences.getBoolean("push_notifications", false);
            boolean pushPermission = settingsPreferences.getBoolean("push_notifications_permission", false);

            Log.d(TAG, "Scheduling reminder - Push enabled: " + pushEnabled + ", Permission: " + pushPermission);

            if (!pushEnabled || !pushPermission) {
                Log.d(TAG, "Notifications disabled - not scheduling reminder");
                return;
            }

            // Get reminder time from preferences
            String reminderTime = settingsPreferences.getString("reminder_time", "9:00 AM");
            Log.d(TAG, "Reminder time from preferences: " + reminderTime);

            // Parse the time
            Calendar reminderCalendar = parseReminderTime(reminderTime);
            if (reminderCalendar == null) {
                Log.e(TAG, "Failed to parse reminder time: " + reminderTime);
                return;
            }

            // If the time has already passed today, schedule for tomorrow
            Calendar now = Calendar.getInstance();
            Log.d(TAG, "Current time: " + now.getTime());
            Log.d(TAG, "Reminder time today: " + reminderCalendar.getTime());

            if (reminderCalendar.before(now)) {
                reminderCalendar.add(Calendar.DAY_OF_YEAR, 1);
                Log.d(TAG, "Reminder time has passed today, scheduling for tomorrow: " + reminderCalendar.getTime());
            }

            // Create intent for the alarm
            Intent intent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REMINDER_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Get alarm manager and schedule the alarm
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                // Cancel any existing alarm first
                alarmManager.cancel(pendingIntent);

                try {
                    // Use setExactAndAllowWhileIdle for better reliability
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            reminderCalendar.getTimeInMillis(),
                            pendingIntent
                    );

                    Log.d(TAG, "Daily reminder scheduled for: " + reminderCalendar.getTime());
                    Log.d(TAG, "Time in millis: " + reminderCalendar.getTimeInMillis());
                    Log.d(TAG, "Current time in millis: " + System.currentTimeMillis());
                    Log.d(TAG, "Time difference: " + (reminderCalendar.getTimeInMillis() - System.currentTimeMillis()) / 1000 + " seconds");

                } catch (SecurityException e) {
                    Log.e(TAG, "Permission denied for exact alarm. Trying inexact alarm: ", e);
                    // Fallback to inexact alarm if exact alarm permission is denied
                    alarmManager.setInexactRepeating(
                            AlarmManager.RTC_WAKEUP,
                            reminderCalendar.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY,
                            pendingIntent
                    );
                    Log.d(TAG, "Fallback: inexact repeating alarm scheduled");
                }

            } else {
                Log.e(TAG, "AlarmManager is null - cannot schedule reminder");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error scheduling daily reminder: ", e);
        }
    }

    /**
     * Cancel the scheduled daily reminder
     */
    public void cancelDailyReminder() {
        try {
            Intent intent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REMINDER_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                Log.d(TAG, "Daily reminder cancelled");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling daily reminder: ", e);
        }
    }

    /**
     * Parse reminder time string (e.g., "9:00 AM") to Calendar object
     */
    private Calendar parseReminderTime(String timeString) {
        try {
            Calendar calendar = Calendar.getInstance();

            // Remove AM/PM and split by ":"
            String cleanTime = timeString.replace(" AM", "").replace(" PM", "");
            String[] parts = cleanTime.split(":");

            if (parts.length != 2) {
                Log.e(TAG, "Invalid time format: " + timeString);
                return null;
            }

            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            // Convert to 24-hour format
            if (timeString.contains("PM") && hour != 12) {
                hour += 12;
            } else if (timeString.contains("AM") && hour == 12) {
                hour = 0;
            }

            // Set the time for today
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            Log.d(TAG, "Parsed reminder time: " + calendar.getTime() + " from string: " + timeString);
            return calendar;

        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing time numbers from: " + timeString, e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing reminder time: " + timeString, e);
            return null;
        }
    }

    /**
     * Reschedule reminder when settings change
     */
    public void rescheduleReminder() {
        try {
            Log.d(TAG, "Rescheduling daily reminder due to settings change");
            cancelDailyReminder();
            scheduleDailyReminder();
        } catch (Exception e) {
            Log.e(TAG, "Error rescheduling reminder: ", e);
        }
    }
}