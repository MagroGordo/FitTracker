package com.example.fittracker.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

import com.example.fittracker.database.entities.User;
import com.example.fittracker.database.entities.Workout;
import com.example.fittracker.database.entities.Goal;
import com.example.fittracker.database.daos.UserDAO;
import com.example.fittracker.database.daos.WorkoutDAO;
import com.example.fittracker.database.daos.GoalDAO;
import com.example.fittracker.database.DateConverter;

@Database(entities = {User.class, Workout.class, Goal.class}, version = 2)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDAO userDao();
    public abstract WorkoutDAO workoutDao();
    public abstract GoalDAO goalDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "fittracker_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
