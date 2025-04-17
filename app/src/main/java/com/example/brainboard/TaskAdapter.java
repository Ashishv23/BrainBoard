package com.example.brainboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brainboard.databinding.ItemTaskBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<String> taskList;
    private final Context context;

    public TaskAdapter(List<String> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        ItemTaskBinding binding;

        public TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String task, int position, List<String> taskList, Context context, TaskAdapter adapter) {
            String[] parts = task.split("\\|\\|");
            String title = parts[0];
            String dueTime = parts.length > 1 ? parts[1] : "No Time";
            String taskId = parts.length > 2 ? parts[2] : String.valueOf(title.hashCode());

            binding.taskText.setText(title + "\nDue: " + dueTime);

            binding.editTaskButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditTaskActivity.class);
                intent.putExtra("oldTask", task);
                context.startActivity(intent);
            });

            binding.deleteTaskButton.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(context)
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to delete this task?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            SharedPreferences prefs = context.getSharedPreferences("taskPrefs", Context.MODE_PRIVATE);
                            Set<String> taskSet = prefs.getStringSet("taskList", new HashSet<>());
                            Set<String> updatedSet = new HashSet<>(taskSet);

                            updatedSet.remove(task);
                            prefs.edit().putStringSet("taskList", updatedSet).apply();

                            taskList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(taskList.get(position), position, taskList, context, this);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
