package com.example.brainboard.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.wear.widget.WearableLinearLayoutManager;

import com.example.brainboard.adapters.TaskAdapter;
import com.example.brainboard.databinding.ActivityTaskListBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskListActivity.java
 *
 * Displays a scrollable list of the user's tasks in the BrainBoard app using a WearOS-friendly UI.
 *
 * Features:
 * - Fetches task data from Firebase Firestore under the current user's UID.
 * - Each task entry includes title, due date/time, and task ID.
 * - Shows tasks in reverse chronological order (most recent first).
 * - Uses a custom RecyclerView adapter (TaskAdapter) for display.
 * - Displays a message when there are no tasks to show.
 *
 * UI:
 * - RecyclerView with WearableLinearLayoutManager for optimized WearOS interaction.
 * - Text view fallback when no tasks exist.
 *
 * Data Format:
 * - Tasks are loaded into a list as formatted strings: "title||dueDateTime||taskId"
 *
 * Dependencies:
 * - ViewBinding (ActivityTaskListBinding)
 * - Firebase Firestore
 * - FirebaseApp initialization
 * - Wearable UI components (WearableLinearLayoutManager)
 *
 * Prerequisites:
 * - User must be authenticated; UID is obtained from MainActivity.getGlobalUid()
 */


public class TaskListActivity extends Activity {

    private ActivityTaskListBinding binding;
    private TaskAdapter taskAdapter;
    private final List<String> taskList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        binding.taskRecyclerView.setLayoutManager(new WearableLinearLayoutManager(this));

        taskAdapter = new TaskAdapter(taskList, this);
        binding.taskRecyclerView.setAdapter(taskAdapter);

        fetchTasksFromFirestore();
    }

    @Override
    protected void onResume(){
        super.onResume();
        fetchTasksFromFirestore();
    }

    private void fetchTasksFromFirestore() {
        String uid = MainActivity.getGlobalUid();

        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "UID not set. Please login first.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(uid)
                .collection("tasks").orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    taskList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String title = doc.getString("title");
                        String due = doc.getString("dueDateTime");
                        String taskId = doc.getString("taskId");

                        if (title != null && due != null && taskId != null) {
                            String entry = title + "||" + due + "||" + taskId;
                            taskList.add(entry);
                            Log.d("FirebaseTask", "Loaded: " + entry);
                        } else {
                            Log.w("FirebaseTask", "Missing fields in: " + doc.getId());
                        }
                    }

                    if (taskList.isEmpty()) {
                        binding.noTasksText.setVisibility(View.VISIBLE);
                        binding.taskRecyclerView.setVisibility(View.GONE);
                    } else {
                        binding.noTasksText.setVisibility(View.GONE);
                        binding.taskRecyclerView.setVisibility(View.VISIBLE);
                    }

                    taskAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching tasks", Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseTask", "Fetch error", e);
                });

    }
}
