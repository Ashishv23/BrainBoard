package com.example.brainboard;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.brainboard.databinding.ActivityAddTaskBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class AddTaskActivity extends Activity {

    private ActivityAddTaskBinding binding;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 101;
    private final Calendar calendar = Calendar.getInstance();
    private String formattedDateTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Voice Input Button
        binding.voiceInputButton.setOnClickListener(view -> startVoiceRecognition());

        // Date & Time Picker
        binding.pickDateTimeButton.setOnClickListener(v -> showDatePicker());

        // Save Task
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
            saveTaskLocally(task, formattedDateTime);
            scheduleNotification(taskId, task, formattedDateTime);
            Toast.makeText(this, "Task saved!", Toast.LENGTH_SHORT).show();
            finish();
        });
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
            } else {
                Toast.makeText(this, "No speech recognized. Try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePicker();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.getDefault());
                    formattedDateTime = sdf.format(calendar.getTime());
                    binding.dueTimeText.setText("Due: " + formattedDateTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void saveTaskLocally(String taskTitle, String dueDateTime) {
        SharedPreferences prefs = getSharedPreferences("taskPrefs", MODE_PRIVATE);
        Set<String> taskSet = prefs.getStringSet("taskList", new HashSet<>());
        Set<String> updatedSet = new HashSet<>(taskSet);
        updatedSet.add(taskTitle + "||" + dueDateTime);
        prefs.edit().putStringSet("taskList", updatedSet).apply();
    }

    private void scheduleNotification(String taskId, String taskTitle, String dueTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.getDefault());
            long dueMillis = sdf.parse(dueTime).getTime();
            long triggerTime = dueMillis - 60 * 60 * 1000; // 1 hour before

            if (triggerTime <= System.currentTimeMillis()) {
                triggerTime = System.currentTimeMillis() + 3000; // 3 sec later (for test/demo)
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
}
