package com.example.habittracker.activities;

import android.content.Intent;
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
import com.android.volley.toolbox.Volley;
import com.example.habittracker.R;
import com.example.habittracker.network.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);

        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v->{
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "http://10.0.2.2:8080/api/auth/login";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("email",email);
                jsonObject.put("password",password);
            }catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonObject,
                    response -> {
                        try {
                            String token = response.getString("token");

                            saveToken(token);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        Toast.makeText(LoginActivity.this,
                                "Login failed",
                                Toast.LENGTH_SHORT).show();
                    }
            );
            VolleySingleton.getInstance(this).getRequestQueue().add(request);
        });
    }
    private void saveToken(String token){
        SharedPreferences prefs = getSharedPreferences("app_prefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("jwt_token",token);
        editor.apply();
    }
}