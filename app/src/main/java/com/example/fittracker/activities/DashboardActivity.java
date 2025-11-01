package com.example.fittracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.fittracker.R;
import com.example.fittracker.core.Prefs;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.Toast;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView btnMenu;
    private View dimOverlay;
    private Button startTrainingBtn;

    // Itens do drawer
    private LinearLayout navDashboard, navTreinos, navPerfil, navLogout;

    private enum NavItem { DASHBOARD, TREINOS, PERFIL }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Drawer, overlay e botão do menu
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);
        dimOverlay = findViewById(R.id.dimOverlay);

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (dimOverlay != null) {
                    dimOverlay.setVisibility(View.VISIBLE);
                    dimOverlay.setAlpha(Math.max(0f, Math.min(1f, slideOffset)));
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (dimOverlay != null) {
                    dimOverlay.setVisibility(View.VISIBLE);
                    dimOverlay.setAlpha(1f);
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (dimOverlay != null) {
                    dimOverlay.setAlpha(0f);
                    dimOverlay.setVisibility(View.GONE);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) { }
        });

        if (dimOverlay != null) {
            dimOverlay.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        }

        // Botão principal
        startTrainingBtn = findViewById(R.id.btnIniciarTreino);
        if (startTrainingBtn != null) {
            startTrainingBtn.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, StartTrainingActivity.class);
                startActivity(intent);
            });
        }

        // Itens do menu lateral
        navDashboard = findViewById(R.id.navDashboard);
        navTreinos = findViewById(R.id.navTreinos);
        navPerfil = findViewById(R.id.navPerfil);
        navLogout = findViewById(R.id.navLogout);

        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        }

        if (navTreinos != null) {
            navTreinos.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(DashboardActivity.this, StartTrainingActivity.class);
                startActivity(intent);
            });
        }

        if (navPerfil != null) {
            navPerfil.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        if (navLogout != null) {
            navLogout.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                performLogout();
            });
        }

        // Destacar item ativo
        highlightCurrentNav(NavItem.DASHBOARD);
    }

    private void highlightCurrentNav(NavItem active) {
        setNavState(navDashboard, R.id.navDashboardLabel, active == NavItem.DASHBOARD);
        setNavState(navTreinos, R.id.navTreinosLabel, active == NavItem.TREINOS);
        setNavState(navPerfil, R.id.navPerfilLabel, active == NavItem.PERFIL);
        // Logout nunca fica selecionado
        setNavState(navLogout, R.id.navLogoutLabel, false);
    }

    private void performLogout() {
        // 1. Fazer logout do Firebase
        FirebaseAuth.getInstance().signOut();

        // 2. Limpar Remember Me
        Prefs.setRememberMe(getApplicationContext(), false);

        // 3. Mostrar mensagem
        Toast.makeText(this, "Sessão terminada", Toast.LENGTH_SHORT).show();

        // 4. Ir para o login
        Intent intent = new Intent(DashboardActivity.this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setNavState(LinearLayout container, int labelId, boolean selected) {
        if (container == null) return;
        container.setBackgroundResource(selected ? R.drawable.btn_orange : R.drawable.nav_item_default);
        TextView label = container.findViewById(labelId);
        if (label != null) {
            label.setTypeface(label.getTypeface(),
                    selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
    }
}