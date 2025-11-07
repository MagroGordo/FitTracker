package com.example.fittracker.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;

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

    @ColumnInfo(name = "type")
    private String type; // 👈 novo campo

    public Goal() {
        this.dailyDistance = 5.0;
        this.dailyCalories = 300.0;
        this.type = "Intermédio";
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

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Override
    public String toString() {
        return "Goal{" +
                "id=" + id +
                ", userId=" + userId +
                ", dailyDistance=" + dailyDistance +
                ", dailyCalories=" + dailyCalories +
                ", type='" + type + '\'' +
                '}';
    }
}