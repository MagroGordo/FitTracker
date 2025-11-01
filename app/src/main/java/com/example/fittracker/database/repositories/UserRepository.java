package com.example.fittracker.database.repositories;

import android.content.Context;

import com.example.fittracker.database.AppDatabase;
import com.example.fittracker.database.daos.UserDAO;
import com.example.fittracker.database.entities.User;
import com.google.firebase.firestore.FirebaseFirestore;

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
        firestore.collection("users")
                .document(user.getFirebaseUid())
                .set(user);
    }

    public void syncUserFromFirebase(String firebaseUid) {
        firestore.collection("users")
                .document(firebaseUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            executor.execute(() -> userDao.insert(user));
                        }
                    }
                });
    }
}
