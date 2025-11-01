package com.example.fittracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.fittracker.R;

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

    private enum NavItem { DASHBOARD, TREINOS, PERFIL }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_training);

        // Drawer + overlay + ícone menu
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
                // Já estamos nesta tela
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
                // Se houver autenticação, faça signOut aqui.
                Intent intent = new Intent(StartTrainingActivity.this, LogInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // Destacar item ativo: TREINOS
        highlightCurrentNav(NavItem.TREINOS);
    }

    private void setSelectableOption(LinearLayout option, int titleId, int radioId) {
        if (option == null) return;

        option.setOnClickListener(v -> {
            // Remove seleção anterior
            if (selectedOption != null) {
                selectedOption.setBackgroundResource(R.drawable.box_unselected);

                TextView previousTitle = selectedOption.findViewById(getTitleId(selectedOption));
                if (previousTitle != null) previousTitle.setTextColor(colorWhite);

                View previousRadio = selectedOption.findViewById(getRadioId(selectedOption));
                if (previousRadio != null) previousRadio.setVisibility(View.GONE);
            }

            // Marca nova seleção
            selectedOption = option;
            selectedOption.setBackgroundResource(R.drawable.box_selected);

            View currentRadio = option.findViewById(radioId);
            if (currentRadio != null) currentRadio.setVisibility(View.VISIBLE);

            TextView currentTitle = option.findViewById(titleId);
            if (currentTitle != null) currentTitle.setTextColor(colorOrange);
        });
    }

    // Funções auxiliares
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

    // Destaque do item ativo no nav
    private void highlightCurrentNav(NavItem active) {
        setNavState(navDashboard, R.id.navDashboardLabel, active == NavItem.DASHBOARD);
        setNavState(navTreinos, R.id.navTreinosLabel, active == NavItem.TREINOS);
        setNavState(navPerfil, R.id.navPerfilLabel, active == NavItem.PERFIL);
        // Logout nunca fica selecionado
        setNavState(navLogout, R.id.navLogoutLabel, false);
    }

    private void setNavState(LinearLayout container, int labelId, boolean selected) {
        if (container == null) return;

        container.setBackgroundResource(selected ? R.drawable.btn_orange : R.drawable.nav_item_default);

        TextView label = container.findViewById(labelId);
        if (label != null) {
            label.setTextColor(0xFFFFFFFF); // branco
            label.setTypeface(label.getTypeface(), selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
    }
}