package com.example.brainboardmobile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.brainboardmobile.databinding.ActivityTaskListBinding;
import com.example.brainboardmobile.firebase.FirestoreHelper;
import com.example.brainboardmobile.models.TaskModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

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
