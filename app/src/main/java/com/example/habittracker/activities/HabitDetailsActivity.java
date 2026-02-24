package com.example.habittracker.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.habittracker.R;
import com.example.habittracker.network.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HabitDetailsActivity extends AppCompatActivity {

    private Long habitId;
    private EditText etTitle, etDescription, etFrequency;
    private CheckBox checkCompleted;
    private Button btnTrackDay;

    private CalendarView calendarView;
    private List<String> completedDates = new ArrayList<>();

    private int currentYear;
    private int currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_details);

        habitId = getIntent().getLongExtra("habit_id", -1);
        calendarView = findViewById(R.id.calendarView);
        etTitle = findViewById(R.id.etTitle);

        etDescription = findViewById(R.id.etDescription);
        etFrequency = findViewById(R.id.etFrequency);
        checkCompleted = findViewById(R.id.checkCompleted);
        btnTrackDay = findViewById(R.id.btnTrackDay);
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(12);
        YearMonth endMonth = currentMonth.plusMonths(12);

        calendarView.setup(startMonth, endMonth, DayOfWeek.MONDAY);
        calendarView.scrollToMonth(currentMonth);

        etTitle.setText(getIntent().getStringExtra("title"));
        etDescription.setText(getIntent().getStringExtra("description"));
        etFrequency.setText(getIntent().getStringExtra("frequency"));
        checkCompleted.setChecked(getIntent().getBooleanExtra("completed", false));

        findViewById(R.id.btnUpdate).setOnClickListener(v -> updateHabit());
        findViewById(R.id.btnDelete).setOnClickListener(v -> deleteHabit());
        btnTrackDay.setOnClickListener(v -> openDatePicker());

        LocalDate now = LocalDate.now();
        this.currentYear = now.getYear();
        this.currentMonth = now.getMonthValue();
        setupCalendar();
        loadMonthLogs();


        calendarView.notifyCalendarChanged();

    }

    private Map<String, String> getHeaders() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + prefs.getString("jwt_token", ""));
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private void setupCalendar() {

        calendarView = findViewById(R.id.calendarView);
        calendarView.setDayViewResource(R.layout.calendar_day_layout);

        YearMonth currentMonth = null;
        currentMonth = YearMonth.now();


        calendarView.setup(
                currentMonth.minusMonths(1),
                currentMonth.plusMonths(1),
                    DayOfWeek.MONDAY
        );


        calendarView.scrollToMonth(currentMonth);

        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @Override
            public DayViewContainer create(View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(DayViewContainer container, CalendarDay day) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    container.dayText.setText(
                            String.valueOf(day.getDate().getDayOfMonth())
                    );
                }
            }
        });
    }
    static class DayViewContainer extends ViewContainer {

        TextView dayText;
        View completedIndicator;

        public DayViewContainer(View view) {
            super(view);
            dayText = view.findViewById(R.id.dayText);
            completedIndicator = view.findViewById(R.id.completedIndicator);
        }
    }

    // ---------------- UPDATE HABIT ----------------

    private void updateHabit() {

        String url = "http://192.168.1.37:8080/api/habits/" + habitId;

        JSONObject json = new JSONObject();
        try {
            json.put("title", etTitle.getText().toString());
            json.put("description", etDescription.getText().toString());
            json.put("frequency", etFrequency.getText().toString());
            json.put("completed", checkCompleted.isChecked());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                json,
                response -> {
                    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return HabitDetailsActivity.this.getHeaders();
            }
        };

        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    // ---------------- DELETE HABIT ----------------

    private void deleteHabit() {

        String url = "http://192.168.1.37:8080/api/habits/" + habitId;

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return HabitDetailsActivity.this.getHeaders();
            }
        };

        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    // ---------------- DAILY TRACKING ----------------

    private void openDatePicker() {

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    month = month + 1;

                    String formattedDate =
                            year + "-" +
                                    String.format("%02d", month) + "-" +
                                    String.format("%02d", dayOfMonth);

                    trackHabitForDate(formattedDate);
                },
                currentYear,
                currentMonth - 1,
                1
        );

        picker.show();
    }

    private void trackHabitForDate(String date) {

        String url = "http://192.168.1.37:8080/api/habits/"
                + habitId + "/log?date=" + date;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                response -> {
                    Toast.makeText(this, "Day updated", Toast.LENGTH_SHORT).show();
                    loadMonthLogs();
                },
                error -> Toast.makeText(this, "Tracking failed", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return HabitDetailsActivity.this.getHeaders();
            }
        };

        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    // ---------------- LOAD MONTH LOGS ----------------

    private void loadMonthLogs() {

        String url = "http://192.168.1.37:8080/api/habits/"
                + habitId
                + "/logs?year=" + currentYear
                + "&month=" + currentMonth;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {

                    completedDates.clear();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            completedDates.add(obj.getString("date"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Toast.makeText(this,
                            "Completed days this month: " + completedDates.size(),
                            Toast.LENGTH_LONG).show();
                },
                error -> Toast.makeText(this,
                        "Failed to load logs",
                        Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return HabitDetailsActivity.this.getHeaders();
            }
        };

        VolleySingleton.getInstance(this).getRequestQueue().add(request);

    }

}