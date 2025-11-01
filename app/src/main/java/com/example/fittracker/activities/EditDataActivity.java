package com.example.fittracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fittracker.R;

public class EditDataActivity extends AppCompatActivity {

    TextView profileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_data);

        profileBtn = findViewById(R.id.profileBtn);

        // Voltar para o login
        profileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(EditDataActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }
}