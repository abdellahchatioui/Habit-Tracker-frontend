package com.example.habittracker.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.habittracker.R;
import com.example.habittracker.adapters.HabitAdapter;
import com.example.habittracker.models.Habit;
import com.example.habittracker.network.VolleySingleton;
import com.example.habittracker.repository.HabitRepository;
import com.example.habittracker.utils.AppConstants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private List<Habit> habitList = new ArrayList<>();
    private HabitAdapter adapter;

    @Override
    protected void onResume() {
        super.onResume();
        loadHabits();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HabitAdapter(habitList, new HabitAdapter.OnHabitClickListener() {

            @Override
            public void onClick(Habit habit) {
                openDetails(habit);
            }

            @Override
            public void onLongClick(Habit habit) {
                showUpdateDialog(habit);
            }
        });

        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddHabit);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddHabitActivity.class);
            startActivity(intent);
        });

        loadHabits();
    }

    // LOAD HABITS
    private void loadHabits() {

        String url = AppConstants.HABIT_URL;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {

                    habitList.clear();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);

                            Habit habit = new Habit(
                                    obj.getLong("id"),
                                    obj.getString("title"),
                                    obj.getString("description"),
                                    obj.getString("frequency"),
                                    obj.getBoolean("completed")
                            );

                            habitList.add(habit);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    adapter.notifyDataSetChanged();

                },
                error -> Toast.makeText(this, "Failed to load habits", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                String token = prefs.getString("jwt_token", "");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    // OPEN DETAILS
    private void openDetails(Habit habit) {

        Intent intent = new Intent(this, HabitDetailsActivity.class);

        intent.putExtra("habit_id", habit.getId());
        intent.putExtra("title", habit.getTitle());
        intent.putExtra("description", habit.getDescription());
        intent.putExtra("frequency", habit.getFrequency());
        intent.putExtra("completed", habit.getCompleted());

        startActivity(intent);
    }


    private void showUpdateDialog(Habit habit) {

        View view = getLayoutInflater().inflate(R.layout.dialog_update_habit, null);

        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etDescription = view.findViewById(R.id.etDescription);
        EditText etFrequency = view.findViewById(R.id.etFrequency);

        etTitle.setText(habit.getTitle());
        etDescription.setText(habit.getDescription());
        etFrequency.setText(habit.getFrequency());

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.DarkAlertDialogCustom)
                .setTitle("Update Habit")
                .setView(view)
                .setPositiveButton("Update", null)
                .setNeutralButton("Delete", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        // UPDATE CLICK
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            new AlertDialog.Builder(this, R.style.DarkAlertDialogCustom)
                    .setTitle("Confirm Update")
                    .setMessage("Are you sure you want to update this habit?")
                    .setPositiveButton("Yes", (d, w) -> {

                        JSONObject json = new JSONObject();

                        try {
                            json.put("title", etTitle.getText().toString());
                            json.put("description", etDescription.getText().toString());
                            json.put("frequency", etFrequency.getText().toString());
                            json.put("completed", habit.getCompleted());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        new HabitRepository(this).updateHabit(
                                this,
                                habit.getId(),
                                json,

                                res -> {
                                    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                                    loadHabits();
                                    dialog.dismiss();
                                },

                                err -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                        );

                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // DELETE CLICK
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {

            new AlertDialog.Builder(this, R.style.DarkAlertDialogCustom)
                    .setTitle("Confirm Delete")
                    .setMessage("This action cannot be undone. Delete this habit?")
                    .setPositiveButton("Delete", (d, w) -> {

                        new HabitRepository(this).deleteHabit(
                                this,
                                habit.getId(),

                                res -> {
                                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                    loadHabits();
                                    dialog.dismiss();
                                },

                                err -> Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
                        );

                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
