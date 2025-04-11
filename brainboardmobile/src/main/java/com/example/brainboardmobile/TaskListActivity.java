package com.example.brainboardmobile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.example.brainboardmobile.databinding.ActivityTaskListBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskListActivity extends AppCompatActivity {

    private ActivityTaskListBinding binding;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup RecyclerView
        binding.taskRecyclerView.setLayoutManager(new WearableLinearLayoutManager(this));
        List<String> tasks = loadTasks();
        taskAdapter = new TaskAdapter(tasks, this);
        binding.taskRecyclerView.setAdapter(taskAdapter);

        // Logout button
        binding.logoutButton.setOnClickListener(view -> showLogoutConfirmation());
    }

    private List<String> loadTasks() {
        SharedPreferences prefs = getSharedPreferences("taskPrefs", MODE_PRIVATE);
        Set<String> taskSet = prefs.getStringSet("taskList", new HashSet<>());
        return new ArrayList<>(taskSet);
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
