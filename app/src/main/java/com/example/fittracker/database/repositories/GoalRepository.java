package com.example.fittracker.database.repositories;

import android.content.Context;

import com.example.fittracker.database.AppDatabase;
import com.example.fittracker.database.daos.GoalDAO;
import com.example.fittracker.database.entities.Goal;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoalRepository {

    private final GoalDAO goalDao;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Tipos de objetivos (3 níveis)
    public enum GoalType {
        BEGINNER("Iniciante", 2.0, 150.0),      // 2km, 150 kcal
        INTERMEDIATE("Intermédio", 5.0, 300.0), // 5km, 300 kcal
        ADVANCED("Avançado", 10.0, 600.0);       // 10km, 600 kcal

        private final String displayName;
        private final double dailyDistance;
        private final double dailyCalories;

        GoalType(String displayName, double dailyDistance, double dailyCalories) {
            this.displayName = displayName;
            this.dailyDistance = dailyDistance;
            this.dailyCalories = dailyCalories;
        }

        public String getDisplayName() { return displayName; }
        public double getDailyDistance() { return dailyDistance; }
        public double getDailyCalories() { return dailyCalories; }
    }

    public GoalRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.goalDao = db.goalDao();
        this.firestore = FirebaseFirestore.getInstance();
    }

    // ========== CRIAR OBJETIVO ALEATÓRIO ==========

    /**
     * Cria um objetivo aleatório para um novo utilizador
     */
    public void createRandomGoalForUser(long userId, String firebaseUid) {
        executor.execute(() -> {
            // Verifica se já tem objetivo
            Goal existing = goalDao.getByUser(userId);
            if (existing != null) {
                android.util.Log.d("GoalRepository", "Utilizador já tem objetivo");
                return;
            }

            GoalType[] types = GoalType.values();
            GoalType randomType = types[new Random().nextInt(types.length)];

            // Cria objetivo
            Goal goal = new Goal(userId);
            goal.setDailyDistance(randomType.getDailyDistance());
            goal.setDailyCalories(randomType.getDailyCalories());

            long goalId = goalDao.insert(goal);
            goal.setId(goalId);

            android.util.Log.d("GoalRepository", "✅ Objetivo criado: " + randomType.getDisplayName() +
                    " (" + randomType.getDailyDistance() + "km, " + randomType.getDailyCalories() + " kcal)");

            // Sincroniza com Firebase
            if (firebaseUid != null) {
                uploadGoalToFirebase(goal, firebaseUid);
            }
        });
    }

    // ========== ROOM (Local) ==========

    public void insertLocal(Goal goal) {
        executor.execute(() -> goalDao.insert(goal));
    }

    public void updateLocal(Goal goal) {
        executor.execute(() -> goalDao.update(goal));
    }

    public List<Goal> getGoalsByUser(long userId) {
        return goalDao.getGoalsByUser(userId);
    }

    public Goal getByUser(long userId) {
        return goalDao.getByUser(userId);
    }

    // ========== FIREBASE ==========

    public void uploadGoalToFirebase(Goal goal, String firebaseUid) {
        if (goal == null || firebaseUid == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("firebaseUid", firebaseUid);
        data.put("userId", goal.getUserId());
        data.put("dailyDistance", goal.getDailyDistance());
        data.put("dailyCalories", goal.getDailyCalories());
        data.put("updatedAt", System.currentTimeMillis());

        firestore.collection("goals")
                .document(firebaseUid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        android.util.Log.d("GoalRepository", "✅ Objetivo sincronizado com Firestore"))
                .addOnFailureListener(e ->
                        android.util.Log.e("GoalRepository", "❌ Erro ao sincronizar objetivo", e));
    }

    public void syncGoalsFromFirebase(String firebaseUid, GoalLoadCallback callback) {
        if (firebaseUid == null) {
            if (callback != null) callback.onError(new Exception("Firebase UID null"));
            return;
        }

        firestore.collection("goals")
                .document(firebaseUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        if (callback != null) callback.onLoaded(null);
                        return;
                    }

                    executor.execute(() -> {
                        Goal goal = new Goal();
                        Number userIdNum = (Number) doc.get("userId");
                        if (userIdNum != null) goal.setUserId(userIdNum.longValue());

                        Double dd = doc.getDouble("dailyDistance");
                        if (dd != null) goal.setDailyDistance(dd);

                        Double dc = doc.getDouble("dailyCalories");
                        if (dc != null) goal.setDailyCalories(dc);

                        // Atualiza ou insere localmente
                        Goal existing = goalDao.getByUser(goal.getUserId());
                        if (existing != null) {
                            goal.setId(existing.getId());
                            goalDao.update(goal);
                        } else {
                            long id = goalDao.insert(goal);
                            goal.setId(id);
                        }

                        if (callback != null) callback.onLoaded(goal);
                    });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    // ========== CALLBACK ==========

    public interface GoalLoadCallback {
        void onLoaded(Goal goal);
        void onError(Exception e);
    }
}