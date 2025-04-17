package com.example.brainboardmobile.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.brainboardmobile.databinding.ActivityAddTaskBinding;
import com.example.brainboardmobile.firebase.FirestoreHelper;
import com.example.brainboardmobile.models.TaskModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

/**
 * AddTaskActivity.java (Mobile)
 *
 * This activity allows users to create or edit a task in the BrainBoard mobile app.
 *
 * Features:
 * - Create new tasks or update existing ones (edit mode via Intent extras)
 * - Supports speech-to-text input for task title (requires RECORD_AUDIO permission)
 * - Date and time pickers for setting task deadlines
 * - Task data is saved or updated in Firebase Firestore via FirestoreHelper
 *
 * UI Components:
 * - taskTitleInput: EditText for task title
 * - dueDateTimeText: TextView displaying selected date and time
 * - btnVoiceInput: Triggers speech recognition input
 * - btnPickDateTime: Launches date and time pickers
 * - btnSaveTask: Saves or updates the task in Firestore
 *
 * Dependencies:
 * - ViewBinding (ActivityAddTaskBinding)
 * - FirestoreHelper for database operations
 * - TaskModel for task data representation
 *
 * Notes:
 * - Handles microphone permission dynamically
 * - Uses UUID for new task IDs
 * - Format used for dueDateTime: "dd/MM/yyyy HH:mm"
 */


public class AddTaskActivity extends AppCompatActivity {

    private ActivityAddTaskBinding binding;
    private final Calendar calendar = Calendar.getInstance();
    private final FirestoreHelper firestoreHelper = new FirestoreHelper();
    private String formattedDateTime = null;
    private String taskIdToEdit = null;

    private final ActivityResultLauncher<Intent> speechLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> results = result.getData()
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (results != null && !results.isEmpty()) {
                        binding.taskTitleInput.setText(results.get(0));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // If this is editing mode, prefill the fields
        Intent intent = getIntent();
        if (intent.hasExtra("taskId")) {
            taskIdToEdit = intent.getStringExtra("taskId");
            binding.taskTitleInput.setText(intent.getStringExtra("title"));
            formattedDateTime = intent.getStringExtra("dueDateTime");
            binding.dueDateTimeText.setText("Due: " + formattedDateTime);
        }

        // Microphone permissions and launch
        binding.btnVoiceInput.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                startVoiceInput();
            } else {
                requestMicPermission();
            }
        });

        binding.btnPickDateTime.setOnClickListener(v -> showDatePicker());

        binding.btnSaveTask.setOnClickListener(v -> {
            if (taskIdToEdit != null) {
                updateTaskToFirestore();
            } else {
                saveTaskToFirestore();
            }
        });
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak task title...");
        speechLauncher.launch(intent);
    }

    private void requestMicPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO}, 1001);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show();
                startVoiceInput();
            } else {
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show();
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
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    formattedDateTime = sdf.format(calendar.getTime());
                    binding.dueDateTimeText.setText("Due: " + formattedDateTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void saveTaskToFirestore() {
        String title = binding.taskTitleInput.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (formattedDateTime == null) {
            Toast.makeText(this, "Please select due date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        TaskModel task = new TaskModel(UUID.randomUUID().toString(), title, formattedDateTime, false);

        firestoreHelper.addTask(task,
                unused -> {
                    Toast.makeText(this, "Task saved!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show()
        );
    }

    private void updateTaskToFirestore() {
        String title = binding.taskTitleInput.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (formattedDateTime == null) {
            Toast.makeText(this, "Please select due date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        TaskModel updatedTask = new TaskModel(taskIdToEdit, title, formattedDateTime, false);

        firestoreHelper.updateTask(updatedTask,
                unused -> {
                    Toast.makeText(this, "Task updated!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show()
        );
    }
}
