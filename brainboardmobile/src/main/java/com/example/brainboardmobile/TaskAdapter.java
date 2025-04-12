package com.example.brainboardmobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brainboardmobile.databinding.ItemTaskBinding;
import com.example.brainboardmobile.firebase.FirestoreHelper;
import com.example.brainboardmobile.models.TaskModel;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<TaskModel> taskList;
    private final Context context;
    private final FirestoreHelper firestoreHelper;

    public TaskAdapter(List<TaskModel> taskList, Context context, FirestoreHelper firestoreHelper) {
        this.taskList = taskList;
        this.context = context;
        this.firestoreHelper = firestoreHelper;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        ItemTaskBinding binding;

        public TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TaskModel task, Context context, FirestoreHelper firestoreHelper, TaskAdapter adapter, int position) {
            binding.taskText.setText(task.getTitle());

            binding.editTaskButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, AddTaskActivity.class);
                intent.putExtra("taskId", task.getTaskId());
                intent.putExtra("title", task.getTitle());
                intent.putExtra("dueDateTime", task.getDueDateTime());
                context.startActivity(intent);
            });

            binding.deleteTaskButton.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to delete this task?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            firestoreHelper.deleteTask(task.getTaskId(),
                                    unused -> {
                                        adapter.taskList.remove(position);
                                        adapter.notifyItemRemoved(position);
                                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
                                    },
                                    e -> Toast.makeText(context, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(taskList.get(position), context, firestoreHelper, this, position);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
