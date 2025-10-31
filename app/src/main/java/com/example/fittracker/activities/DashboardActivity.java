package com.example.fittracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.fittracker.R;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView btnMenu;
    private Button startTrainingBtn;

    // Itens do drawer
    private LinearLayout navDashboard, navTreinos, navPerfil, navLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Drawer e botão do menu
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.START));

        // Botão principal
        startTrainingBtn = findViewById(R.id.btnIniciarTreino);
        startTrainingBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, StartTrainingActivity.class);
            startActivity(intent);
        });

        // Itens do menu lateral
        navDashboard = findViewById(R.id.navDashboard);
        navTreinos = findViewById(R.id.navTreinos);
        navPerfil = findViewById(R.id.navPerfil);
        navLogout = findViewById(R.id.navLogout);

        navDashboard.setOnClickListener(v -> drawerLayout.closeDrawer(Gravity.START));

        navTreinos.setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            Intent intent = new Intent(DashboardActivity.this, StartTrainingActivity.class);
            startActivity(intent);
        });

        navPerfil.setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        navLogout.setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            // TODO: implementar logout
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        } else {
            super.onBackPressed();
        }
    }
}
