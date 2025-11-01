package com.example.fittracker.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.fittracker.database.entities.User;
import com.example.fittracker.database.entities.Workout;
import com.example.fittracker.database.repositories.UserRepository;
import com.example.fittracker.database.repositories.WorkoutRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView btnMenu;
    private View dimOverlay;
    private Button startTrainingBtn;

    // Itens do drawer
    private LinearLayout navDashboard, navTreinos, navPerfil, navLogout;

    // Header/Drawer user views
    private TextView tvHeaderName, tvHeaderEmail;
    private TextView tvDrawerName, tvDrawerEmail;

    // Último treino views
    private TextView tvUltTreinoTipo, tvUltTreinoQuando, tvUltTreinoDist, tvUltTreinoTempo, tvUltTreinoKcal, tvUltTreinoVel;

    // Repositórios e IO
    private UserRepository userRepo;
    private WorkoutRepository workoutRepo;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private enum NavItem { DASHBOARD, TREINOS, PERFIL }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Drawer, overlay e botão do menu
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);
        dimOverlay = findViewById(R.id.dimOverlay);

        // Header/Drawer user views
        tvHeaderName = findViewById(R.id.tvHeaderName);
        tvHeaderEmail = findViewById(R.id.tvHeaderEmail);
        tvDrawerName = findViewById(R.id.tvDrawerName);
        tvDrawerEmail = findViewById(R.id.tvDrawerEmail);

        // Último treino views
        tvUltTreinoTipo = findViewById(R.id.tvUltTreinoTipo);
        tvUltTreinoQuando = findViewById(R.id.tvUltTreinoQuando);
        tvUltTreinoDist = findViewById(R.id.tvUltTreinoDist);
        tvUltTreinoTempo = findViewById(R.id.tvUltTreinoTempo);
        tvUltTreinoKcal = findViewById(R.id.tvUltTreinoKcal);
        tvUltTreinoVel = findViewById(R.id.tvUltTreinoVel);

        userRepo = new UserRepository(getApplicationContext());
        workoutRepo = new WorkoutRepository(getApplicationContext());

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
            });
        }

        if (drawerLayout != null) {
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
        }

        if (dimOverlay != null) {
            dimOverlay.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
            });
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
            navDashboard.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        if (navTreinos != null) {
            navTreinos.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(DashboardActivity.this, StartTrainingActivity.class);
                startActivity(intent);
            });
        }

        if (navPerfil != null) {
            navPerfil.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        if (navLogout != null) {
            navLogout.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                performLogout();
            });
        }

        // Inicialmente mostra traços no cartão "Último Treino"
        showDashedLastWorkout();

        // Carregar dados do utilizador para header + drawer + último treino
        loadCurrentUserData();

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
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception ignored) {}
        Prefs.setRememberMe(getApplicationContext(), false);
        Toast.makeText(this, "Sessão terminada", Toast.LENGTH_SHORT).show();
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

    // ==== User loading/binding ====

    private void loadCurrentUserData() {
        FirebaseUser fbUser = null;
        try {
            fbUser = FirebaseAuth.getInstance().getCurrentUser();
        } catch (Exception e) {
            android.util.Log.e("Dashboard", "Erro ao obter Firebase user", e);
        }

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

        if (tvHeaderEmail != null) tvHeaderEmail.setText(fallbackEmail != null ? fallbackEmail : "");
        if (tvDrawerEmail != null) tvDrawerEmail.setText(fallbackEmail != null ? fallbackEmail : "");

        // Tenta Room primeiro
        io.execute(() -> {
            try {
                User local = userRepo.getByFirebaseUid(firebaseUid);
                runOnUiThread(() -> {
                    if (local != null) {
                        bindUserToUI(local, fallbackEmail);
                        if (local.getId() > 0) {
                            loadLastWorkout(local.getId());
                        } else {
                            showDashedLastWorkout();
                        }
                    } else {
                        showDashedLastWorkout();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("Dashboard", "Erro ao carregar utilizador local", e);
                runOnUiThread(this::showDashedLastWorkout);
            }
        });

        // Busca Firestore e atualiza
        userRepo.fetchUserFromFirestore(firebaseUid, new UserRepository.UserLoadCallback() {
            @Override public void onLoaded(User user) {
                runOnUiThread(() -> {
                    bindUserToUI(user, fallbackEmail);
                    if (user != null && user.getId() > 0) {
                        loadLastWorkout(user.getId());
                    } else {
                        io.execute(() -> {
                            try {
                                User local = userRepo.getByFirebaseUid(firebaseUid);
                                runOnUiThread(() -> {
                                    if (local != null && local.getId() > 0) {
                                        loadLastWorkout(local.getId());
                                    } else {
                                        showDashedLastWorkout();
                                    }
                                });
                            } catch (Exception e) {
                                android.util.Log.e("Dashboard", "Erro ao recarregar utilizador local", e);
                                runOnUiThread(DashboardActivity.this::showDashedLastWorkout);
                            }
                        });
                    }
                });
            }
            @Override public void onError(Exception e) {
                android.util.Log.e("Dashboard", "Erro ao carregar utilizador do Firestore", e);
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

    // ==== Ultimo Treino ====

    private void showDashedLastWorkout() {
        if (tvUltTreinoTipo != null) tvUltTreinoTipo.setText("—");
        if (tvUltTreinoQuando != null) tvUltTreinoQuando.setText("—");
        if (tvUltTreinoDist != null) tvUltTreinoDist.setText("—");
        if (tvUltTreinoTempo != null) tvUltTreinoTempo.setText("—");
        if (tvUltTreinoKcal != null) tvUltTreinoKcal.setText("—");
        if (tvUltTreinoVel != null) tvUltTreinoVel.setText("—");
    }

    private void loadLastWorkout(long userId) {
        if (userId <= 0) {
            runOnUiThread(this::showDashedLastWorkout);
            return;
        }
        io.execute(() -> {
            try {
                Workout last = workoutRepo.getLastWorkout(userId);
                runOnUiThread(() -> {
                    if (last == null) {
                        showDashedLastWorkout();
                    } else {
                        bindLastWorkout(last);
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("Dashboard", "Erro ao carregar último treino", e);
                runOnUiThread(this::showDashedLastWorkout);
            }
        });
    }

    private void bindLastWorkout(Workout w) {
        String tipoPt;
        if (w.getType() == null) {
            tipoPt = "—";
        } else {
            switch (w.getType().toLowerCase()) {
                case "running":
                case "run": tipoPt = "Corrida"; break;
                case "walking": tipoPt = "Caminhada"; break;
                case "cycling":
                case "bike": tipoPt = "Ciclismo"; break;
                default: tipoPt = w.getType();
            }
        }
        if (tvUltTreinoTipo != null) tvUltTreinoTipo.setText(tipoPt != null ? tipoPt : "—");

        java.util.Date when = w.getDate();
        if (when == null) when = w.getStartTime();
        if (when != null) {
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());
            if (tvUltTreinoQuando != null) tvUltTreinoQuando.setText(df.format(when));
        } else {
            if (tvUltTreinoQuando != null) tvUltTreinoQuando.setText("—");
        }

        if (tvUltTreinoDist != null) {
            double km = w.getDistance();
            if (km > 0) {
                tvUltTreinoDist.setText(String.format(Locale.getDefault(), "%.2f km", km));
            } else {
                tvUltTreinoDist.setText("—");
            }
        }

        if (tvUltTreinoTempo != null) {
            int dur = w.getDuration();
            String tempoStr;
            if (dur <= 0) {
                tempoStr = "—";
            } else {
                long ms = dur;
                if (ms < 10_000_000L) ms = ms * 1000L;
                long totalSec = ms / 1000L;
                long h = totalSec / 3600L;
                long m = (totalSec % 3600L) / 60L;
                long s = totalSec % 60L;
                tempoStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
            }
            tvUltTreinoTempo.setText(tempoStr);
        }

        if (tvUltTreinoKcal != null) {
            double kcal = w.getCalories();
            if (kcal > 0) {
                tvUltTreinoKcal.setText(String.format(Locale.getDefault(), "%.0f kcal", kcal));
            } else {
                tvUltTreinoKcal.setText("—");
            }
        }

        if (tvUltTreinoVel != null) {
            double v = w.getAvgSpeed();
            if (v > 0) {
                tvUltTreinoVel.setText(String.format(Locale.getDefault(), "%.1f km/h", v));
            } else {
                tvUltTreinoVel.setText("—");
            }
        }
    }
}