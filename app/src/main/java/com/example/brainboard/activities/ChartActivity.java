package com.example.brainboard.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.brainboard.databinding.ActivityChartBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ChartActivity extends Activity {

    private ActivityChartBinding binding;
    private FirebaseFirestore db;
    private final HashMap<String, Integer> dateCountMap = new HashMap<>();
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayFormatter = new SimpleDateFormat("dd/MM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        db = FirebaseFirestore.getInstance();
        fetchTasks();
    }

    private void fetchTasks() {
        String uid = MainActivity.getGlobalUid();

        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "UID not set. Please login first.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(uid)
                .collection("tasks")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Log.d("FirestoreDoc", doc.getData().toString());
                        String due = doc.getString("dueDateTime");
                        if (due != null) {
                            try {
                                String[] parts = due.split(" "); // split at space between date and time
                                String dateOnly = parts[0]; // "dd/MM/yyyy"

                                // Reformat from dd/MM/yyyy to dd/MM
                                String[] dateParts = dateOnly.split("/");
                                String dayMonth = dateParts[0] + "/" + dateParts[1]; // "dd/MM"

                                dateCountMap.put(dayMonth, dateCountMap.getOrDefault(dayMonth, 0) + 1);
                            } catch (Exception e) {
                                Log.e("ChartParse", "Date parse error: " + due, e);
                            }
                        }


                    }
                    Log.d("ChartData", "Date Count Map: " + dateCountMap.toString());

                    drawBarChart();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch tasks", Toast.LENGTH_SHORT).show();
                    Log.e("ChartError", "Firestore error", e);
                });
    }

    private void drawBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int index = 0;
        for (String date : dateCountMap.keySet()) {
            entries.add(new BarEntry(index, dateCountMap.get(date)));
            labels.add(date);
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);

        BarChart chart = binding.barChart;
        chart.setData(data);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45f);

        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextSize(1f);
        chart.getLegend().setForm(Legend.LegendForm.CIRCLE);

        chart.setFitBars(true);
        chart.invalidate(); // refresh
    }

}
