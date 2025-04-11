package com.example.brainboard;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.example.brainboard.databinding.ActivityTaskListBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskListActivity extends Activity {

    private ActivityTaskListBinding binding;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set LayoutManager
        binding.taskRecyclerView.setLayoutManager(new WearableLinearLayoutManager(this));

        // Load tasks and display
        List<String> tasks = loadTasks();
        taskAdapter = new TaskAdapter(tasks,this);
        binding.taskRecyclerView.setAdapter(taskAdapter);
    }

    private List<String> loadTasks() {
        SharedPreferences prefs = getSharedPreferences("taskPrefs", MODE_PRIVATE);
        Set<String> taskSet = prefs.getStringSet("taskList", new HashSet<>());
        return new ArrayList<>(taskSet);
    }
}
