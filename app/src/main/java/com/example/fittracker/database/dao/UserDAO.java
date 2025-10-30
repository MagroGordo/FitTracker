package com.example.fittracker.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fittracker.database.entities.User;

import java.util.List;

@Dao
public interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(User user);

    @Update
    int update(User user);

    @Delete
    int delete(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getById(long id);

    @Query("UPDATE users SET last_login = :ts WHERE id = :id")
    void updateLastLogin(long id, java.util.Date ts);

    @Query("SELECT * FROM users ORDER BY created_at DESC")
    List<User> listAll();
}