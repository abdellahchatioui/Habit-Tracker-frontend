package com.example.habittracker.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.habittracker.R;
import com.example.habittracker.network.VolleySingleton;
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
    private EditText etTitle, etDescription, etFrequency;
    private CheckBox checkCompleted;
    private TextView tvMonthName;
    private CalendarView calendarView;
    private Set<LocalDate> completedDates = new HashSet<>();
    private final DateTimeFormatter monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_habit_details);

        habitId = getIntent().getLongExtra("habit_id", -1);
        
        tvMonthName = findViewById(R.id.tvMonthName);
        calendarView = findViewById(R.id.calendarView);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etFrequency = findViewById(R.id.etFrequency);
        checkCompleted = findViewById(R.id.checkCompleted);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        etTitle.setText(getIntent().getStringExtra("title"));
        etDescription.setText(getIntent().getStringExtra("description"));
        etFrequency.setText(getIntent().getStringExtra("frequency"));
        checkCompleted.setChecked(getIntent().getBooleanExtra("completed", false));

        setupCalendar();
        
        findViewById(R.id.btnUpdate).setOnClickListener(v -> showUpdateConfirmation());
        findViewById(R.id.btnDelete).setOnClickListener(v -> showDeleteConfirmation());
    }

    private void setupCalendar() {
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
                
                textView.setText(String.valueOf(day.getDate().getDayOfMonth()));
                
                if (day.getPosition() == DayPosition.MonthDate) {
                    textView.setAlpha(1f);
                    if (completedDates.contains(day.getDate())) {
                        checkImage.setVisibility(View.VISIBLE);
                    } else {
                        checkImage.setVisibility(View.GONE);
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

        String url = AppConstants.HABIT_URL + "/" + habitId +
                "/logs?year=" + year + "&month=" + month;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {

                    completedDates.removeIf(date ->
                            date.getYear() == year &&
                                    date.getMonthValue() == month
                    );


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
                },
                error -> error.printStackTrace()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return HabitDetailsActivity.this.getHeaders();
            }
        };

        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }


    private void toggleDate(LocalDate date) {

        boolean wasCompleted = completedDates.contains(date);

        if (wasCompleted) {
            completedDates.remove(date);
        } else {
            completedDates.add(date);
        }

        calendarView.notifyDateChanged(date);

        String url = AppConstants.HABIT_URL + "/" + habitId +
                "/log?date=" + date.toString();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {},
                error -> {

                    if (wasCompleted)
                        completedDates.add(date);
                    else
                        completedDates.remove(date);

                    calendarView.notifyDateChanged(date);

                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return HabitDetailsActivity.this.getHeaders();
            }
        };

        VolleySingleton.getInstance(this).getRequestQueue().add(request);
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

    private Map<String, String> getHeaders() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + prefs.getString("jwt_token", ""));
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Habit")
                .setMessage("Are you sure you want to delete this habit?")
                .setPositiveButton("Delete", (dialog, which) -> deleteHabit())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showUpdateConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Update Habit")
                .setMessage("Save changes to this habit?")
                .setPositiveButton("Update", (dialog, which) -> updateHabit())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateHabit() {
        String url = AppConstants.HABIT_URL + "/" + habitId;
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

    private void deleteHabit() {
        String url = AppConstants.HABIT_URL + "/" + habitId;
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
}
