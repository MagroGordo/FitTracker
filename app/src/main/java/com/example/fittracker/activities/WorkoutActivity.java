package com.example.fittracker.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.fittracker.R;
import com.example.fittracker.database.entities.User;
import com.example.fittracker.database.entities.Workout;
import com.example.fittracker.database.repositories.UserRepository;
import com.example.fittracker.database.repositories.WorkoutRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutActivity extends AppCompatActivity {

    private static final int REQ_LOCATION = 1001;

    // UI
    private TextView tvLatitude, tvLongitude, tvDistancia, tvCalorias, tvVelocidade, tvTempo;
    private Button btnFinish;

    // Localização
    private FusedLocationProviderClient fusedClient;
    private LocationCallback locationCallback;
    private Location lastLocation;

    // Métricas
    private float totalDistanceMeters = 0f;
    private float currentSpeedKmh = 0f;
    private long startTimeMs = 0L;
    private long elapsedMs = 0L;
    private Handler timerHandler;
    private Runnable timerRunnable;

    // Persistência
    private ExecutorService ioExecutor;
    private WorkoutRepository workoutRepo;
    private UserRepository userRepo;

    // Parametrização (podes passar via Intent "type" = "run"|"bike")
    private String workoutType = "run";
    private float kcalPerKm = 60f; // run ~60, bike ~30

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout);

        // Tipo de treino opcional
        String typeExtra = getIntent().getStringExtra("type");
        if (typeExtra != null) workoutType = typeExtra;
        kcalPerKm = workoutType.equalsIgnoreCase("bike") ? 30f : 60f;

        bindViews();

        // Repositórios com Context (alinhado com a tua implementação atual)
        workoutRepo = new WorkoutRepository(getApplicationContext());
        userRepo = new UserRepository(getApplicationContext());
        ioExecutor = Executors.newSingleThreadExecutor();

        // Localização
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        // Cronómetro
        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (startTimeMs > 0) {
                    elapsedMs = System.currentTimeMillis() - startTimeMs;
                    tvTempo.setText(formatElapsed(elapsedMs));
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        btnFinish.setOnClickListener(v -> {
            stopLocationUpdates();
            stopTimer();
            saveWorkoutAndExit();
        });

        requestLocationPermissions();
    }

    private void bindViews() {
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvDistancia = findViewById(R.id.tvDistancia);
        tvCalorias = findViewById(R.id.tvCalorias);
        tvVelocidade = findViewById(R.id.tvVelocidade);
        tvTempo = findViewById(R.id.tvTempo);
        btnFinish = findViewById(R.id.btnFinish);
    }

    private void requestLocationPermissions() {
        boolean fine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!fine || !coarse) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION
            );
        } else {
            startTracking();
        }
    }

    private void startTracking() {
        startTimeMs = System.currentTimeMillis();
        timerHandler.post(timerRunnable);

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(2000)            // 2s
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location location = result.getLastLocation();
                if (location == null) return;

                updateMetrics(location);
                updateUi(location);
                lastLocation = location;
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void updateMetrics(Location newLoc) {
        if (lastLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(
                    lastLocation.getLatitude(), lastLocation.getLongitude(),
                    newLoc.getLatitude(), newLoc.getLongitude(),
                    results
            );
            float segment = results[0];

            // Filtro anti-ruído (ignora flutuações muito pequenas e saltos absurdos)
            if (segment > 1f && segment < 100f) {
                totalDistanceMeters += segment;
            }
        }

        // Velocidade instantânea (m/s -> km/h)
        if (newLoc.hasSpeed()) {
            currentSpeedKmh = newLoc.getSpeed() * 3.6f;
        } else {
            // Fallback: velocidade média aproximada
            if (elapsedMs > 0) {
                float hours = elapsedMs / 3_600_000f;
                float km = totalDistanceMeters / 1000f;
                currentSpeedKmh = (hours > 0f) ? (km / hours) : 0f;
            } else {
                currentSpeedKmh = 0f;
            }
        }
    }

    private void updateUi(Location loc) {
        tvLatitude.setText(String.format(Locale.getDefault(), "%.5f°", loc.getLatitude()));
        tvLongitude.setText(String.format(Locale.getDefault(), "%.5f°", loc.getLongitude()));

        float km = totalDistanceMeters / 1000f;
        tvDistancia.setText(String.format(Locale.getDefault(), "%.2f", km));

        tvVelocidade.setText(String.format(Locale.getDefault(), "%.1f", currentSpeedKmh));

        int kcal = (int) Math.max(0, Math.round(km * kcalPerKm));
        tvCalorias.setText(String.valueOf(kcal));
    }

    private void stopLocationUpdates() {
        if (fusedClient != null && locationCallback != null) {
            fusedClient.removeLocationUpdates(locationCallback);
        }
    }

    private void stopTimer() {
        startTimeMs = 0L;
        timerHandler.removeCallbacks(timerRunnable);
    }

    private String formatElapsed(long ms) {
        long seconds = ms / 1000;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
    }

    private void saveWorkoutAndExit() {
        final float km = totalDistanceMeters / 1000f;
        final int kcal = (int) Math.max(0, Math.round(km * kcalPerKm));
        final long durationMs = elapsedMs;

        final float avgKmh;
        if (durationMs > 0) {
            float hours = durationMs / 3_600_000f;
            avgKmh = (hours > 0f) ? (km / hours) : 0f;
        } else {
            avgKmh = 0f;
        }

        ioExecutor.execute(() -> {
            try {
                User current = userRepo.getFirstUser();
                if (current == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Utilizador não encontrado", Toast.LENGTH_SHORT).show());
                    goToDashboard(false);
                    return;
                }

                Workout w = new Workout();
                // Campos conforme a tua entidade (ajusta se os nomes forem diferentes)
                w.setUserId(current.getId());
                w.setType(workoutType); // "run" | "bike"
                w.setDistance(km);      // se o teu campo for distanceKm, ajusta o setter
                w.setDuration((int) durationMs); // se guardas em long, ajusta o tipo/setter
                w.setCalories((double) kcal);
                w.setAvgSpeed((double) avgKmh);
                w.setDate(new Date());

                // Opcional: se tiveres start/end time ou coordenadas
                // w.setStartTime(new Date(System.currentTimeMillis() - durationMs));
                // w.setEndTime(new Date());
                // w.setStartLatitude(...); w.setStartLongitude(...);
                // w.setEndLatitude(...);   w.setEndLongitude(...);

                workoutRepo.insertLocal(w);

                runOnUiThread(() -> Toast.makeText(this, "Treino guardado!", Toast.LENGTH_SHORT).show());
                goToDashboard(true);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Erro ao guardar treino", Toast.LENGTH_SHORT).show());
                goToDashboard(false);
            }
        });
    }

    private void goToDashboard(boolean saved) {
        Intent intent = new Intent(WorkoutActivity.this, DashboardActivity.class);
        intent.putExtra("refresh_last_workout", saved);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        stopTimer();
        if (ioExecutor != null) ioExecutor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION) {
            boolean granted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                startTracking();
            } else {
                Toast.makeText(this, "Permissão de localização é necessária", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}