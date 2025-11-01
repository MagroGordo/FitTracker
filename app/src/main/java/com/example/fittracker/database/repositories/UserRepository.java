package com.example.fittracker.database.repositories;

import android.content.Context;

import com.example.fittracker.database.AppDatabase;
import com.example.fittracker.database.daos.UserDAO;
import com.example.fittracker.database.entities.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    // FIREBASE
    public void uploadUserToFirebase(User user) {
        if (user == null || user.getFirebaseUid() == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("name", user.getName());
        data.put("email", user.getEmail()); // opcional
        data.put("gender", user.getGender());
        data.put("birthday", user.getBirthday() != null ? user.getBirthday().getTime() : null);
        data.put("weight", user.getWeight());
        data.put("height", user.getHeight());
        data.put("updatedAt", System.currentTimeMillis());

        firestore.collection("users")
                .document(user.getFirebaseUid()) // id = UID
                .set(data, SetOptions.merge());  // merge para não perder campos
    }

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

                        Long birthdayMs = doc.getLong("birthday");
                        if (birthdayMs != null) user.setBirthday(new Date(birthdayMs));

                        Double weight = doc.getDouble("weight");
                        if (weight != null) user.setWeight(weight);

                        Double height = doc.getDouble("height");
                        if (height != null) user.setHeight(height);

                        user.setSynced(true);

                        executor.execute(() -> {
                            // Preferência: conciliar por email quando existir, senão inserir
                            if (user.getEmail() != null) {
                                User existing = userDao.getUserByEmail(user.getEmail());
                                if (existing != null) {
                                    user.setId(existing.getId());
                                    userDao.update(user);
                                } else {
                                    userDao.insert(user);
                                }
                            } else {
                                // Sem email, tenta inserir
                                userDao.insert(user);
                            }
                        });
                    }
                });
    }
}