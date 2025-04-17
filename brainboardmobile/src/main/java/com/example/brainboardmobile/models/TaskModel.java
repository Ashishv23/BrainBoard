package com.example.brainboardmobile.models;

/**
 * TaskModel.java (Mobile)
 *
 * A data model class representing a study task in the BrainBoard mobile app.
 * Used to map data to and from Firebase Firestore.
 *
 * Fields:
 * - taskId: Unique identifier for the task
 * - title: The task description or title
 * - dueDateTime: Due date and time as a string (Format: "dd/MM/yyyy HH:mm")
 * - completed: Boolean flag indicating whether the task is completed
 *
 * Constructors:
 * - Empty constructor required for Firebase deserialization
 * - Parameterized constructor for easy instantiation
 *
 * Notes:
 * - All fields use standard Java getters and setters
 * - Used across FirestoreHelper, adapters, and UI components
 */

public class TaskModel {
    private String taskId;
    private String title;
    private String dueDateTime; // Format: "dd/MM/yyyy HH:mm"
    private boolean completed;

    // Required empty constructor for Firebase
    public TaskModel() {
    }

    public TaskModel(String taskId, String title, String dueDateTime, boolean completed) {
        this.taskId = taskId;
        this.title = title;
        this.dueDateTime = dueDateTime;
        this.completed = completed;
    }

    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDueDateTime() {
        return dueDateTime;
    }

    public void setDueDateTime(String dueDateTime) {
        this.dueDateTime = dueDateTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
