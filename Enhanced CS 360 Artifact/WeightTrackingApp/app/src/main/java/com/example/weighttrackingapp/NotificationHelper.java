package com.example.weighttrackingapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    // Notification Channels
    private static final String CHANNEL_GENERAL = "general_notifications";
    private static final String CHANNEL_REMINDERS = "reminder_notifications";
    private static final String CHANNEL_GOALS = "goal_notifications";

    // Notification IDs
    private static final int NOTIFICATION_ID_WELCOME = 1001;
    private static final int NOTIFICATION_ID_DAILY_REMINDER = 1002;
    private static final int NOTIFICATION_ID_GOAL_ACHIEVED = 1003;
    private static final int NOTIFICATION_ID_MILESTONE = 1004;
    private static final int NOTIFICATION_ID_MOTIVATIONAL = 1005;

    private Context context;
    private NotificationManagerCompat notificationManager;
    private SharedPreferences settingsPreferences;
    private SharedPreferences notificationHistoryPrefs;
    private String currentUsername;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);

        // Get current username from main preferences
        SharedPreferences mainPrefs = context.getSharedPreferences("WeightTrackerPrefs", Context.MODE_PRIVATE);
        this.currentUsername = mainPrefs.getString("username", "");

        // Use user-specific settings preferences
        this.settingsPreferences = context.getSharedPreferences("SettingsPrefs_" + currentUsername, Context.MODE_PRIVATE);

        // Use user-specific notification history
        this.notificationHistoryPrefs = context.getSharedPreferences("NotificationHistory_" + currentUsername, Context.MODE_PRIVATE);

        createNotificationChannels();
    }

    /**
     * Create notification channels
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                // General notifications channel
                NotificationChannel generalChannel = new NotificationChannel(
                        CHANNEL_GENERAL,
                        "General Notifications",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                generalChannel.setDescription("Welcome messages and app updates");
                generalChannel.setShowBadge(true);
                manager.createNotificationChannel(generalChannel);

                // Reminders channel
                NotificationChannel reminderChannel = new NotificationChannel(
                        CHANNEL_REMINDERS,
                        "Daily Reminders",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                reminderChannel.setDescription("Daily weight tracking reminders");
                reminderChannel.setShowBadge(true);
                manager.createNotificationChannel(reminderChannel);

                // Goals channel
                NotificationChannel goalChannel = new NotificationChannel(
                        CHANNEL_GOALS,
                        "Goal Achievements",
                        NotificationManager.IMPORTANCE_HIGH
                );
                goalChannel.setDescription("Weight goal achievements and milestones");
                goalChannel.setShowBadge(true);
                manager.createNotificationChannel(goalChannel);

                Log.d(TAG, "Notification channels created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channels: ", e);
            }
        }
    }

    /**
     * Save notification to history for display in notification tab (user-specific)
     */
    private void saveNotificationToHistory(String title, String message, String type) {
        try {
            // Only save if we have a valid username
            if (currentUsername == null || currentUsername.isEmpty()) {
                Log.w(TAG, "No current username - not saving notification to history");
                return;
            }

            // Get current notification history for this specific user
            String historyJson = notificationHistoryPrefs.getString("notifications", "[]");
            JSONArray notifications = new JSONArray(historyJson);

            // Create new notification object
            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("message", message);
            notification.put("type", type);
            notification.put("timestamp", System.currentTimeMillis());
            notification.put("username", currentUsername); // Store username for extra safety

            // Format timestamp for display
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
            notification.put("displayTime", sdf.format(new Date()));

            // Add to beginning of array (most recent first)
            JSONArray newNotifications = new JSONArray();
            newNotifications.put(notification);

            // Add existing notifications (keep only last 20)
            int maxNotifications = 20;
            for (int i = 0; i < Math.min(notifications.length(), maxNotifications - 1); i++) {
                newNotifications.put(notifications.get(i));
            }

            // Save back to user-specific preferences
            SharedPreferences.Editor editor = notificationHistoryPrefs.edit();
            editor.putString("notifications", newNotifications.toString());
            editor.apply();

            Log.d(TAG, "Notification saved to history for user " + currentUsername + ": " + title);
        } catch (Exception e) {
            Log.e(TAG, "Error saving notification to history: ", e);
        }
    }
    private boolean areNotificationsEnabled() {
        boolean permissionGranted = settingsPreferences.getBoolean("push_notifications_permission", false);
        boolean settingEnabled = settingsPreferences.getBoolean("push_notifications", true);

        Log.d(TAG, "Notifications enabled check - Permission: " + permissionGranted + ", Setting: " + settingEnabled);
        return permissionGranted && settingEnabled;
    }

    /**
     * Show welcome notification when push notifications are first enabled
     */
    public void showWelcomeNotification() {
        if (!areNotificationsEnabled()) {
            Log.d(TAG, "Notifications disabled - skipping welcome notification");
            return;
        }

        try {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GENERAL)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setContentTitle("Welcome to WeightTracker!")
                    .setContentText("Push notifications are now enabled. We'll help keep you motivated!")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Push notifications are now enabled. We'll send you daily reminders, celebrate your achievements, and keep you motivated on your weight loss journey!"))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setShowWhen(true);

            notificationManager.notify(NOTIFICATION_ID_WELCOME, builder.build());

            // Save to notification history
            saveNotificationToHistory("Welcome to WeightTracker!",
                    "Push notifications are now enabled. We'll help keep you motivated!",
                    "welcome");

            Log.d(TAG, "Welcome notification sent");

        } catch (Exception e) {
            Log.e(TAG, "Error showing welcome notification: ", e);
        }
    }

    /**
     * Show daily weight tracking reminder
     */
    public void showDailyReminder() {
        if (!areNotificationsEnabled()) {
            Log.d(TAG, "Notifications disabled - skipping daily reminder");
            return;
        }

        try {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Get different reminder messages
            String[] reminderMessages = {
                    "Time to log your weight! Keep up the great work! 💪",
                    "Don't forget to track your progress today! 📊",
                    "Your daily weight check-in is waiting! 🎯",
                    "Stay consistent - log your weight now! ⭐",
                    "Track your progress and stay motivated! 🚀"
            };

            String message = reminderMessages[(int) (Math.random() * reminderMessages.length)];

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                    .setSmallIcon(R.drawable.ic_scale)
                    .setContentTitle("Daily Weight Reminder")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setShowWhen(true);

            notificationManager.notify(NOTIFICATION_ID_DAILY_REMINDER, builder.build());

            // Save to notification history
            saveNotificationToHistory("Daily Weight Reminder", message, "reminder");

            Log.d(TAG, "Daily reminder notification sent");

        } catch (Exception e) {
            Log.e(TAG, "Error showing daily reminder: ", e);
        }
    }

    /**
     * Show goal achievement notification
     */
    public void showGoalAchievedNotification(double goalWeight) {
        if (!areNotificationsEnabled()) {
            Log.d(TAG, "Notifications disabled - skipping goal achievement notification");
            return;
        }

        try {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GOALS)
                    .setSmallIcon(R.drawable.ic_target)
                    .setContentTitle("🎉 GOAL ACHIEVED! 🎉")
                    .setContentText(String.format("Congratulations! You've reached your goal weight of %.1f kg!", goalWeight))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(String.format("🎉 AMAZING! You've successfully reached your goal weight of %.1f kg! " +
                                    "Your dedication and consistency have paid off. Take a moment to celebrate this incredible achievement!", goalWeight)))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setShowWhen(true);

            notificationManager.notify(NOTIFICATION_ID_GOAL_ACHIEVED, builder.build());

            // Save to notification history
            saveNotificationToHistory("🎉 GOAL ACHIEVED! 🎉",
                    String.format("Congratulations! You've reached your goal weight of %.1f kg!", goalWeight),
                    "goal");

            Log.d(TAG, "Goal achieved notification sent for weight: " + goalWeight);

        } catch (Exception e) {
            Log.e(TAG, "Error showing goal achieved notification: ", e);
        }
    }

    /**
     * Show milestone notification
     */
    public void showMilestoneNotification(double weightLost, double startingWeight, double currentWeight) {
        if (!areNotificationsEnabled()) {
            Log.d(TAG, "Notifications disabled - skipping milestone notification");
            return;
        }

        try {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            String title = String.format("🌟 %.0f kg Lost! 🌟", weightLost);
            String message = String.format("You've lost %.1f kg! From %.1f kg to %.1f kg. Keep going!",
                    weightLost, startingWeight, currentWeight);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GOALS)
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(String.format("🌟 Incredible progress! You've lost %.1f kg so far! " +
                                            "From %.1f kg to %.1f kg - that's real progress. " +
                                            "Every step counts, and you're doing amazing. Keep up the fantastic work!",
                                    weightLost, startingWeight, currentWeight)))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setShowWhen(true);

            notificationManager.notify(NOTIFICATION_ID_MILESTONE, builder.build());

            // Save to notification history
            saveNotificationToHistory(title, message, "milestone");

            Log.d(TAG, "Milestone notification sent for " + weightLost + " kg lost");

        } catch (Exception e) {
            Log.e(TAG, "Error showing milestone notification: ", e);
        }
    }



    /**
     * Cancel all notifications
     */
    public void cancelAllNotifications() {
        try {
            notificationManager.cancelAll();
            Log.d(TAG, "All notifications cancelled");
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling notifications: ", e);
        }
    }

    /**
     * Cancel specific notification
     */
    public void cancelNotification(int notificationId) {
        try {
            notificationManager.cancel(notificationId);
            Log.d(TAG, "Notification cancelled: " + notificationId);
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling notification: ", e);
        }
    }

    /**
     * Check for goal achievement and send notification if appropriate
     */
    public void checkAndNotifyGoalAchievement(String username, DatabaseHelper databaseHelper) {
        try {
            double currentWeight = databaseHelper.getCurrentWeight(username);
            double goalWeight = databaseHelper.getGoalWeight(username);

            if (currentWeight > 0 && goalWeight > 0 && currentWeight <= goalWeight) {
                // Check if we've already notified for this goal achievement
                String goalKey = "goal_achieved_" + goalWeight + "_notified";
                boolean alreadyNotified = settingsPreferences.getBoolean(goalKey, false);

                if (!alreadyNotified) {
                    // Goal achieved for the first time!
                    showGoalAchievedNotification(goalWeight);

                    // Mark this goal as notified
                    SharedPreferences.Editor editor = settingsPreferences.edit();
                    editor.putBoolean(goalKey, true);
                    editor.apply();

                    Log.d(TAG, "Goal achievement detected and notification sent for " + goalWeight + "kg");
                } else {
                    Log.d(TAG, "Goal already achieved and notified for " + goalWeight + "kg - skipping duplicate");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking goal achievement: ", e);
        }
    }

    /**
     * Check for milestones and send notification if appropriate
     */
    public void checkAndNotifyMilestone(String username, DatabaseHelper databaseHelper) {
        try {
            double startingWeight = databaseHelper.getStartingWeight(username);
            double currentWeight = databaseHelper.getCurrentWeight(username);

            if (startingWeight > 0 && currentWeight > 0 && startingWeight > currentWeight) {
                double weightLost = startingWeight - currentWeight;

                // Check for milestone achievements (every 5kg lost)
                int milestone = (int) (weightLost / 5) * 5;

                if (milestone >= 5) {
                    // Check if we've already notified for this milestone
                    String milestoneKey = "milestone_" + milestone + "_notified";
                    boolean alreadyNotified = settingsPreferences.getBoolean(milestoneKey, false);

                    if (!alreadyNotified) {
                        showMilestoneNotification(milestone, startingWeight, currentWeight);

                        // Mark this milestone as notified
                        SharedPreferences.Editor editor = settingsPreferences.edit();
                        editor.putBoolean(milestoneKey, true);
                        editor.apply();

                        Log.d(TAG, "Milestone notification sent for " + milestone + " kg lost");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking milestone: ", e);
        }
    }
}