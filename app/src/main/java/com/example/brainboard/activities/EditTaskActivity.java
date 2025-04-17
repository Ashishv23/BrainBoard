package com.example.brainboard.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.brainboard.notifications.NotificationReceiver;
import com.example.brainboard.databinding.ActivityEditTaskBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * EditTaskActivity.java
 *
 * This activity allows users to edit an existing task in the BrainBoard app.
 * Features:
 * - Loads the original task data (title, due time, task ID) passed via Intent.
 * - Lets users update task details via text or voice input.
 * - Users can reschedule the task's due date and time using date & time pickers.
 * - Updates both local SharedPreferences and remote Firestore.
 * - Reschedules notification to trigger 1 hour before the updated due time.
 *
 * Task Format:
 * - Passed and saved in the format: "title||dueDateTime||taskId"
 *
 * Dependencies:
 * - ViewBinding (ActivityEditTaskBinding)
 * - Firebase Firestore
 * - SharedPreferences (for local task cache)
 * - AlarmManager and BroadcastReceiver for notifications
 *
 * Prerequisites:
 * - User must be authenticated and UID must be available from MainActivity.getGlobalUid()
 */


public class EditTaskActivity extends Activity {

    private ActivityEditTaskBinding binding;
    private static final int SPEECH_REQUEST_CODE = 200;
    private final Calendar calendar = Calendar.getInstance();

    private String originalTaskEntry;
    private String originalTitle;
    private String originalDueTime;
    private String originalTaskId;
    private String formattedDateTime;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        // Extract original data
        originalTaskEntry = getIntent().getStringExtra("oldTask");
        if (originalTaskEntry != null && originalTaskEntry.contains("||")) {
            String[] parts = originalTaskEntry.split("\\|\\|");
            originalTitle = parts[0];
            originalDueTime = parts.length >= 2 ? parts[1] : "";
            originalTaskId = parts.length >= 3 ? parts[2] : String.valueOf(originalTitle.hashCode());
            formattedDateTime = originalDueTime;
            binding.taskEditInput.setText(originalTitle);
            binding.dueTimeText.setText("Due: " + formattedDateTime);
        }

        // Voice input
        binding.voiceEditButton.setOnClickListener(v -> startVoiceRecognition());

        // Time Picker
        binding.pickDateTimeButton.setOnClickListener(v -> showDatePicker());

        // Save
        binding.saveEditedTaskButton.setOnClickListener(v -> {
            String updatedTitle = binding.taskEditInput.getText().toString().trim();

            if (updatedTitle.isEmpty()) {
                Toast.makeText(this, "Task title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (formattedDateTime == null || formattedDateTime.isEmpty()) {
                Toast.makeText(this, "Please select a due time", Toast.LENGTH_SHORT).show();
                return;
            }

            String updatedEntry = updatedTitle + "||" + formattedDateTime + "||" + originalTaskId;
            updateTaskLocally(originalTaskEntry, updatedEntry);
            updateTaskInFirestore(updatedTitle, formattedDateTime);
            scheduleNotification(updatedTitle, formattedDateTime);

            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void updateTaskLocally(String oldTask, String newTask) {
        SharedPreferences prefs = getSharedPreferences("taskPrefs", MODE_PRIVATE);
        Set<String> taskSet = prefs.getStringSet("taskList", new HashSet<>());
        Set<String> updatedSet = new HashSet<>(taskSet);
        updatedSet.remove(oldTask);
        updatedSet.add(newTask);
        prefs.edit().putStringSet("taskList", updatedSet).apply();
    }

    private void updateTaskInFirestore(String title, String dueTime) {
        String uid = MainActivity.getGlobalUid();
        if (uid == null || uid.isEmpty()) return;

        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("title", title);
        taskMap.put("dueDateTime", dueTime);
        taskMap.put("taskId", originalTaskId);
        taskMap.put("completed", false);

        db.collection("users")
                .document(uid)
                .collection("tasks")
                .document(originalTaskId)
                .set(taskMap)
                .addOnSuccessListener(unused -> Log.d("EditTask", "Firestore task updated"))
                .addOnFailureListener(e -> Log.e("EditTask", "Firestore update failed", e));
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your updated task...");
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice input not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                binding.taskEditInput.setText(results.get(0));
            }
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            showTimePicker();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hour, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.getDefault());
            formattedDateTime = sdf.format(calendar.getTime());
            binding.dueTimeText.setText("Due: " + formattedDateTime);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void scheduleNotification(String taskTitle, String dueTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.getDefault());
            long dueMillis = sdf.parse(dueTime).getTime();
            long triggerTime = dueMillis - (60 * 60 * 1000);
            if (triggerTime <= System.currentTimeMillis()) {
                triggerTime = System.currentTimeMillis() + 5000;
            }

            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("taskTitle", taskTitle);
            intent.putExtra("taskId", originalTaskId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, originalTaskId.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
