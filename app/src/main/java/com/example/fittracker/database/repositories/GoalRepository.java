package com.example.fittracker.database.repositories;

import android.content.Context;

import com.example.fittracker.database.AppDatabase;
import com.example.fittracker.database.daos.GoalDAO;
import com.example.fittracker.database.entities.Goal;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
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
    public void uploadGoalToFirebase(Goal goal) {
        firestore.collection("goals")
                .document(String.valueOf(goal.getId()))
                .set(goal);
    }

    public void syncGoalsFromFirebase(long userId) {
        firestore.collection("goals")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> executor.execute(() -> {
                    for (var doc : snapshot.getDocuments()) {
                        Goal goal = doc.toObject(Goal.class);
                        if (goal != null) goalDao.insert(goal);
                    }
                }));
    }
}
