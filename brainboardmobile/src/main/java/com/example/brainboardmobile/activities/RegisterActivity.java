package com.example.brainboardmobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.brainboardmobile.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;

/**
 * RegisterActivity.java (Mobile)
 *
 * Handles new user registration for the BrainBoard mobile app using Firebase Authentication.
 *
 * Features:
 * - Allows users to register with an email and password.
 * - Automatically logs in and redirects to TaskListActivity upon successful registration.
 * - Displays appropriate error messages for input validation and registration failures.
 *
 * UI Components:
 * - emailInput: EditText for user email
 * - passwordInput: EditText for user password
 * - registerButton: Triggers Firebase account creation
 *
 * Dependencies:
 * - ViewBinding (ActivityRegisterBinding)
 * - Firebase Authentication (FirebaseAuth)
 *
 * Notes:
 * - Validates that both fields are filled using `TextUtils.isEmpty`.
 */


public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        binding.registerButton.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, TaskListActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }
}
