package com.example.fittracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RecoverPasswordActivity extends AppCompatActivity {

    TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        txtLogin = findViewById(R.id.txtLogin);

        // Voltar para o login
        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RecoverPasswordActivity.this, LogInActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
