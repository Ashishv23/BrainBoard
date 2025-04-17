package com.example.brainboardmobile.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.brainboardmobile.models.TaskModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.UUID;

/**
 * FirestoreHelper.java (Mobile)
 *
 * A helper class to manage all Firestore interactions for task operations in the BrainBoard mobile app.
 *
 * Features:
 * - Provides access to the authenticated user's task collection in Firestore.
 * - Adds, updates, deletes, and marks tasks as completed.
 * - Supports real-time task fetching via Firestore snapshot listeners.
 *
 * Methods:
 * - getUserTasksCollection(): Returns the Firestore CollectionReference for the current user's tasks.
 * - addTask(): Adds a new task or overwrites an existing one by ID.
 * - deleteTask(): Deletes a task by its ID.
 * - updateTask(): Rewrites a task document with updated information.
 * - fetchAllTasks(): Attaches a snapshot listener to keep task list updated in real-time.
 * - markTaskCompleted(): Updates the `completed` status field of a task.
 *
 * Dependencies:
 * - FirebaseFirestore for backend storage
 * - FirebaseAuth to identify the current user
 * - TaskModel as the data structure for tasks
 *
 * Notes:
 * - All operations use Firestore's async task listeners for success and failure handling.
 * - Ensures that each task has a unique ID (UUID generated if not provided).
 * - Assumes the user is authenticated and UID is always available (no null checks for `getCurrentUser()`).
 */


public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public CollectionReference getUserTasksCollection() {
        String uid = auth.getCurrentUser().getUid();
        return db.collection("users").document(uid).collection("tasks");
    }

    public void addTask(TaskModel task, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        if (task.getTaskId() == null) {
            task.setTaskId(UUID.randomUUID().toString());
        }

        getUserTasksCollection()
                .document(task.getTaskId())
                .set(task)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void deleteTask(String taskId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        getUserTasksCollection()
                .document(taskId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void updateTask(TaskModel task, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        getUserTasksCollection()
                .document(task.getTaskId())
                .set(task)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void fetchAllTasks(
            EventListener<QuerySnapshot> listener
    ) {
        getUserTasksCollection()
                .orderBy("dueDateTime")
                .addSnapshotListener(listener);
    }

    public void markTaskCompleted(String taskId, boolean completed,
                                  OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        getUserTasksCollection()
                .document(taskId)
                .update("completed", completed)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
}
