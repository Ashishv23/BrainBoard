package com.example.brainboardmobile.activities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.brainboardmobile.adapters.TaskAdapter;
import com.example.brainboardmobile.databinding.ActivityTaskListBinding;
import com.example.brainboardmobile.firebase.FirestoreHelper;
import com.example.brainboardmobile.models.TaskModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskListActivity.java (Mobile)
 *
 * Displays a real-time list of user tasks in the BrainBoard mobile app.
 *
 * Features:
 * - Uses Firestore snapshot listener to reflect task changes (add, edit, delete) in real-time.
 * - Allows users to:
 *     - View their UID and copy it to the clipboard
 *     - Add new tasks via AddTaskActivity
 *     - Log out securely with confirmation
 *
 * UI Components:
 * - RecyclerView for displaying tasks using TaskAdapter
 * - UID TextView (tap to copy UID)
 * - Add Task button
 * - Logout button with AlertDialog confirmation
 *
 * Data Handling:
 * - Uses FirestoreHelper to connect to Firebase Firestore
 * - Updates taskList and notifies TaskAdapter on Firestore document changes (ADDED, MODIFIED, REMOVED)
 *
 * Dependencies:
 * - ViewBinding (ActivityTaskListBinding)
 * - FirebaseAuth for current user context and logout
 * - FirestoreHelper and TaskModel for backend interaction
 *
 * Notes:
 * - Uses `FirebaseAuth.getInstance().getCurrentUser().getUid()` to fetch and show UID
 */


public class TaskListActivity extends AppCompatActivity {

    private ActivityTaskListBinding binding;
    private TaskAdapter taskAdapter;
    private final List<TaskModel> taskList = new ArrayList<>();
    private final FirestoreHelper firestoreHelper = new FirestoreHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up RecyclerView
        binding.taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, this, firestoreHelper);
        binding.taskRecyclerView.setAdapter(taskAdapter);


    // Display UID
    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    binding.uidTextView.setText("UID: " + uid);

    // Copy UID to clipboard on click
    binding.uidTextView.setOnClickListener(v -> {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("User UID", uid);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "UID copied to clipboard", Toast.LENGTH_SHORT).show();
    });

        // Load tasks in real-time
        fetchTasksRealtime();

        // Add Task button (assuming it's present in layout)
        binding.addTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTaskActivity.class);
            startActivity(intent);
        });

        // Logout button
        binding.logoutButton.setOnClickListener(view -> showLogoutConfirmation());
    }

    private void fetchTasksRealtime() {
        firestoreHelper.getUserTasksCollection().addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(TaskListActivity.this, "Error loading tasks", Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                for (DocumentChange doc : value.getDocumentChanges()) {
                    TaskModel task = doc.getDocument().toObject(TaskModel.class);

                    switch (doc.getType()) {
                        case ADDED:
                            taskList.add(task);
                            break;

                        case MODIFIED:
                            for (int i = 0; i < taskList.size(); i++) {
                                if (taskList.get(i).getTaskId().equals(task.getTaskId())) {
                                    taskList.set(i, task);
                                    break;
                                }
                            }
                            break;

                        case REMOVED:
                            taskList.removeIf(t -> t.getTaskId().equals(task.getTaskId()));
                            break;
                    }
                }
                taskAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("No", null)
                .show();
    }
}
