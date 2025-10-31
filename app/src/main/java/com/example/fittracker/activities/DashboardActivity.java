package com.example.fittracker.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fittracker.R;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Button startTrainingBtn = findViewById(R.id.btnIniciarTreino);

        startTrainingBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, StartTrainingActivity.class);
            startActivity(intent);
        });
    }
}