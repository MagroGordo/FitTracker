package com.example.fittracker.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;
import java.util.Date;

@Entity(
        tableName = "goals",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("user_id")}
)
public class Goal {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "daily_distance")
    private double dailyDistance;

    @ColumnInfo(name = "daily_calories")
    private double dailyCalories;

    @ColumnInfo(name = "daily_duration")
    private int dailyDuration;

    @ColumnInfo(name = "weekly_workouts")
    private int weeklyWorkouts;

    @ColumnInfo(name = "weekly_distance")
    private double weeklyDistance;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    public Goal() {
        this.dailyDistance = 5.0;
        this.dailyCalories = 300.0;
        this.dailyDuration = 1800;
        this.weeklyWorkouts = 3;
        this.weeklyDistance = 20.0;
    }

    public Goal(long userId) {
        this();
        this.userId = userId;
    }

    // Getters e setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public double getDailyDistance() { return dailyDistance; }
    public void setDailyDistance(double dailyDistance) { this.dailyDistance = dailyDistance; }

    public double getDailyCalories() { return dailyCalories; }
    public void setDailyCalories(double dailyCalories) { this.dailyCalories = dailyCalories; }

    public int getDailyDuration() { return dailyDuration; }
    public void setDailyDuration(int dailyDuration) { this.dailyDuration = dailyDuration; }

    public int getWeeklyWorkouts() { return weeklyWorkouts; }
    public void setWeeklyWorkouts(int weeklyWorkouts) { this.weeklyWorkouts = weeklyWorkouts; }

    public double getWeeklyDistance() { return weeklyDistance; }
    public void setWeeklyDistance(double weeklyDistance) { this.weeklyDistance = weeklyDistance; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Goal{" +
                "id=" + id +
                ", userId=" + userId +
                ", dailyDistance=" + dailyDistance +
                ", dailyCalories=" + dailyCalories +
                ", weeklyWorkouts=" + weeklyWorkouts +
                '}';
    }
}