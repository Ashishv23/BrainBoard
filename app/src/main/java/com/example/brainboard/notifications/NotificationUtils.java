package com.example.brainboard.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * NotificationUtils.java
 *
 * Utility class for scheduling task reminder notifications in the BrainBoard app.
 *
 * Features:
 * - Schedules a one-time snoozed notification using AlarmManager.
 * - Accepts a custom delay duration (in milliseconds) for flexible snoozing.
 *
 * Parameters:
 * - context: Application or activity context
 * - taskTitle: Title of the task to display in the notification
 * - taskId: Unique identifier used to manage PendingIntent and notification ID
 * - delayMillis: Delay time before the notification should trigger (e.g., 5 minutes = 300000)
 *
 * Dependencies:
 * - AlarmManager: Used to schedule the future notification
 * - NotificationReceiver: BroadcastReceiver that builds and shows the actual notification
 */


public class NotificationUtils {

    public static void scheduleSnoozedNotification(Context context, String taskTitle, String taskId, long delayMillis) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("taskTitle", taskTitle);
        intent.putExtra("taskId", taskId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, taskId.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + delayMillis,
                pendingIntent
        );
    }
}
