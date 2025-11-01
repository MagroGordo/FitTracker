package com.example.fittracker.database.repositories;

import android.content.Context;

import com.example.fittracker.database.AppDatabase;
import com.example.fittracker.database.daos.WorkoutDAO;
import com.example.fittracker.database.entities.Workout;
import com.google.firebase.firestore.FirebaseFirestore;

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

    // FIREBASE (opcional)
    public void uploadToFirebase(Workout workout, String firebaseUid) {
        if (workout == null || firebaseUid == null) return;
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
        data.put("startTime", workout.getStartTime() != null ? workout.getStartTime().getTime() : null);
        data.put("endTime", workout.getEndTime() != null ? workout.getEndTime().getTime() : null);
        data.put("date", workout.getDate() != null ? workout.getDate().getTime() : null);
        data.put("createdAt", workout.getCreatedAt() != null ? workout.getCreatedAt().getTime() : System.currentTimeMillis());

        firestore.collection("workouts")
                .document(docId)
                .set(data);
    }

    public void syncFromFirebase(String firebaseUid) {
        if (firebaseUid == null) return;
        firestore.collection("workouts")
                .whereEqualTo("firebaseUid", firebaseUid)
                .get()
                .addOnSuccessListener(snapshot -> executor.execute(() -> {
                    snapshot.getDocuments().forEach(doc -> {
                        Workout w = new Workout();
                        w.setFirebaseId(doc.getId());

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

                        Number startMs = (Number) doc.get("startTime");
                        if (startMs != null) w.setStartTime(new java.util.Date(startMs.longValue()));
                        Number endMs = (Number) doc.get("endTime");
                        if (endMs != null) w.setEndTime(new java.util.Date(endMs.longValue()));
                        Number dateMs = (Number) doc.get("date");
                        if (dateMs != null) w.setDate(new java.util.Date(dateMs.longValue()));
                        Number createdAtMs = (Number) doc.get("createdAt");
                        if (createdAtMs != null) w.setCreatedAt(new java.util.Date(createdAtMs.longValue()));

                        workoutDao.insert(w);
                    });
                }));
    }
}