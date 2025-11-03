package com.example.fittracker.database.repositories;

import android.content.Context;

import com.example.fittracker.database.AppDatabase;
import com.example.fittracker.database.daos.WorkoutDAO;
import com.example.fittracker.database.entities.Workout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public Workout getLastWorkoutByFirebaseUid(String firebaseUid) {
        return workoutDao.getLastWorkoutByFirebaseUid(firebaseUid);
    }

    public List<Workout> getAllWorkoutsByFirebaseUid(String firebaseUid) {
        return workoutDao.getAllWorkoutsByFirebaseUid(firebaseUid);
    }

    public List<Workout> getTodayWorkoutsByFirebaseUid(String firebaseUid, Date startOfDay) {
        return workoutDao.getTodayWorkoutsByFirebaseUid(firebaseUid, startOfDay);
    }

    /**
     * Busca treinos de hoje por User ID
     */
    public List<Workout> getTodayWorkoutsByUserId(long userId, Date startOfDay) {
        return workoutDao.getTodayWorkoutsByUserId(userId, startOfDay);
    }

    // COMBINED: Insert local + sync to Firebase
    public void insertAndSync(Workout workout, String firebaseUid) {
        executor.execute(() -> {
            // Guardar o firebaseUid no workout ANTES de inserir
            workout.setFirebaseUid(firebaseUid);

            // Inserir e obter o ID gerado pelo Room
            long generatedId = workoutDao.insert(workout);
            workout.setId(generatedId);

            android.util.Log.d("WorkoutRepository", "Workout inserted locally with ID: " + generatedId + " and firebaseUid: " + firebaseUid);

            // Agora fazer upload para Firestore com o ID correto
            if (firebaseUid != null && generatedId > 0) {
                uploadToFirebase(workout, firebaseUid);
                android.util.Log.d("WorkoutRepository", "Uploading to Firestore with ID: " + generatedId + " for user: " + firebaseUid);
            } else {
                android.util.Log.w("WorkoutRepository", "Firebase UID is null or ID invalid, workout not uploaded");
            }
        });
    }

    // FIREBASE (opcional)
    public void uploadToFirebase(Workout workout, String firebaseUid) {
        if (workout == null || firebaseUid == null) {
            android.util.Log.e("WorkoutRepository", "Workout or firebaseUid is null");
            return;
        }

        String docId = workout.getFirebaseId() != null ? workout.getFirebaseId() : String.valueOf(workout.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("firebaseUid", firebaseUid);
        data.put("userId", workout.getUserId());
        data.put("type", workout.getType());
        data.put("distance", workout.getDistance());
        data.put("duration", workout.getDuration());
        data.put("calories", workout.getCalories());
        data.put("avgSpeed", workout.getAvgSpeed());
        data.put("startLatitude", workout.getStartLatitude());
        data.put("startLongitude", workout.getStartLongitude());
        data.put("endLatitude", workout.getEndLatitude());
        data.put("endLongitude", workout.getEndLongitude());

        // Usar Timestamp do Firestore em vez de Long
        data.put("startTime", workout.getStartTime() != null ? new Timestamp(workout.getStartTime()) : null);
        data.put("endTime", workout.getEndTime() != null ? new Timestamp(workout.getEndTime()) : null);
        data.put("date", workout.getDate() != null ? new Timestamp(workout.getDate()) : null);
        data.put("createdAt", workout.getCreatedAt() != null ? new Timestamp(workout.getCreatedAt()) : new Timestamp(new java.util.Date()));

        firestore.collection("workouts")
                .document(docId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("WorkoutRepository", "✅ Workout uploaded successfully to Firestore with ID: " + docId);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("WorkoutRepository", "❌ Failed to upload workout to Firestore", e);
                });
    }

    public interface SyncCallback {
        void onComplete();
        void onError(Exception e);
    }

    public void syncFromFirebase(String firebaseUid, SyncCallback callback) {
        if (firebaseUid == null) {
            if (callback != null) callback.onError(new Exception("Firebase UID is null"));
            return;
        }

        android.util.Log.d("WorkoutRepository", "🔄 Syncing workouts from Firestore for user: " + firebaseUid);

        firestore.collection("workouts")
                .whereEqualTo("firebaseUid", firebaseUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    android.util.Log.d("WorkoutRepository", "📥 Found " + snapshot.size() + " workouts in Firestore");

                    executor.execute(() -> {
                        try {
                            snapshot.getDocuments().forEach(doc -> {
                                Workout w = new Workout();
                                w.setFirebaseId(doc.getId());

                                // IMPORTANTE: Guardar o firebaseUid
                                w.setFirebaseUid(firebaseUid);

                                Number userIdNum = (Number) doc.get("userId");
                                if (userIdNum != null) w.setUserId(userIdNum.longValue());

                                w.setType((String) doc.get("type"));
                                Double distance = doc.getDouble("distance");
                                if (distance != null) w.setDistance(distance);
                                Number duration = (Number) doc.get("duration");
                                if (duration != null) w.setDuration(duration.intValue());
                                Double calories = doc.getDouble("calories");
                                if (calories != null) w.setCalories(calories);
                                Double avgSpeed = doc.getDouble("avgSpeed");
                                if (avgSpeed != null) w.setAvgSpeed(avgSpeed);

                                Double slat = doc.getDouble("startLatitude");
                                if (slat != null) w.setStartLatitude(slat);
                                Double slon = doc.getDouble("startLongitude");
                                if (slon != null) w.setStartLongitude(slon);
                                Double elat = doc.getDouble("endLatitude");
                                if (elat != null) w.setEndLatitude(elat);
                                Double elon = doc.getDouble("endLongitude");
                                if (elon != null) w.setEndLongitude(elon);

                                // Ler Timestamps do Firestore
                                Timestamp startTs = doc.getTimestamp("startTime");
                                if (startTs != null) w.setStartTime(startTs.toDate());
                                Timestamp endTs = doc.getTimestamp("endTime");
                                if (endTs != null) w.setEndTime(endTs.toDate());
                                Timestamp dateTs = doc.getTimestamp("date");
                                if (dateTs != null) w.setDate(dateTs.toDate());
                                Timestamp createdTs = doc.getTimestamp("createdAt");
                                if (createdTs != null) w.setCreatedAt(createdTs.toDate());

                                long insertedId = workoutDao.insert(w);
                                android.util.Log.d("WorkoutRepository", "✅ Workout synced to Room with ID: " + insertedId);
                            });

                            if (callback != null) callback.onComplete();
                        } catch (Exception e) {
                            android.util.Log.e("WorkoutRepository", "❌ Error syncing workouts", e);
                            if (callback != null) callback.onError(e);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("WorkoutRepository", "❌ Failed to fetch workouts from Firestore", e);
                    if (callback != null) callback.onError(e);
                });
    }
}