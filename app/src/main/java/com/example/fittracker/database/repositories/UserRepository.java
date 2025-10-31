package com.example.fittracker.database.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fittracker.database.AppDatabase;
import com.example.fittracker.database.daos.UserDAO;
import com.example.fittracker.database.entities.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {

    private final UserDAO userDao;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor;

    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.userDao = db.userDao();
        this.firestore = FirebaseFirestore.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
    }

    // --- LOCAL DATABASE (ROOM) ---

    public LiveData<User> getUserLive(long userId) {
        return userDao.observeUserById(userId);
    }

    public void insertLocal(User user) {
        executor.execute(() -> userDao.insert(user));
    }

    public void updateLocal(User user) {
        executor.execute(() -> userDao.update(user));
    }

    public void deleteLocal(User user) {
        executor.execute(() -> userDao.delete(user));
    }

    // --- REMOTE DATABASE (FIREBASE) ---

    public void syncUserFromFirebase(String firebaseUid, MutableLiveData<User> liveUser) {
        firestore.collection("users")
                .document(firebaseUid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        executor.execute(() -> userDao.insert(user)); // Guarda localmente
                        liveUser.postValue(user);
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public void uploadUserToFirebase(User user) {
        firestore.collection("users")
                .document(user.getFirebaseUid())
                .set(user)
                .addOnSuccessListener(aVoid -> System.out.println("User sincronizado com Firebase"))
                .addOnFailureListener(Throwable::printStackTrace);
    }
}