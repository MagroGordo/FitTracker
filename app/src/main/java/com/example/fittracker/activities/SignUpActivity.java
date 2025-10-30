package com.example.fittracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fittracker.R;

public class SignUpActivity extends AppCompatActivity {

    TextView txtEntrar;
    Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        txtEntrar = findViewById(R.id.txtEnter);
        btnCreate = findViewById(R.id.btnSignUp);

        // Voltar para o login
        txtEntrar.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
            startActivity(intent);
            finish();
        });

        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });

    }
}
