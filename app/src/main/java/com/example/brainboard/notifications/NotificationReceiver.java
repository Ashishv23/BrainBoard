package com.example.brainboard.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.brainboard.R;

/**
 * NotificationReceiver.java
 *
 * A BroadcastReceiver responsible for handling scheduled task reminders and notification actions
 * in the BrainBoard app.
 *
 * Features:
 * - Displays a notification when a task is due soon
 * - Handles three user actions directly from the notification:
 *     - ✔️ Mark as Done: Removes the task from SharedPreferences (can be extended)
 *     - 🔁 Snooze: Reschedules the reminder 5 minutes later
 *     - ❌ Dismiss: Dismisses the notification without action
 *
 * Notification Details:
 * - Uses Android's NotificationManager and NotificationCompat
 * - Creates a notification channel (required for Android 8+)
 * - Notification includes three interactive action buttons
 *
 * Dependencies:
 * - NotificationUtils: Utility class to handle rescheduling for snooze
 * - SharedPreferences ("taskPrefs"): Used to simulate marking a task as done
 *
 * Notes:
 * - `taskTitle` and `taskId` are passed via Intent extras to identify the task.
 * - `taskId.hashCode()` is used as the notification ID for uniqueness.
 */


public class NotificationReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "brainboard_wear_channel";
    public static final String ACTION_MARK_DONE = "MARK_DONE";
    public static final String ACTION_SNOOZE = "SNOOZE";
    public static final String ACTION_DISMISS = "DISMISS";

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("taskTitle");
        String taskId = intent.getStringExtra("taskId");

        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_MARK_DONE:
                    // Handle marking the task as done
                    context.getSharedPreferences("taskPrefs", Context.MODE_PRIVATE)
                            .edit()
                            .remove(taskTitle) // Optional: You can use a proper key if needed
                            .apply();
                    return;

                case ACTION_SNOOZE:
                    // Reschedule notification in 5 minutes (300000 ms)
                    NotificationUtils.scheduleSnoozedNotification(context, taskTitle, taskId, 5 * 60 * 1000);
                    return;

                case ACTION_DISMISS:
                    return; // Do nothing (dismiss notification)
            }
        }

        // Normal notification
        showNotification(context, taskTitle, taskId);
    }

    private void showNotification(Context context, String taskTitle, String taskId) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "BrainBoard Wear Alerts", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Intents for actions
        PendingIntent markDoneIntent = PendingIntent.getBroadcast(
                context, 1, getActionIntent(context, ACTION_MARK_DONE, taskTitle, taskId), PendingIntent.FLAG_IMMUTABLE);
        PendingIntent snoozeIntent = PendingIntent.getBroadcast(
                context, 2, getActionIntent(context, ACTION_SNOOZE, taskTitle, taskId), PendingIntent.FLAG_IMMUTABLE);
        PendingIntent dismissIntent = PendingIntent.getBroadcast(
                context, 3, getActionIntent(context, ACTION_DISMISS, taskTitle, taskId), PendingIntent.FLAG_IMMUTABLE);

        // Notification with actions
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Task Reminder")
                .setContentText(taskTitle + " is due soon!")
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher, "✔️ Done", markDoneIntent)
                .addAction(R.mipmap.ic_launcher, "🔁 Snooze", snoozeIntent)
                .addAction(R.mipmap.ic_launcher, "❌ Dismiss", dismissIntent);

        notificationManager.notify(taskId.hashCode(), builder.build());
    }

    private Intent getActionIntent(Context context, String action, String title, String taskId) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(action);
        intent.putExtra("taskTitle", title);
        intent.putExtra("taskId", taskId);
        return intent;
    }
}
