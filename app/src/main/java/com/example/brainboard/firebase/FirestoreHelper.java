package com.example.brainboard.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.brainboard.models.TaskModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * FirestoreHelper.java
 *
 * A utility class that encapsulates all Firebase Firestore operations for task management
 * in the BrainBoard app.
 *
 * Features:
 * - Fetches tasks from Firestore and maps them to TaskModel objects
 * - Adds or updates a task in Firestore
 * - Deletes a task from Firestore
 *
 * Usage:
 * - Requires access to a valid Firebase UID stored in SharedPreferences ("wearPrefs")
 * - Methods provide callbacks for success handling (Consumer for fetch, Runnable for others)
 *
 * Dependencies:
 * - Firebase Firestore
 * - SharedPreferences for UID access
 * - TaskModel (custom model class for task objects)
 *
 * Notes:
 * - The `updateTask()` method is an alias for `addTask()` (since Firestore `.set()` overwrites)
 * - Gracefully handles null UID scenarios and logs errors
 */


public class FirestoreHelper {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Context context;

    public FirestoreHelper(Context context) {
        this.context = context;
    }

    private String getUid() {
        SharedPreferences prefs = context.getSharedPreferences("wearPrefs", Context.MODE_PRIVATE);
        return prefs.getString("firebase_uid", null);
    }

    public void fetchTasks(Consumer<List<TaskModel>> callback) {
        String uid = getUid();
        if (uid == null) {
            Log.e("FirestoreHelper", "UID is null");
            callback.accept(new ArrayList<>());
            return;
        }

        db.collection("users").document(uid).collection("tasks")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<TaskModel> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        list.add(doc.toObject(TaskModel.class));
                    }
                    callback.accept(list);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreHelper", "Failed to fetch tasks", e);
                    callback.accept(new ArrayList<>());
                });
    }

    public void addTask(TaskModel task, Runnable onSuccess) {
        String uid = getUid();
        if (uid == null) return;

        db.collection("users").document(uid)
                .collection("tasks").document(task.getTaskId())
                .set(task)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreHelper", "Task added");
                    onSuccess.run();
                });
    }

    public void updateTask(TaskModel task, Runnable onSuccess) {
        addTask(task, onSuccess); // same logic as add
    }

    public void deleteTask(TaskModel task, Runnable onSuccess) {
        String uid = getUid();
        if (uid == null) return;

        db.collection("users").document(uid)
                .collection("tasks").document(task.getTaskId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreHelper", "Task deleted");
                    onSuccess.run();
                });
    }
}
