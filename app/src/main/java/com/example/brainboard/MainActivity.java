package com.example.brainboard;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.brainboard.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; // ViewBinding variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout using ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Button click handlers
        binding.addTaskButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddTaskActivity.class));
        });

        binding.viewTasksButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TaskListActivity.class));
        });

        binding.startTimerButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TimerActivity.class));
        });
    }
}
