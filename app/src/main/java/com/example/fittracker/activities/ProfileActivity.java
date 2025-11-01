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
import com.example.fittracker.database.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView btnMenu;
    private View dimOverlay;
    private LinearLayout navDashboard, navTreinos, navPerfil, navLogout;
    private Button btnChangePassword;

    // Views de dados
    private TextView tvHeaderName, tvHeaderEmail;
    private TextView tvName, tvEmail, tvGender, tvAge, tvWeight, tvHeight;
    private TextView tvDrawerName, tvDrawerEmail;

    private UserRepository userRepo;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private enum NavItem { DASHBOARD, TREINOS, PERFIL }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userRepo = new UserRepository(getApplicationContext());

        // Drawer e UI
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);
        dimOverlay = findViewById(R.id.dimOverlay);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // Dados - cabeçalho principal
        tvHeaderName = findViewById(R.id.tvHeaderName);
        tvHeaderEmail = findViewById(R.id.tvHeaderEmail);

        // Dados - secção "Dados Pessoais"
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvGender = findViewById(R.id.tvGender);
        tvAge = findViewById(R.id.tvAge);
        tvWeight = findViewById(R.id.tvWeight);
        tvHeight = findViewById(R.id.tvHeight);

        // Dados - cabeçalho dentro do Drawer
        tvDrawerName = findViewById(R.id.tvDrawerName);
        tvDrawerEmail = findViewById(R.id.tvDrawerEmail);

        // Ações
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v ->
                    startActivity(new Intent(this, ChangePasswordActivity.class))
            );
        }

        if (btnMenu != null) btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override public void onDrawerSlide(View drawerView, float slideOffset) {
                if (dimOverlay != null) {
                    dimOverlay.setVisibility(View.VISIBLE);
                    dimOverlay.setAlpha(Math.max(0f, Math.min(1f, slideOffset)));
                }
            }
            @Override public void onDrawerOpened(View drawerView) {
                if (dimOverlay != null) { dimOverlay.setVisibility(View.VISIBLE); dimOverlay.setAlpha(1f); }
            }
            @Override public void onDrawerClosed(View drawerView) {
                if (dimOverlay != null) { dimOverlay.setAlpha(0f); dimOverlay.setVisibility(View.GONE); }
            }
            @Override public void onDrawerStateChanged(int newState) {}
        });

        if (dimOverlay != null) dimOverlay.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        // Nav items
        navDashboard = findViewById(R.id.navDashboard);
        navTreinos = findViewById(R.id.navTreinos);
        navPerfil = findViewById(R.id.navPerfil);
        navLogout = findViewById(R.id.navLogout);

        if (navDashboard != null) navDashboard.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, DashboardActivity.class));
        });
        if (navTreinos != null) navTreinos.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, StartTrainingActivity.class));
        });
        if (navPerfil != null) navPerfil.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        if (navLogout != null) navLogout.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            performLogout();
        });

        highlightCurrentNav(NavItem.PERFIL);

        // Carregar dados do utilizador
        loadCurrentUserData();
    }

    private void loadCurrentUserData() {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser == null) {
            Toast.makeText(this, "Sem sessão. Faça login novamente.", Toast.LENGTH_SHORT).show();
            goToLogin();
            return;
        }

        final String firebaseUid = fbUser.getUid();
        final String fallbackEmail = fbUser.getEmail();

        // Mostrar já o email conhecido
        if (tvHeaderEmail != null) tvHeaderEmail.setText(fallbackEmail != null ? fallbackEmail : "");
        if (tvEmail != null) tvEmail.setText(fallbackEmail != null ? fallbackEmail : "");
        if (tvDrawerEmail != null) tvDrawerEmail.setText(fallbackEmail != null ? fallbackEmail : "");

        // 1) Tenta Room por UID (rápido)
        io.execute(() -> {
            User local = userRepo.getByFirebaseUid(firebaseUid);
            runOnUiThread(() -> {
                if (local != null) bindUserToUI(local, fallbackEmail);
            });
        });

        // 2) Busca Firestore e atualiza UI quando chegar, e persiste em Room
        userRepo.fetchUserFromFirestore(firebaseUid, new UserRepository.UserLoadCallback() {
            @Override public void onLoaded(User user) {
                runOnUiThread(() -> bindUserToUI(user, fallbackEmail));
            }
            @Override public void onError(Exception e) {
                // mantém local/fallback
            }
        });
    }

    private void bindUserToUI(User user, String fallbackEmail) {
        if (user == null) {
            if (tvHeaderName != null) tvHeaderName.setText("Utilizador");
            if (tvDrawerName != null) tvDrawerName.setText("Utilizador");
            if (tvName != null) tvName.setText("—");
            if (tvGender != null) tvGender.setText("—");
            if (tvAge != null) tvAge.setText("—");
            if (tvWeight != null) tvWeight.setText("—");
            if (tvHeight != null) tvHeight.setText("—");
            return;
        }

        String name = user.getName();
        String email = user.getEmail() != null ? user.getEmail() : fallbackEmail;

        if (tvHeaderName != null) tvHeaderName.setText(name != null && !name.isEmpty() ? name : "Utilizador");
        if (tvDrawerName != null) tvDrawerName.setText(name != null && !name.isEmpty() ? name : "Utilizador");
        if (tvName != null) tvName.setText(name != null && !name.isEmpty() ? name : "—");

        if (tvHeaderEmail != null) tvHeaderEmail.setText(email != null ? email : "");
        if (tvDrawerEmail != null) tvDrawerEmail.setText(email != null ? email : "");
        if (tvEmail != null) tvEmail.setText(email != null ? email : "—");

        if (tvGender != null) tvGender.setText(user.getGender() != null && !user.getGender().isEmpty() ? user.getGender() : "—");

        if (tvAge != null) {
            if (user.getBirthday() != null) {
                tvAge.setText(calculateAge(user.getBirthday()) + " anos");
            } else {
                tvAge.setText("—");
            }
        }

        if (tvWeight != null) {
            tvWeight.setText(user.getWeight() > 0 ? String.format("%.1f kg", user.getWeight()) : "—");
        }

        if (tvHeight != null) {
            double h = user.getHeight();
            tvHeight.setText(h > 0 ? String.format("%.0f cm", h) : "—");
        }
    }

    private int calculateAge(Date birthday) {
        Calendar dob = Calendar.getInstance();
        dob.setTime(birthday);
        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        Prefs.setRememberMe(getApplicationContext(), false);
        Toast.makeText(this, "Sessão terminada", Toast.LENGTH_SHORT).show();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void highlightCurrentNav(NavItem active) {
        setNavState(navDashboard, R.id.navDashboardLabel, active == NavItem.DASHBOARD);
        setNavState(navTreinos, R.id.navTreinosLabel, active == NavItem.TREINOS);
        setNavState(navPerfil, R.id.navPerfilLabel, active == NavItem.PERFIL);
        setNavState(navLogout, R.id.navLogoutLabel, false);
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