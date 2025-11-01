package com.example.fittracker.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.fittracker.database.entities.Workout;

import java.util.Date;
import java.util.List;

@Dao
public interface WorkoutDAO {

    // Inserir treino
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Workout workout);

    // Atualizar treino
    @Update
    int update(Workout workout);

    // Apagar treino
    @Delete
    int delete(Workout workout);

    // Obter treino por ID
    @Query("SELECT * FROM workouts WHERE id = :id LIMIT 1")
    Workout getById(long id);

    // Obter todos os treinos de um utilizador
    @Query("SELECT * FROM workouts WHERE user_id = :userId ORDER BY start_time DESC")
    List<Workout> getAllWorkoutsForUser(long userId);

    // Obter o último treino de um utilizador
    @Query("SELECT * FROM workouts WHERE user_id = :userId ORDER BY start_time DESC LIMIT 1")
    Workout getLastWorkout(long userId);

    // Apagar todos os treinos de um utilizador (opcional)
    @Query("DELETE FROM workouts WHERE user_id = :userId")
    void deleteAllByUser(long userId);

    // FIXED: Added @Query annotation
    @Query("SELECT * FROM workouts WHERE user_id = :userId")
    List<Workout> listByUser(long userId);

    // FIXED: Added @Query annotation (assuming a 'type' column exists)
    @Query("SELECT * FROM workouts WHERE user_id = :userId AND type = :type")
    List<Workout> listByUserAndType(long userId, String type);

    // FIXED: Added @Query annotation (assuming a 'start_time' column exists)
    @Query("SELECT * FROM workouts WHERE user_id = :userId AND start_time BETWEEN :from AND :to")
    List<Workout> listBetweenDates(long userId, Date from, Date to);
}