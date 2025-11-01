package com.example.fittracker.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fittracker.database.entities.Goal;

import java.util.List;

@Dao
public interface GoalDAO {

    // Inserir meta
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Goal goal);

    // Atualizar meta
    @Update
    int update(Goal goal);

    // Apagar meta
    @Delete
    int delete(Goal goal);

    // Obter todas as metas de um utilizador
    @Query("SELECT * FROM goals WHERE user_id = :userId")
    List<Goal> getGoalsByUser(long userId);

    // Obter meta por ID
    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    Goal getById(long id);

    // Apagar todas as metas de um utilizador
    @Query("DELETE FROM goals WHERE user_id = :userId")
    void deleteAllByUser(long userId);

    @Query("SELECT * FROM goals WHERE user_id = :userId LIMIT 1")
    Goal getByUser(long userId);
}
