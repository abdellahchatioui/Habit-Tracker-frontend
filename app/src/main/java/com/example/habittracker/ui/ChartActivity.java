package com.example.habittracker;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.habittracker.repository.HabitRepository;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChartActivity extends AppCompatActivity {

    private LineChart lineChart;
    private Long habitId;
    private HabitRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        habitId = getIntent().getLongExtra("habit_id", -1);

        lineChart = findViewById(R.id.lineChart);
        repository = new HabitRepository(this);

        loadChartData();
    }

    private void loadChartData() {

        YearMonth currentMonth = YearMonth.now();

        repository.loadLogsForMonth(
                habitId,
                currentMonth.getYear(),
                currentMonth.getMonthValue(),

                response -> {

                    List<Entry> entries = new ArrayList<>();

                    Set<Integer> completedDays = new HashSet<>();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            LocalDate date = LocalDate.parse(obj.getString("date"));
                            completedDays.add(date.getDayOfMonth());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    int totalDays = currentMonth.lengthOfMonth();

                    for (int day = 1; day <= totalDays; day++) {
                        float value = completedDays.contains(day) ? 1f : 0f;
                        entries.add(new Entry(day, value));
                    }

                    showChart(entries);

                },

                error -> Toast.makeText(this, "Failed to load chart", Toast.LENGTH_SHORT).show()
        );
    }

    private void showChart(List<Entry> entries) {

        LineDataSet dataSet = new LineDataSet(entries, "Daily Habit");

        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);

        lineChart.setData(lineData);
        lineChart.invalidate();
    }
}