package com.example.fittracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LogInActivity extends AppCompatActivity {

    TextView txtRegistar, txtEsqueceuPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        txtRegistar = findViewById(R.id.txtRegister);
        txtEsqueceuPassword = findViewById(R.id.txtForgot);

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
    }
}
