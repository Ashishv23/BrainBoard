package com.example.brainboard.models;

public class TaskModel {
    private String taskId;
    private String title;
    private String dueDateTime;
    private boolean completed;

    public TaskModel() {}  // Required by Firestore

    public TaskModel(String taskId, String title, String dueDateTime, boolean completed) {
        this.taskId = taskId;
        this.title = title;
        this.dueDateTime = dueDateTime;
        this.completed = completed;
    }

    // Getters and setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDueDateTime() { return dueDateTime; }
    public void setDueDateTime(String dueDateTime) { this.dueDateTime = dueDateTime; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
