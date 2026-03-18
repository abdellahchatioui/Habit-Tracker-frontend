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

        tvMonthName = findViewById(R.id.tvMonthName);
        calendarView = findViewById(R.id.calendarView);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

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
                },

                error -> Toast.makeText(
                        this,
                        "Failed to load logs",
                        Toast.LENGTH_SHORT
                ).show()
        );
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


}
