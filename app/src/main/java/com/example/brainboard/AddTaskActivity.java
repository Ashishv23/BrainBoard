package com.example.brainboard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.brainboard.databinding.ActivityAddTaskBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AddTaskActivity extends Activity {

    private ActivityAddTaskBinding binding;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Voice Input Button
        binding.voiceInputButton.setOnClickListener(view -> startVoiceRecognition());

        // Save Task Button
        binding.saveTaskButton.setOnClickListener(view -> {
            String task = binding.taskInput.getText().toString().trim();
            if (!task.isEmpty()) {
                saveTask(task);
                Toast.makeText(this, "Task saved: " + task, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Please enter or speak a task", Toast.LENGTH_SHORT).show();
            }
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
                String spokenText = matches.get(0);
                binding.taskInput.setText(spokenText);
            } else {
                Toast.makeText(this, "No speech recognized. Try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveTask(String task) {
        SharedPreferences prefs = getSharedPreferences("taskPrefs", MODE_PRIVATE);
        Set<String> taskSet = prefs.getStringSet("taskList", new HashSet<>());
        Set<String> updatedSet = new HashSet<>(taskSet);
        updatedSet.add(task);
        prefs.edit().putStringSet("taskList", updatedSet).apply();
    }
}
