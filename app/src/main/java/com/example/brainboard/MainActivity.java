package com.example.brainboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.wear.widget.BoxInsetLayout;

import com.example.brainboard.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; // ViewBinding variable
    private static String globalUid; // Global UID variable

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

        // Handle loginButton click
        binding.loginButton.setOnClickListener(v -> showLoginPopup());
    }

    private void showLoginPopup() {
        // ScrollView to handle round screens safely
        ScrollView scrollView = new ScrollView(this);
        scrollView.setPadding(24, 24, 24, 24);

        // BoxInsetLayout as container
        androidx.wear.widget.BoxInsetLayout boxInsetLayout = new androidx.wear.widget.BoxInsetLayout(this);
        boxInsetLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        boxInsetLayout.setPadding(16, 16, 16, 16);

        // Create EditText for UID input
        EditText uidInput = new EditText(this);
        uidInput.setHint("Enter UID");
        uidInput.setSingleLine(true);
        uidInput.setTextSize(20);

        // Set centered layout params
        BoxInsetLayout.LayoutParams uidParams = new BoxInsetLayout.LayoutParams(
                BoxInsetLayout.LayoutParams.MATCH_PARENT,
                BoxInsetLayout.LayoutParams.WRAP_CONTENT
        );
        uidParams.gravity = Gravity.CENTER;
        uidInput.setLayoutParams(uidParams);

        // Add to BoxInsetLayout
        boxInsetLayout.addView(uidInput);
        scrollView.addView(boxInsetLayout);

        // AlertDialog with Save button only
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(scrollView)
                .setPositiveButton("Login", (d, which) -> {
                    String uid = uidInput.getText().toString().trim();
                    if (uid.isEmpty()) {
                        Toast.makeText(this, "UID cannot be empty", Toast.LENGTH_SHORT).show();
                    } else {
                        saveUidGlobally(uid);
                        Toast.makeText(this, "Saved UID: " + uid, Toast.LENGTH_SHORT).show();
                    }
                })
                .create();

        dialog.show();

        // Make Save button smaller
        Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (saveButton != null) saveButton.setTextSize(12);
    }

    private void saveUidGlobally(String uid) {
        globalUid = uid; // Save UID globally
    }

    public static String getGlobalUid() {
        return globalUid; // Provide access to the global UID
    }
}