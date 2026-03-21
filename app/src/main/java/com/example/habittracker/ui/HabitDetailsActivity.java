package com.example.habittracker.ui;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.habittracker.R;
import com.example.habittracker.network.VolleySingleton;
import com.example.habittracker.repository.HabitRepository;
import com.example.habittracker.utils.AppConstants;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kotlin.Unit;

public class HabitDetailsActivity extends AppCompatActivity {

    private Long habitId;

    private TextView tvStreak, tvDaysDone, tvProgress;
    private ProgressBar progressBar;
    private TextView tvMonthName;
    private CalendarView calendarView;
    private Set<LocalDate> completedDates = new HashSet<>();
    private final DateTimeFormatter monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    private HabitRepository habitRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_habit_details);
        habitRepository = new HabitRepository(this);

        habitId = getIntent().getLongExtra("habit_id", -1);

        tvStreak = findViewById(R.id.tvStreak);
        tvDaysDone = findViewById(R.id.tvDaysDone);
        tvProgress = findViewById(R.id.tvProgress);
        progressBar = findViewById(R.id.progressBar);

        tvMonthName = findViewById(R.id.tvMonthName);
        calendarView = findViewById(R.id.calendarView);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ImageButton btnShowChart = findViewById(R.id.btnShowChart);
        btnShowChart.setOnClickListener(v -> {
            Intent intent = new Intent(HabitDetailsActivity.this, ChartActivity.class);
            intent.putExtra("habit_id", habitId);
            startActivity(intent);
        });

        setupCalendar();

    }

    private void setupCalendar() {
        LocalDate today = LocalDate.now();

        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {

            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, CalendarDay day) {

                container.day = day;

                TextView textView = container.textView;
                ImageView checkImage = container.checkImage;

                LocalDate date = day.getDate();

                textView.setText(String.valueOf(date.getDayOfMonth()));

                textView.setBackground(null);

                if (day.getPosition() == DayPosition.MonthDate) {

                    textView.setAlpha(1f);

                    if (completedDates.contains(date)) {
                        checkImage.setVisibility(View.VISIBLE);
                    } else {
                        checkImage.setVisibility(View.GONE);
                    }

                    if (date.equals(today)) {
                        textView.setBackgroundResource(R.drawable.bg_today_border);
                    }

                } else {
                    textView.setAlpha(0.3f);
                    checkImage.setVisibility(View.GONE);
                }
            }
        });

        YearMonth currentMonth = YearMonth.now();

        YearMonth startMonth = YearMonth.of(2000, 1);
        YearMonth endMonth = YearMonth.of(2100, 12);

        calendarView.setup(startMonth, endMonth, DayOfWeek.MONDAY);

        calendarView.scrollToMonth(currentMonth);


        loadCompletionsForMonth(
                currentMonth.getYear(),
                currentMonth.getMonthValue()
        );


        tvMonthName.setText(monthTitleFormatter.format(currentMonth));

        calendarView.setMonthScrollListener(calendarMonth -> {

            YearMonth ym = calendarMonth.getYearMonth();

            tvMonthName.setText(monthTitleFormatter.format(ym));

            loadCompletionsForMonth(
                    ym.getYear(),
                    ym.getMonthValue()
            );

            return Unit.INSTANCE;
        });


    }

    private void loadCompletionsForMonth(int year, int month) {

        habitRepository.loadLogsForMonth(
                habitId,
                year,
                month,

                response -> {

                    completedDates.clear();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String dateStr = obj.getString("date");
                            completedDates.add(LocalDate.parse(dateStr));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    calendarView.notifyCalendarChanged();

                    // 🔥 ADD THIS
                    updateMonthStats(year, month);
                },

                error -> Toast.makeText(this, "Failed to load logs", Toast.LENGTH_SHORT).show()
        );
    }

    private void updateMonthStats(int year, int month) {

        int streak = calculateMonthStreak();

        int totalDays = YearMonth.of(year, month).lengthOfMonth();
        int doneDays = completedDates.size();

        int progress = (int) ((doneDays * 100.0f) / totalDays);

        tvStreak.setText("🔥 Streak: " + streak);
        tvDaysDone.setText("📅 Days done: " + doneDays + " / " + totalDays);
        tvProgress.setText("📊 Progress: " + progress + "%");

        if (progress < 25) {
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
        } else if (progress < 55) {
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
        } else {
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
        }

        animateProgress(progress);
    }
    private void animateProgress(int progress) {

        ObjectAnimator animation = ObjectAnimator.ofInt(
                progressBar,
                "progress",
                progressBar.getProgress(),
                progress
        );

        animation.setDuration(500); // smooth animation
        animation.start();
    }
    private int calculateMonthStreak() {

        if (completedDates.isEmpty()) return 0;

        int streak = 0;

        // 🔥 Start from latest date in this month
        LocalDate latest = completedDates.stream()
                .max(LocalDate::compareTo)
                .orElse(null);

        if (latest == null) return 0;

        LocalDate current = latest;

        while (completedDates.contains(current)) {
            streak++;
            current = current.minusDays(1);
        }

        return streak;
    }
    private void toggleDate(LocalDate date) {

        boolean wasCompleted = completedDates.contains(date);

        String url = AppConstants.HABIT_URL + "/" + habitId +
                "/log?date=" + date.toString();

        int method;

        if (wasCompleted) {
            method = Request.Method.DELETE;
            completedDates.remove(date);
        } else {
            method = Request.Method.POST;
            completedDates.add(date);
        }

        calendarView.notifyDateChanged(date);

        StringRequest request = new StringRequest(
                method,
                url,

                response -> {
                    updateMonthStats(
                            calendarView.findFirstVisibleMonth().getYearMonth().getYear(),
                            calendarView.findFirstVisibleMonth().getYearMonth().getMonthValue()
                    );
                },

                error -> {

                    if (wasCompleted) {
                        completedDates.add(date);
                    } else {
                        completedDates.remove(date);
                    }

                    calendarView.notifyDateChanged(date);

                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildHeaders();
            }
        };

        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private Map<String, String> buildHeaders() {

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + prefs.getString("jwt_token", ""));
        headers.put("Content-Type", "application/json");

        return headers;
    }

    private class DayViewContainer extends ViewContainer {
        TextView textView;
        ImageView checkImage;
        CalendarDay day;

        public DayViewContainer(View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);
            checkImage = view.findViewById(R.id.checkImage);

            view.setOnClickListener(v -> {
                if (day != null && day.getPosition() == DayPosition.MonthDate) {
                    toggleDate(day.getDate());
                }
            });
        }
    }

}
