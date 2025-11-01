package com.example.fittracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.fittracker.R;
import com.example.fittracker.core.Prefs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartTrainingActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView btnMenu;
    private View dimOverlay;

    // Nav items
    private LinearLayout navDashboard, navTreinos, navPerfil, navLogout;

    // Opções de treino
    private LinearLayout optionCorrida, optionCiclismo;
    private LinearLayout selectedOption = null;

    // Cores
    private final int colorOrange = 0xFFF97316;
    private final int colorWhite = 0xFFFFFFFF;

    private Button btnStart;

    private enum NavItem { DASHBOARD, TREINOS, PERFIL }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_training);

        // Drawer + overlay + ícone menu
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);
        dimOverlay = findViewById(R.id.dimOverlay);
        btnStart = findViewById(R.id.btnStart);

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(StartTrainingActivity.this, WorkoutActivity.class);
            startActivity(intent);
            finish();
        });

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

        // Opções de treino
        optionCorrida = findViewById(R.id.optionCorrida);
        optionCiclismo = findViewById(R.id.optionCiclismo);

        setSelectableOption(optionCorrida, R.id.txtCorrida, R.id.radioCorrida);
        setSelectableOption(optionCiclismo, R.id.txtCiclismo, R.id.radioCiclismo);

        // Itens do menu lateral
        navDashboard = findViewById(R.id.navDashboard);
        navTreinos = findViewById(R.id.navTreinos);
        navPerfil = findViewById(R.id.navPerfil);
        navLogout = findViewById(R.id.navLogout);

        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(StartTrainingActivity.this, DashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }

        if (navTreinos != null) {
            navTreinos.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        if (navPerfil != null) {
            navPerfil.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(StartTrainingActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        if (navLogout != null) {
            navLogout.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                performLogout();
            });
        }

        highlightCurrentNav(NavItem.TREINOS);
    }

    @Override
    protected void onStart() {
        super.onStart();
        validateSession();
    }

    private void validateSession() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        boolean remember = Prefs.isRememberMe(getApplicationContext());
        FirebaseUser current = auth.getCurrentUser();

        if (current == null || !remember) {
            if (current != null && !remember) {
                auth.signOut();
            }
            redirectToLogin();
            return;
        }

        current.reload().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || auth.getCurrentUser() == null) {
                auth.signOut();
                Prefs.setRememberMe(getApplicationContext(), false);
                Toast.makeText(this, "Sessão inválida.", Toast.LENGTH_SHORT).show();
                redirectToLogin();
            }
        });
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        Prefs.setRememberMe(getApplicationContext(), false);
        Toast.makeText(this, "Sessão terminada.", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent i = new Intent(this, LogInActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void setSelectableOption(LinearLayout option, int titleId, int radioId) {
        if (option == null) return;

        option.setOnClickListener(v -> {
            if (selectedOption != null) {
                selectedOption.setBackgroundResource(R.drawable.box_unselected);

                TextView previousTitle = selectedOption.findViewById(getTitleId(selectedOption));
                if (previousTitle != null) previousTitle.setTextColor(colorWhite);

                View previousRadio = selectedOption.findViewById(getRadioId(selectedOption));
                if (previousRadio != null) previousRadio.setVisibility(View.GONE);
            }

            selectedOption = option;
            selectedOption.setBackgroundResource(R.drawable.box_selected);

            View currentRadio = option.findViewById(radioId);
            if (currentRadio != null) currentRadio.setVisibility(View.VISIBLE);

            TextView currentTitle = option.findViewById(titleId);
            if (currentTitle != null) currentTitle.setTextColor(colorOrange);
        });
    }

    private int getTitleId(LinearLayout option) {
        if (option.getId() == R.id.optionCorrida) return R.id.txtCorrida;
        if (option.getId() == R.id.optionCiclismo) return R.id.txtCiclismo;
        return -1;
    }

    private int getRadioId(LinearLayout option) {
        if (option.getId() == R.id.optionCorrida) return R.id.radioCorrida;
        if (option.getId() == R.id.optionCiclismo) return R.id.radioCiclismo;
        return -1;
    }

    private void highlightCurrentNav(NavItem active) {
        setNavState(navDashboard, active == NavItem.DASHBOARD);
        setNavState(navTreinos, active == NavItem.TREINOS);
        setNavState(navPerfil, active == NavItem.PERFIL);
    }

    private void setNavState(LinearLayout container, boolean selected) {
        if (container == null) return;
        container.setBackgroundResource(selected ? R.drawable.btn_orange : 0);
    }
}