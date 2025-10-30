package com.example.fittracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fittracker.R;

public class LogInActivity extends AppCompatActivity {

    TextView txtRegistar, txtEsqueceuPassword;
    Button btnEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        txtRegistar = findViewById(R.id.txtRegister);
        txtEsqueceuPassword = findViewById(R.id.txtForgot);
        btnEntrar = findViewById(R.id.btnLogin);

        // Ir para página de registo
        txtRegistar.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Ir para página de recuperação de password
        txtEsqueceuPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, RecoverPasswordActivity.class);
            startActivity(intent);
        });

        btnEntrar.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
