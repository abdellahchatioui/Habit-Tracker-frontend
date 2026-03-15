package com.example.habittracker.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.habittracker.network.VolleySingleton;
import com.example.habittracker.utils.AppConstants;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class HabitRepository {

    private Context context;

    public HabitRepository(Context context) {
        this.context = context;
    }

    public void loadLogsForMonth(
            Long habitId,
            int year,
            int month,
            Response.Listener<JSONArray> listener,
            Response.ErrorListener errorListener
    ) {

        String url = AppConstants.HABIT_URL + "/" + habitId +
                "/logs?year=" + year + "&month=" + month;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                listener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() {

                SharedPreferences prefs =
                        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization",
                        "Bearer " + prefs.getString("jwt_token", ""));

                headers.put("Content-Type", "application/json");

                return headers;
            }
        };

        VolleySingleton
                .getInstance(context)
                .getRequestQueue()
                .add(request);
    }
}