package com.example.fittracker.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.fittracker.database.DateConverter;
import com.example.fittracker.database.dao.GoalDAO;
import com.example.fittracker.database.dao.UserDAO;
import com.example.fittracker.database.dao.WorkoutDAO;
import com.example.fittracker.database.entities.Goal;
import com.example.fittracker.database.entities.User;
import com.example.fittracker.database.entities.Workout;

@Database(entities = {User.class, Workout.class, Goal.class}, version = 1, exportSchema = true)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract UserDAO userDao();
    public abstract WorkoutDAO workoutDao();
    public abstract GoalDAO goalDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "fit_tracker_db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}