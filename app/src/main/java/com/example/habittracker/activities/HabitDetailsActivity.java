package com.example.habittracker.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.habittracker.R;
import com.example.habittracker.network.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HabitDetailsActivity extends AppCompatActivity {

    private Long habitId;
    private EditText etTitle, etDescription, etFrequency;
    private CheckBox checkCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_details);

        habitId = getIntent().getLongExtra("habit_id", -1);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etFrequency = findViewById(R.id.etFrequency);
        checkCompleted = findViewById(R.id.checkCompleted);

        etTitle.setText(getIntent().getStringExtra("title"));
        etDescription.setText(getIntent().getStringExtra("description"));
        etFrequency.setText(getIntent().getStringExtra("frequency"));
        checkCompleted.setChecked(getIntent().getBooleanExtra("completed", false));

        findViewById(R.id.btnUpdate).setOnClickListener(v -> updateHabit());
        findViewById(R.id.btnDelete).setOnClickListener(v -> deleteHabit());
    }

    private Map<String, String> getHeaders() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + prefs.getString("jwt_token", ""));
        headers.put("Content-Type", "application/json");
        return headers;
    }

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
}