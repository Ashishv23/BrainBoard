package com.example.brainboard.activities;

import android.content.Intent;
import android.content.SharedPreferences;
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

    private ActivityMainBinding binding;
    private static String globalUid;

    private static final String PREF_NAME = "brainboard_prefs";
    private static final String KEY_UID = "firebase_uid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load UID from SharedPreferences on app start
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        globalUid = prefs.getString(KEY_UID, null);

        // Button handlers
        binding.addTaskButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddTaskActivity.class));
        });

        binding.viewTasksButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TaskListActivity.class));
        });

        binding.startTimerButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TimerActivity.class));
        });

        binding.news.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FactActivity.class));
        });

        binding.chartRoot.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ChartActivity.class));
        });

        binding.loginButton.setOnClickListener(v -> showLoginPopup());
    }

    private void showLoginPopup() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setPadding(24, 24, 24, 24);

        BoxInsetLayout boxInsetLayout = new BoxInsetLayout(this);
        boxInsetLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        boxInsetLayout.setPadding(16, 16, 16, 16);

        EditText uidInput = new EditText(this);
        uidInput.setHint("Enter UID");
        uidInput.setSingleLine(true);
        uidInput.setTextSize(20);

        // Show existing UID if available
        uidInput.setText(globalUid != null ? globalUid : "");

        BoxInsetLayout.LayoutParams uidParams = new BoxInsetLayout.LayoutParams(
                BoxInsetLayout.LayoutParams.MATCH_PARENT,
                BoxInsetLayout.LayoutParams.WRAP_CONTENT
        );
        uidParams.gravity = Gravity.CENTER;
        uidInput.setLayoutParams(uidParams);

        boxInsetLayout.addView(uidInput);
        scrollView.addView(boxInsetLayout);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(scrollView)
                .setPositiveButton("Login", (d, which) -> {
                    String uid = uidInput.getText().toString().trim();
                    if (uid.isEmpty()) {
                        Toast.makeText(this, "UID cannot be empty", Toast.LENGTH_SHORT).show();
                    } else {
                        // Save to both global and SharedPreferences
                        globalUid = uid;
                        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                                .edit()
                                .putString(KEY_UID, uid)
                                .apply();
                        Toast.makeText(this, "Saved UID: " + uid, Toast.LENGTH_SHORT).show();
                    }
                })
                .create();

        dialog.show();

        Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (saveButton != null) saveButton.setTextSize(12);
    }

    public static String getGlobalUid() {
        return globalUid;
    }
}
