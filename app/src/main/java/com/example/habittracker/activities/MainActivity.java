package com.example.habittracker.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.habittracker.R;
import com.example.habittracker.adapters.HabitAdapter;
import com.example.habittracker.models.Habit;
import com.example.habittracker.network.VolleySingleton;
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
        loadHabits(); // refresh list when returning
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HabitAdapter(habitList);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddHabit);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddHabitActivity.class);
            startActivity(intent);
        });

        loadHabits();

    }

    private void loadHabits() {

        String url = "http://192.168.1.37:8080/api/habits";

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
                error -> {
                    Toast.makeText(this, "Failed to load habits", Toast.LENGTH_SHORT).show();
                }
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

}