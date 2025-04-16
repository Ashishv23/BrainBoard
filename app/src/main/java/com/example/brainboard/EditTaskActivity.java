package com.example.brainboard;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.brainboard.databinding.ActivityEditTaskBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class EditTaskActivity extends Activity {

    private ActivityEditTaskBinding binding;
    private static final int SPEECH_REQUEST_CODE = 200;
    private final Calendar calendar = Calendar.getInstance();

    private String originalTaskEntry;
    private String originalTitle;
    private String formattedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Extract original data
        originalTaskEntry = getIntent().getStringExtra("oldTask");
        if (originalTaskEntry != null && originalTaskEntry.contains("||")) {
            String[] parts = originalTaskEntry.split("\\|\\|");
            originalTitle = parts[0];
            formattedDateTime = parts[1];
            binding.taskEditInput.setText(originalTitle);
            binding.dueTimeText.setText("Due: " + formattedDateTime);
        }

        // Voice input
        binding.voiceEditButton.setOnClickListener(v -> startVoiceRecognition());

        // Date-Time picker
        binding.pickDateTimeButton.setOnClickListener(v -> showDatePicker());

        // Save button
        binding.saveEditedTaskButton.setOnClickListener(v -> {
            String updatedTitle = binding.taskEditInput.getText().toString().trim();

            if (updatedTitle.isEmpty()) {
                Toast.makeText(this, "Task cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (formattedDateTime == null) {
                Toast.makeText(this, "Please select a due time", Toast.LENGTH_SHORT).show();
                return;
            }

            updateTask(originalTaskEntry, updatedTitle + "||" + formattedDateTime);
            scheduleNotification(updatedTitle, formattedDateTime);
            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your updated task...");

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Speech input not supported", Toast.LENGTH_SHORT).show();
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
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    showTimePicker();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hour, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
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

    private void updateTask(String oldTask, String newTask) {
        SharedPreferences prefs = getSharedPreferences("taskPrefs", MODE_PRIVATE);
        Set<String> taskSet = prefs.getStringSet("taskList", new HashSet<>());
        Set<String> updatedSet = new HashSet<>(taskSet);

        updatedSet.remove(oldTask);
        updatedSet.add(newTask);

        prefs.edit().putStringSet("taskList", updatedSet).apply();
    }

    private void scheduleNotification(String taskTitle, String dueTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.getDefault());
            long dueMillis = sdf.parse(dueTime).getTime();
            long triggerTime = dueMillis - (60 * 60 * 1000); // 1 hour before

            if (triggerTime <= System.currentTimeMillis()) {
                triggerTime = System.currentTimeMillis() + 5000; // fallback: 5s later
            }

            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("taskTitle", taskTitle);
            intent.putExtra("taskId", String.valueOf(taskTitle.hashCode()));

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, taskTitle.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
