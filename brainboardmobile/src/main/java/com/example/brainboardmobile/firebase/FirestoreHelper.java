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
