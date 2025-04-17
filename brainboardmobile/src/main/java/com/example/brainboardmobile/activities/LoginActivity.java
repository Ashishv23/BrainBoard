package com.example.brainboardmobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.brainboardmobile.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

/**
 * LoginActivity.java (Mobile)
 *
 * Handles user login for the BrainBoard mobile app using Firebase Authentication.
 *
 * Features:
 * - Allows users to log in with email and password.
 * - Redirects to TaskListActivity upon successful login.
 * - Redirects to RegisterActivity for new account creation.
 * - Automatically skips login screen if the user is already authenticated.
 *
 * UI Components:
 * - emailInput: EditText for user email
 * - passwordInput: EditText for user password
 * - loginButton: Triggers Firebase sign-in
 * - registerRedirect: Opens RegisterActivity for new users
 *
 * Dependencies:
 * - ViewBinding (ActivityLoginBinding)
 * - Firebase Authentication (FirebaseAuth)
 *
 * Notes:
 * - Uses `TextUtils.isEmpty` for input validation.
 */


public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        // If user already logged in, skip login screen
        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(this, TaskListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        // Login button click
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, TaskListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // Register redirect
        binding.registerRedirect.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }
}
