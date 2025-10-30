package com.example.fittracker.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fittracker.database.entities.Goal;

@Dao
public interface GoalDAO {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Goal goal);

    @Update
    int update(Goal goal);

    @Query("SELECT * FROM goals WHERE user_id = :userId LIMIT 1")
    Goal getByUser(long userId);

    // Upsert manual para 1–1
    default long upsert(Goal existing, Goal toSave) {
        if (existing == null) {
            return insert(toSave);
        } else {
            toSave.setId(existing.getId());
            update(toSave);
            return existing.getId();
        }
    }
}