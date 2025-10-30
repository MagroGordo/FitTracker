package com.example.fittracker.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fittracker.database.entities.Workout;

import java.util.Date;
import java.util.List;

@Dao
public interface WorkoutDAO {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Workout workout);

    @Update
    int update(Workout workout);

    @Delete
    int delete(Workout workout);

    // Último treino do utilizador (para Dashboard)
    @Query("SELECT * FROM workouts WHERE user_id = :userId ORDER BY start_time DESC LIMIT 1")
    Workout getLastWorkout(long userId);

    // Histórico por utilizador
    @Query("SELECT * FROM workouts WHERE user_id = :userId ORDER BY start_time DESC")
    List<Workout> listByUser(long userId);

    // Filtro por tipo
    @Query("SELECT * FROM workouts WHERE user_id = :userId AND type = :type ORDER BY start_time DESC")
    List<Workout> listByUserAndType(long userId, String type);

    // Workouts entre datas
    @Query("SELECT * FROM workouts WHERE user_id = :userId AND date BETWEEN :from AND :to ORDER BY date DESC")
    List<Workout> listBetweenDates(long userId, Date from, Date to);

    // Marcar como sincronizado (quando subires ao Firebase)
    @Query("UPDATE workouts SET synced = 1, firebase_id = :firebaseId WHERE id = :id")
    void markSynced(long id, String firebaseId);

    // Atualizar métricas ao terminar treino
    @Query("UPDATE workouts SET distance = :distance, duration = :duration, calories = :calories, avg_speed = :avgSpeed, end_time = :endTime, synced = 0 WHERE id = :id")
    void finalizeWorkout(long id, double distance, int duration, double calories, double avgSpeed, Date endTime);
}