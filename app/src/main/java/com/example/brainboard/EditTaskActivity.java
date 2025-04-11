package com.example.brainboard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.brainboard.databinding.ActivityEditTaskBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class EditTaskActivity extends Activity {

    private ActivityEditTaskBinding binding;
    private String oldTask;
    private static final int SPEECH_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        oldTask = getIntent().getStringExtra("oldTask");

        if (oldTask != null) {
            binding.taskEditInput.setText(oldTask);
        }

        // Save edited task
        binding.saveEditedTaskButton.setOnClickListener(v -> {
            String newTask = binding.taskEditInput.getText().toString().trim();
            if (newTask.isEmpty()) {
                Toast.makeText(this, "Task cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                updateTask(oldTask, newTask);
                Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Voice input button
        binding.voiceEditButton.setOnClickListener(v -> startVoiceRecognition());
    }

    private void updateTask(String oldTask, String newTask) {
        SharedPreferences prefs = getSharedPreferences("taskPrefs", MODE_PRIVATE);
        Set<String> taskSet = prefs.getStringSet("taskList", new HashSet<>());
        Set<String> updatedSet = new HashSet<>(taskSet);

        updatedSet.remove(oldTask);
        updatedSet.add(newTask);

        prefs.edit().putStringSet("taskList", updatedSet).apply();
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
            Toast.makeText(this, "Speech input not supported on this device", Toast.LENGTH_SHORT).show();
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
}
