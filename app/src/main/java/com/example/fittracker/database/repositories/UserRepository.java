package com.example.fittracker.database.repositories;

import android.content.Context;

import com.example.fittracker.database.AppDatabase;
import com.example.fittracker.database.daos.UserDAO;
import com.example.fittracker.database.entities.User;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Calendar;

public class UserRepository {

    private final UserDAO userDao;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.userDao = db.userDao();
        this.firestore = FirebaseFirestore.getInstance();
    }

    // ROOM (Local)
    public void insertLocal(User user) {
        executor.execute(() -> userDao.insert(user));
    }

    public void updateLocal(User user) {
        executor.execute(() -> userDao.update(user));
    }

    public User getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    public User getById(long id) {
        return userDao.getById(id);
    }

    public User getByFirebaseUid(String uid) {
        return userDao.getByFirebaseUid(uid);
    }

    // NOVO: obter "utilizador atual" (aqui: primeiro utilizador guardado em Room)
    public User getFirstUser() {
        return userDao.getFirstUser();
    }

    // FIREBASE upload
    public void uploadUserToFirebase(User user) {
        if (user == null || user.getFirebaseUid() == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("gender", user.getGender());
        data.put("birthday", user.getBirthday() != null ? user.getBirthday().getTime() : null);
        data.put("weight", user.getWeight());
        data.put("height", user.getHeight());
        data.put("updatedAt", System.currentTimeMillis());

        // NOVOS campos
        data.put("streak", user.getStreak());
        if (user.getLastWorkoutAt() != null) {
            data.put("lastWorkoutAt", new Timestamp(user.getLastWorkoutAt()));
        } else {
            data.put("lastWorkoutAt", null);
        }

        firestore.collection("users")
                .document(user.getFirebaseUid())
                .set(data, SetOptions.merge());
    }

    // FIREBASE -> Room sync One-shot
    public void syncUserFromFirebase(String firebaseUid) {
        if (firebaseUid == null) return;

        firestore.collection("users")
                .document(firebaseUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = new User();
                        user.setFirebaseUid(firebaseUid);
                        user.setName(doc.getString("name"));
                        user.setEmail(doc.getString("email"));
                        user.setGender(doc.getString("gender"));

                        // Lidar com birthday como Timestamp
                        try {
                            com.google.firebase.Timestamp birthdayTs = doc.getTimestamp("birthday");
                            if (birthdayTs != null) {
                                user.setBirthday(birthdayTs.toDate());
                            }
                        } catch (Exception e) {
                            android.util.Log.e("UserRepository", "Erro ao processar birthday", e);
                        }

                        Double weight = doc.getDouble("weight");
                        if (weight != null) user.setWeight(weight);

                        Double height = doc.getDouble("height");
                        if (height != null) user.setHeight(height);

                        // Lê streak e lastWorkoutAt (se existirem)
                        Long streakLong = doc.contains("streak") ? (doc.getLong("streak") != null ? doc.getLong("streak") : null) : null;
                        if (streakLong != null) user.setStreak(streakLong.intValue());

                        try {
                            com.google.firebase.Timestamp lastW = doc.getTimestamp("lastWorkoutAt");
                            if (lastW != null) user.setLastWorkoutAt(lastW.toDate());
                        } catch (Exception e) {
                            android.util.Log.e("UserRepository", "Erro ao processar lastWorkoutAt", e);
                        }

                        user.setSynced(true);

                        executor.execute(() -> {
                            User existing = userDao.getByFirebaseUid(firebaseUid);
                            if (existing != null) {
                                user.setId(existing.getId());
                                userDao.update(user);
                            } else {
                                userDao.insert(user);
                            }
                        });
                    }
                });
    }

    // Callback para fetch direto
    public interface UserLoadCallback {
        void onLoaded(User user);
        void onError(Exception e);
    }

    // Ler diretamente do Firestore e persistir em Room
    public void fetchUserFromFirestore(String firebaseUid, UserLoadCallback cb) {
        if (firebaseUid == null) { if (cb != null) cb.onLoaded(null); return; }
        firestore.collection("users")
                .document(firebaseUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { if (cb != null) cb.onLoaded(null); return; }

                    User user = new User();
                    user.setFirebaseUid(firebaseUid);
                    user.setName(doc.getString("name"));
                    user.setEmail(doc.getString("email"));
                    user.setGender(doc.getString("gender"));

                    // Lidar com birthday como Timestamp
                    try {
                        com.google.firebase.Timestamp birthdayTs = doc.getTimestamp("birthday");
                        if (birthdayTs != null) {
                            user.setBirthday(birthdayTs.toDate());
                        }
                    } catch (Exception e) {
                        android.util.Log.e("UserRepository", "Erro ao processar birthday", e);
                    }
                    Double weight = doc.getDouble("weight");
                    if (weight != null) user.setWeight(weight);
                    Double height = doc.getDouble("height");
                    if (height != null) user.setHeight(height);

                    // Lê streak e lastWorkoutAt (se existirem)
                    Long streakLong = doc.contains("streak") ? (doc.getLong("streak") != null ? doc.getLong("streak") : null) : null;
                    if (streakLong != null) user.setStreak(streakLong.intValue());

                    try {
                        com.google.firebase.Timestamp lastW = doc.getTimestamp("lastWorkoutAt");
                        if (lastW != null) user.setLastWorkoutAt(lastW.toDate());
                    } catch (Exception e) {
                        android.util.Log.e("UserRepository", "Erro ao processar lastWorkoutAt", e);
                    }

                    if (cb != null) cb.onLoaded(user);

                    // Persistir localmente
                    executor.execute(() -> {
                        User existing = userDao.getByFirebaseUid(firebaseUid);
                        if (existing != null) {
                            user.setId(existing.getId());
                            userDao.update(user);
                        } else {
                            userDao.insert(user);
                        }
                    });
                })
                .addOnFailureListener(e -> { if (cb != null) cb.onError(e); });
    }

    // --------------------
    // NOVO: registar um treino (atualiza streak)
    // --------------------
    public void recordWorkout(String firebaseUid, Date workoutDate) {
        if (firebaseUid == null) return;
        final Date wDate = workoutDate != null ? workoutDate : new Date();

        executor.execute(() -> {
            User user = userDao.getByFirebaseUid(firebaseUid);
            if (user == null) return;

            // Normaliza para início do dia (midnight) para comparar apenas data
            Date normalizedWorkout = truncateToStartOfDay(wDate);
            Date last = user.getLastWorkoutAt();
            int newStreak = 1;

            if (last != null) {
                Date normalizedLast = truncateToStartOfDay(last);
                long diffMillis = normalizedWorkout.getTime() - normalizedLast.getTime();
                long daysBetween = TimeUnit.MILLISECONDS.toDays(diffMillis);

                if (daysBetween == 0) {
                    // mesmo dia -> não incrementa
                    return;
                } else if (daysBetween == 1) {
                    newStreak = user.getStreak() + 1;
                } else {
                    // gap maior que 1 dia -> reset para 1
                    newStreak = 1;
                }
            } else {
                // sem último -> começa streak com 1
                newStreak = 1;
            }

            user.setStreak(newStreak);
            user.setLastWorkoutAt(wDate);
            user.setSynced(false);
            userDao.update(user);

            // Envia para Firestore os campos novos
            Map<String, Object> data = new HashMap<>();
            data.put("streak", newStreak);
            data.put("lastWorkoutAt", new Timestamp(wDate));
            data.put("updatedAt", System.currentTimeMillis());

            firestore.collection("users")
                    .document(firebaseUid)
                    .set(data, SetOptions.merge());
        });
    }

    // Helper: truncar data para início do dia respetando timezone local
    private Date truncateToStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}