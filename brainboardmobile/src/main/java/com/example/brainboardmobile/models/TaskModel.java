package com.example.brainboardmobile.models;

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
