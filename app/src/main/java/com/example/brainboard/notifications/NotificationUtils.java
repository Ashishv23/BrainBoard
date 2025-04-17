package com.example.brainboard.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

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
