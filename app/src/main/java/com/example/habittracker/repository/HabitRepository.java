package com.example.habittracker.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.habittracker.network.VolleySingleton;
import com.example.habittracker.utils.AppConstants;

import org.json.JSONArray;
import org.json.JSONObject;

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

    public void updateHabit(
            Context context,
            Long habitId,
            JSONObject data,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener
    ) {

        String url = AppConstants.HABIT_URL + "/" + habitId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                data,
                listener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences prefs =
                        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + prefs.getString("jwt_token", ""));
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }

    public void deleteHabit(
            Context context,
            Long habitId,
            Response.Listener<String> listener,
            Response.ErrorListener errorListener
    ) {

        String url = AppConstants.HABIT_URL + "/" + habitId;

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                listener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences prefs =
                        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + prefs.getString("jwt_token", ""));
                return headers;
            }
        };

        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }
}