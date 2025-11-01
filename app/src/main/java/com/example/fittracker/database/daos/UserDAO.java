package com.example.fittracker.database.daos;

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

    // Inserir novo utilizador
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(User user);

    // Atualizar dados do utilizador
    @Update
    int update(User user);

    // Apagar utilizador
    @Delete
    int delete(User user);

    // Obter utilizador por ID
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getById(long id);

    // Obter utilizador por e-mail (para login)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    // Obter utilizador por Firebase UID
    @Query("SELECT * FROM users WHERE firebase_uid = :uid LIMIT 1")
    User getByFirebaseUid(String uid);

    // Obter todos os utilizadores (opcional)
    @Query("SELECT * FROM users ORDER BY name ASC")
    List<User> getAllUsers();

    // Atualizar apenas peso, altura ou data de nascimento
    @Query("UPDATE users SET weight = :weight, height = :height, birthday = :birthday WHERE id = :id")
    int updatePhysicalData(long id, double weight, double height, java.util.Date birthday);

    @Query("SELECT * FROM users LIMIT 1")
    User getFirstUser();
}