package com.example.habittracker.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.habittracker.R;
import com.example.habittracker.network.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddHabitActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etFrequency;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etFrequency = findViewById(R.id.etFrequency);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> saveHabit());
    }

    private void saveHabit() {

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String frequency = etFrequency.getText().toString().trim();

        if(title.isEmpty() || description.isEmpty() || frequency.isEmpty()){
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = "http://192.168.1.37:8080/api/habits";

        JSONObject json = new JSONObject();

        try {
            json.put("title", title);
            json.put("description", description);
            json.put("frequency", frequency);
            json.put("completed",false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                json,
                response -> {
                    Toast.makeText(this, "Habit Added!", Toast.LENGTH_SHORT).show();
                    finish(); // go back to MainActivity
                },
                error -> Toast.makeText(this, "Error adding habit", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                headers.put("Authorization", "Bearer " + prefs.getString("jwt_token", ""));
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }
}
