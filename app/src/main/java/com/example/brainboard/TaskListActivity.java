package com.example.brainboard;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.wear.widget.WearableLinearLayoutManager;

import com.example.brainboard.databinding.ActivityTaskListBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

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
