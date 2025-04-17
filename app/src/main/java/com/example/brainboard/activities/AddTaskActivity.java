package com.example.brainboard.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.brainboard.notifications.NotificationReceiver;
import com.example.brainboard.databinding.ActivityAddTaskBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * AddTaskActivity.java
 *
 * This activity allows users to create a new study task in the BrainBoard app.
 * Features:
 * - Users can input tasks via text or voice recognition.
 * - Users can pick a due date and time using a date and time picker dialog.
 * - Tasks are saved to Firebase Firestore under the logged-in user's UID.
 * - A notification is scheduled 1 hour before the task is due using AlarmManager.
 *
 * Firebase:
 * - Firestore is used to store tasks in the format:
 *   users/{uid}/tasks/{taskId}
 *
 * Notifications:
 * - A local notification is triggered via a BroadcastReceiver (NotificationReceiver).
 *
 * Prerequisites:
 * - Firebase must be initialized and user must be authenticated.
 * - Global UID is fetched from MainActivity.getGlobalUid().
 *
 * Dependencies:
 * - ViewBinding (ActivityAddTaskBinding)
 * - Firebase Firestore
 * - AlarmManager for notifications
 * - Android speech recognition APIs
 */


public class AddTaskActivity extends Activity {

    private ActivityAddTaskBinding binding;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 101;
    private final Calendar calendar = Calendar.getInstance();
    private String formattedDateTime = null;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        // Check if UID is available
        String uid = MainActivity.getGlobalUid();
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "UID not set. Please login first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.voiceInputButton.setOnClickListener(view -> startVoiceRecognition());
        binding.pickDateTimeButton.setOnClickListener(view -> showDatePicker());

        binding.saveTaskButton.setOnClickListener(view -> {
            String task = binding.taskInput.getText().toString().trim();
            if (task.isEmpty()) {
                Toast.makeText(this, "Please enter or speak a task", Toast.LENGTH_SHORT).show();
                return;
            }
            if (formattedDateTime == null) {
                Toast.makeText(this, "Please pick due date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            String taskId = UUID.randomUUID().toString();
            saveTaskToFirestore(taskId, task, formattedDateTime);
            scheduleNotification(taskId, task, formattedDateTime);
            Toast.makeText(this, "Task saved!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void saveTaskToFirestore(String taskId, String title, String dueTime) {
        String uid = MainActivity.getGlobalUid();
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "UID not set. Please login.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("taskId", taskId);
        taskMap.put("title", title);
        taskMap.put("dueDateTime", dueTime);
        taskMap.put("completed", false);
        taskMap.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("users")
                .document(uid)
                .collection("tasks")
                .document(taskId)
                .set(taskMap)
                .addOnSuccessListener(unused -> {})
                .addOnFailureListener(e -> e.printStackTrace());
    }

    private void scheduleNotification(String taskId, String taskTitle, String dueTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.getDefault());
            long dueMillis = sdf.parse(dueTime).getTime();
            long triggerTime = dueMillis - 60 * 60 * 1000;

            if (triggerTime <= System.currentTimeMillis()) {
                triggerTime = System.currentTimeMillis() + 3000;
            }

            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("taskTitle", taskTitle);
            intent.putExtra("taskId", taskId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, taskId.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your task...");

        try {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice input not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                binding.taskInput.setText(matches.get(0));
            }
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            showTimePicker();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.getDefault());
            formattedDateTime = sdf.format(calendar.getTime());
            binding.dueTimeText.setText("Due: " + formattedDateTime);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }
}
