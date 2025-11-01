package com.example.fittracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fittracker.R;

public class WorkoutActivity extends AppCompatActivity {

    Button btnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout);

        btnFinish = findViewById(R.id.btnFinish);

        btnFinish.setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });
    }
}