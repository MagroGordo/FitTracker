package com.example.fittracker.database.repositories;

import android.content.Context;

import com.example.fittracker.database.AppDatabase;
import com.example.fittracker.database.daos.GoalDAO;
import com.example.fittracker.database.entities.Goal;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoalRepository {

    private final GoalDAO goalDao;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public GoalRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.goalDao = db.goalDao();
        this.firestore = FirebaseFirestore.getInstance();
    }

    // ROOM
    public void insertLocal(Goal goal) {
        executor.execute(() -> goalDao.insert(goal));
    }

    public List<Goal> getGoalsByUser(long userId) {
        return goalDao.getGoalsByUser(userId);
    }

    // FIREBASE
    public void uploadGoalToFirebase(Goal goal, String firebaseUid) {
        if (goal == null || firebaseUid == null) return;
        String docId = String.valueOf(goal.getId()); // ou firebaseUid se for 1 meta por user

        Map<String, Object> data = new HashMap<>();
        data.put("firebaseUid", firebaseUid);
        data.put("userId", goal.getUserId()); // opcional
        data.put("dailyDistance", goal.getDailyDistance());
        data.put("dailyCalories", goal.getDailyCalories());

        firestore.collection("goals")
                .document(docId)
                .set(data);
    }

    public void syncGoalsFromFirebase(String firebaseUid) {
        if (firebaseUid == null) return;
        firestore.collection("goals")
                .whereEqualTo("firebaseUid", firebaseUid)
                .get()
                .addOnSuccessListener(snapshot -> executor.execute(() -> {
                    snapshot.getDocuments().forEach(doc -> {
                        Goal goal = new Goal();
                        Number userIdNum = (Number) doc.get("userId");
                        if (userIdNum != null) goal.setUserId(userIdNum.longValue());
                        Double dd = doc.getDouble("dailyDistance");
                        if (dd != null) goal.setDailyDistance(dd);
                        Double dc = doc.getDouble("dailyCalories");
                        if (dc != null) goal.setDailyCalories(dc);
                        goalDao.insert(goal);
                    });
                }));
    }
}