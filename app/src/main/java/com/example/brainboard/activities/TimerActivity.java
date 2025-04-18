package com.example.brainboard.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;
import android.app.AlertDialog;

import com.example.brainboard.databinding.ActivityTimerBinding;

/**
 * TimerActivity.java
 *
 * This activity provides a 25-minute Pomodoro-style countdown timer to help users stay focused.
 *
 * Features:
 * - Start/Stop button toggles the timer.
 * - Displays countdown in MM:SS format.
 * - Alerts the user with a Toast when the session completes.
 * - Confirms before exiting if the timer is currently running.
 *
 * Timer Logic:
 * - Uses Android's CountDownTimer for precise countdown management.
 * - Resets to 25 minutes after each completion or stop.
 *
 * UI:
 * - Uses ViewBinding (ActivityTimerBinding)
 * - Timer text and button dynamically update based on state.
 *
 * Lifecycle Handling:
 * - Cancels timer onDestroy to avoid memory leaks.
 * - Prompts user via AlertDialog before navigating back while timer is active.
 */


public class TimerActivity extends Activity {

    private ActivityTimerBinding binding;
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private long timeLeftInMillis = 25 * 60 * 1000; // 25 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTimerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        updateTimerDisplay();

        binding.startStopButton.setOnClickListener(v -> {
            if (isRunning) {
                stopTimer();
            } else {
                startTimer();
            }
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                isRunning = false;
                binding.startStopButton.setText("Start");
                Toast.makeText(TimerActivity.this, "Session complete!", Toast.LENGTH_SHORT).show();
                timeLeftInMillis = 25 * 60 * 1000; // Reset
                updateTimerDisplay();
            }
        }.start();

        isRunning = true;
        binding.startStopButton.setText("Stop");
    }

    private void stopTimer() {
        countDownTimer.cancel();
        isRunning = false;
        binding.startStopButton.setText("Start");
        timeLeftInMillis = 25 * 60 * 1000; // Reset
        updateTimerDisplay();
    }

    private void updateTimerDisplay() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        binding.timerText.setText(timeFormatted);
    }

    @Override
    public void onBackPressed() {
        if (isRunning) {
            new AlertDialog.Builder(this)
                    .setTitle("Timer is running")
                    .setMessage("Do you want to stop the timer and go back?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        stopTimer();
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            super.onBackPressed(); // Normal behavior
        }
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}
