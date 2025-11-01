package com.example.fittracker.database.repositories;

import android.content.Context;

import com.example.fittracker.database.AppDatabase;
import com.example.fittracker.database.daos.WorkoutDAO;
import com.example.fittracker.database.entities.Workout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutRepository {

    private final WorkoutDAO workoutDao;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public WorkoutRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.workoutDao = db.workoutDao();
        this.firestore = FirebaseFirestore.getInstance();
    }

    // ROOM
    public void insertLocal(Workout workout) {
        executor.execute(() -> workoutDao.insert(workout));
    }

    public List<Workout> getAllForUser(long userId) {
        return workoutDao.getAllWorkoutsForUser(userId);
    }

    public Workout getLastWorkout(long userId) {
        return workoutDao.getLastWorkout(userId);
    }

    // FIREBASE
    public void uploadToFirebase(Workout workout) {
        firestore.collection("workouts")
                .document(workout.getFirebaseId())
                .set(workout);
    }

    public void syncFromFirebase(long userId) {
        firestore.collection("workouts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> executor.execute(() -> {
                    for (var doc : snapshot.getDocuments()) {
                        Workout w = doc.toObject(Workout.class);
                        if (w != null) workoutDao.insert(w);
                    }
                }));
    }
}
