package com.example.fittracker.database.daos;

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Workout workout);

    @Update
    int update(Workout workout);

    @Delete
    int delete(Workout workout);

    @Query("SELECT * FROM workouts WHERE id = :id LIMIT 1")
    Workout getById(long id);

    @Query("SELECT * FROM workouts WHERE user_id = :userId ORDER BY start_time DESC")
    List<Workout> getAllWorkoutsForUser(long userId);

    @Query("SELECT * FROM workouts WHERE user_id = :userId ORDER BY start_time DESC LIMIT 1")
    Workout getLastWorkout(long userId);

    @Query("DELETE FROM workouts WHERE user_id = :userId")
    void deleteAllByUser(long userId);

    @Query("SELECT * FROM workouts WHERE user_id = :userId")
    List<Workout> listByUser(long userId);

    @Query("SELECT * FROM workouts WHERE user_id = :userId AND type = :type")
    List<Workout> listByUserAndType(long userId, String type);

    @Query("SELECT * FROM workouts WHERE user_id = :userId AND start_time BETWEEN :from AND :to")
    List<Workout> listBetweenDates(long userId, Date from, Date to);

    @Query("SELECT * FROM workouts WHERE firebase_uid = :firebaseUid ORDER BY date DESC LIMIT 1")
    Workout getLastWorkoutByFirebaseUid(String firebaseUid);

    @Query("SELECT * FROM workouts WHERE firebase_uid = :firebaseUid ORDER BY date DESC")
    List<Workout> getAllWorkoutsByFirebaseUid(String firebaseUid);
}