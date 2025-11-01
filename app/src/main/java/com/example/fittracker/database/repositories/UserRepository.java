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

    public User getByFirebaseUid(String uid) {
        return userDao.getByFirebaseUid(uid);
    }

    // FIREBASE upload - envia Date (Timestamp nativo)
    public void uploadUserToFirebase(User user) {
        if (user == null || user.getFirebaseUid() == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("gender", user.getGender());
        // Guardar como Date para Firestore gravar como Timestamp
        data.put("birthday", user.getBirthday() != null ? user.getBirthday() : null);
        data.put("weight", user.getWeight());
        data.put("height", user.getHeight());
        data.put("updatedAt", new Date());

        // Opcional: se o doc ainda não tem createdAt, mantém se já existir
        if (user.getCreatedAt() != null) {
            data.put("createdAt", user.getCreatedAt());
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

                        // birthday pode estar como Timestamp (novo) ou Long (antigo)
                        Date birthday = null;
                        Object b = doc.get("birthday");
                        if (b instanceof Timestamp) {
                            birthday = ((Timestamp) b).toDate();
                        } else if (b instanceof Long) {
                            birthday = new Date((Long) b);
                        }
                        user.setBirthday(birthday);

                        Double weight = doc.getDouble("weight");
                        if (weight != null) user.setWeight(weight);

                        Double height = doc.getDouble("height");
                        if (height != null) user.setHeight(height);

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

                    // birthday como Timestamp (preferencial) ou Long (retrocompatibilidade)
                    Date birthday = null;
                    Object b = doc.get("birthday");
                    if (b instanceof Timestamp) {
                        birthday = ((Timestamp) b).toDate();
                    } else if (b instanceof Long) {
                        birthday = new Date((Long) b);
                    }
                    user.setBirthday(birthday);

                    Double weight = doc.getDouble("weight");
                    if (weight != null) user.setWeight(weight);

                    Double height = doc.getDouble("height");
                    if (height != null) user.setHeight(height);

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
}