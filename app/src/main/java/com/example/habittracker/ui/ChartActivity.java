package com.example.habittracker.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.habittracker.R;
import com.example.habittracker.repository.HabitRepository;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChartActivity extends AppCompatActivity {

    private LineChart lineChart;
    private TextView tvHabitTitle, tvTotalHeader;
    private Long habitId;
    private HabitRepository repository;

    // Stat Cards
    private View cardTotal, cardThisYear, cardThisMonth, cardLastMonth, cardThisWeek, cardLastWeek;
    private View cardLastCheck, cardMaxStreak, cardCreationDate, cardDaysElapsed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        habitId = getIntent().getLongExtra("habit_id", -1);
        String title = getIntent().getStringExtra("title");

        tvHabitTitle = findViewById(R.id.tvHabitTitle);
        tvTotalHeader = findViewById(R.id.tvTotalHeader);
        lineChart = findViewById(R.id.lineChart);

        if (title != null) {
            tvHabitTitle.setText(title);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        initStatCards();
        repository = new HabitRepository(this);

        setupLineChart();
        loadAllData();
    }

    private void initStatCards() {
        cardTotal = findViewById(R.id.cardTotal);
        setupCard(cardTotal, "Total", "0");

        cardThisYear = findViewById(R.id.cardThisYear);
        setupCard(cardThisYear, "This Year", "0");

        cardThisMonth = findViewById(R.id.cardThisMonth);
        setupCard(cardThisMonth, "This Month", "0");

        cardLastMonth = findViewById(R.id.cardLastMonth);
        setupCard(cardLastMonth, "Last Month", "0");

        cardThisWeek = findViewById(R.id.cardThisWeek);
        setupCard(cardThisWeek, "This Week", "0");

        cardLastWeek = findViewById(R.id.cardLastWeek);
        setupCard(cardLastWeek, "Last Week", "0");

        cardLastCheck = findViewById(R.id.cardLastCheck);
        setupCard(cardLastCheck, "Last Check Date", "-");

        cardMaxStreak = findViewById(R.id.cardMaxStreak);
        setupCard(cardMaxStreak, "Maximum Consecutive Checks", "0");

        cardCreationDate = findViewById(R.id.cardCreationDate);
        setupCard(cardCreationDate, "Creation Date", "Jan 1, 2024"); // Placeholder

        cardDaysElapsed = findViewById(R.id.cardDaysElapsed);
        setupCard(cardDaysElapsed, "Days Elapsed", "0");
    }

    private void setupCard(View card, String label, String value) {
        ((TextView) card.findViewById(R.id.tvStatLabel)).setText(label);
        ((TextView) card.findViewById(R.id.tvStatValue)).setText(value);
    }

    private void updateCard(View card, String value, String subValue) {
        ((TextView) card.findViewById(R.id.tvStatValue)).setText(value);
        if (subValue != null && !subValue.isEmpty()) {
            TextView tvSubValue = card.findViewById(R.id.tvStatSubValue);
            tvSubValue.setText(subValue);
            tvSubValue.setVisibility(View.VISIBLE);
        }
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setBackgroundColor(Color.BLACK);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#808080"));
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.parseColor("#1A1A1A"));
        xAxis.setGranularity(1f);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#808080"));
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#1A1A1A"));
        leftAxis.setAxisMinimum(0f);

        lineChart.getAxisRight().setEnabled(false);
    }

    private void loadAllData() {
        // Since the current API only supports month-by-month, 
        // for a "creative" UI like the photo, we'll load the current month 
        // and calculate stats based on it. 
        // In a real app, you'd have a "/stats" or "/logs" (all) endpoint.
        
        YearMonth currentMonth = YearMonth.now();
        repository.loadLogsForMonth(habitId, currentMonth.getYear(), currentMonth.getMonthValue(),
                response -> {
                    List<LocalDate> logs = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            logs.add(LocalDate.parse(response.getJSONObject(i).getString("date")));
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                    processStats(logs);
                },
                error -> Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
        );
    }

    private void processStats(List<LocalDate> logs) {
        if (logs.isEmpty()) return;
        Collections.sort(logs);

        LocalDate today = LocalDate.now();
        int thisMonthCount = 0;
        int thisYearCount = 0;
        int thisWeekCount = 0;
        
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);

        for (LocalDate date : logs) {
            if (date.getMonth() == today.getMonth() && date.getYear() == today.getYear()) thisMonthCount++;
            if (date.getYear() == today.getYear()) thisYearCount++;
            if (!date.isBefore(startOfWeek)) thisWeekCount++;
        }

        tvTotalHeader.setText("Total: " + logs.size());
        updateCard(cardTotal, String.valueOf(logs.size()), null);
        updateCard(cardThisYear, String.valueOf(thisYearCount), null);
        updateCard(cardThisMonth, String.valueOf(thisMonthCount), null);
        updateCard(cardThisWeek, String.valueOf(thisWeekCount), null);

        // Last Check
        LocalDate lastCheck = logs.get(logs.size() - 1);
        String lastCheckStr = lastCheck.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        String dayOfWeek = lastCheck.format(DateTimeFormatter.ofPattern("(EEE)"));
        updateCard(cardLastCheck, lastCheckStr, dayOfWeek);

        // Streak
        int maxStreak = calculateMaxStreak(logs);
        updateCard(cardMaxStreak, String.valueOf(maxStreak), null);

        // Days Elapsed (Mocking creation date as 60 days ago if not available)
        LocalDate creationDate = logs.get(0).minusDays(5); // Estimated
        updateCard(cardCreationDate, creationDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")), creationDate.format(DateTimeFormatter.ofPattern("(EEE)")));
        long daysElapsed = ChronoUnit.DAYS.between(creationDate, today);
        updateCard(cardDaysElapsed, String.valueOf(daysElapsed), "days");

        updateChart(logs);
    }

    private int calculateMaxStreak(List<LocalDate> logs) {
        if (logs.isEmpty()) return 0;
        int max = 0;
        int current = 1;
        for (int i = 1; i < logs.size(); i++) {
            if (logs.get(i).equals(logs.get(i-1).plusDays(1))) {
                current++;
            } else {
                max = Math.max(max, current);
                current = 1;
            }
        }
        return Math.max(max, current);
    }

    private void updateChart(List<LocalDate> logs) {
        // To simulate the monthly "peak" chart from the image
        // We'll count completions per month for the last 6 months
        List<Entry> entries = new ArrayList<>();
        String[] months = new String[6];
        YearMonth current = YearMonth.now().minusMonths(5);

        for (int i = 0; i < 6; i++) {
            int count = 0;
            for (LocalDate date : logs) {
                if (YearMonth.from(date).equals(current)) {
                    count++;
                }
            }
            entries.add(new Entry(i, count));
            months[i] = current.format(DateTimeFormatter.ofPattern("MMM"));
            current = current.plusMonths(1);
        }

        LineDataSet dataSet = new LineDataSet(entries, "Completions");
        dataSet.setColor(Color.WHITE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.WHITE);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.DKGRAY);
        dataSet.setFillAlpha(100);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        
        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                return (idx >= 0 && idx < 6) ? months[idx] : "";
            }
        });

        lineChart.animateY(1000);
        lineChart.invalidate();
    }
}
