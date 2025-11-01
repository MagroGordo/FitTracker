package com.example.fittracker.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.fittracker.database.entities.User;
import com.example.fittracker.database.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final int colorWhite = 0xFFFF;

    private Button btnStart;

    // Header/Drawer user views
    private TextView tvHeaderName, tvHeaderEmail;
    private TextView tvDrawerName, tvDrawerEmail;

    // Repositório e IO
    private UserRepository userRepo;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

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

        // Header/Drawer user views
        tvHeaderName = findViewById(R.id.tvHeaderName);
        tvHeaderEmail = findViewById(R.id.tvHeaderEmail);
        tvDrawerName = findViewById(R.id.tvDrawerName);
        tvDrawerEmail = findViewById(R.id.tvDrawerEmail);

        userRepo = new UserRepository(getApplicationContext());

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        if (btnStart != null) {
            btnStart.setOnClickListener(v -> {
                // Verificar se o utilizador escolheu um tipo de treino
                if (selectedOption == null) {
                    Toast.makeText(this, "Por favor, escolha um tipo de treino", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Determinar o tipo de treino selecionado
                String workoutType = "run"; // default
                if (selectedOption.getId() == R.id.optionCorrida) {
                    workoutType = "run";
                } else if (selectedOption.getId() == R.id.optionCiclismo) {
                    workoutType = "bike";
                }

                // Passar o tipo de treino para o WorkoutActivity
                Intent intent = new Intent(StartTrainingActivity.this, WorkoutActivity.class);
                intent.putExtra("type", workoutType);
                startActivity(intent);
                finish();
            });
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
                performLogout();
            });
        }

        // Carregar dados do utilizador para header + drawer
        loadCurrentUserData();

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

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        Prefs.setRememberMe(getApplicationContext(), false);
        Toast.makeText(this, "Sessão terminada", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LogInActivity.class);
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

    // ==== User loading/binding (igual ao padrão do ProfileActivity, mas reduzido ao header/drawer) ====

    private void loadCurrentUserData() {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser == null) {
            Toast.makeText(this, "Sem sessão. Faça login novamente.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        final String firebaseUid = fbUser.getUid();
        final String fallbackEmail = fbUser.getEmail();

        // Pré-preencher email
        if (tvHeaderEmail != null) tvHeaderEmail.setText(fallbackEmail != null ? fallbackEmail : "");
        if (tvDrawerEmail != null) tvDrawerEmail.setText(fallbackEmail != null ? fallbackEmail : "");

        // Tenta Room primeiro
        io.execute(() -> {
            User local = userRepo.getByFirebaseUid(firebaseUid);
            runOnUiThread(() -> {
                if (local != null) bindUserToUI(local, fallbackEmail);
            });
        });

        // Busca Firestore e atualiza
        userRepo.fetchUserFromFirestore(firebaseUid, new UserRepository.UserLoadCallback() {
            @Override public void onLoaded(User user) {
                runOnUiThread(() -> bindUserToUI(user, fallbackEmail));
            }
            @Override public void onError(Exception e) {
                // mantém valores locais/fallback
            }
        });
    }

    private void bindUserToUI(User user, String fallbackEmail) {
        if (user == null) {
            if (tvHeaderName != null) tvHeaderName.setText("Utilizador");
            if (tvDrawerName != null) tvDrawerName.setText("Utilizador");
            return;
        }

        String name = user.getName();
        String email = user.getEmail() != null ? user.getEmail() : fallbackEmail;

        if (tvHeaderName != null) tvHeaderName.setText(name != null && !name.isEmpty() ? name : "Utilizador");
        if (tvDrawerName != null) tvDrawerName.setText(name != null && !name.isEmpty() ? name : "Utilizador");

        if (tvHeaderEmail != null) tvHeaderEmail.setText(email != null ? email : "");
        if (tvDrawerEmail != null) tvDrawerEmail.setText(email != null ? email : "");
    }
}