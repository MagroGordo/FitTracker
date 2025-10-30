package com.example.fittracker.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;
import java.util.Date;

@Entity(
        tableName = "workouts",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("user_id")}
)
public class Workout {

    public static final String TYPE_RUNNING = "running";
    public static final String TYPE_WALKING = "walking";
    public static final String TYPE_CYCLING = "cycling";

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "firebase_id")
    private String firebaseId;

    @ColumnInfo(name = "user_id")
    private long userId;

    private String type;
    private double distance;
    private int duration;
    private double calories;

    @ColumnInfo(name = "avg_speed")
    private double avgSpeed;

    @ColumnInfo(name = "start_latitude")
    private double startLatitude;

    @ColumnInfo(name = "start_longitude")
    private double startLongitude;

    @ColumnInfo(name = "end_latitude")
    private double endLatitude;

    @ColumnInfo(name = "end_longitude")
    private double endLongitude;

    @ColumnInfo(name = "start_time")
    private Date startTime;

    @ColumnInfo(name = "end_time")
    private Date endTime;

    private Date date;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    private boolean synced;

    public Workout() { }

    public Workout(long userId, String type, double distance, int duration) {
        this.userId = userId;
        this.type = type;
        this.distance = distance;
        this.duration = duration;
        this.synced = false;
    }

    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public double getAvgSpeed() { return avgSpeed; }
    public void setAvgSpeed(double avgSpeed) { this.avgSpeed = avgSpeed; }

    public double getStartLatitude() { return startLatitude; }
    public void setStartLatitude(double startLatitude) { this.startLatitude = startLatitude; }

    public double getStartLongitude() { return startLongitude; }
    public void setStartLongitude(double startLongitude) { this.startLongitude = startLongitude; }

    public double getEndLatitude() { return endLatitude; }
    public void setEndLatitude(double endLatitude) { this.endLatitude = endLatitude; }

    public double getEndLongitude() { return endLongitude; }
    public void setEndLongitude(double endLongitude) { this.endLongitude = endLongitude; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }

    public void calculateAvgSpeed() {
        if (duration > 0) {
            double hours = duration / 3600.0;
            this.avgSpeed = distance / hours;
        }
    }

    public String getTypeDisplayName() {
        switch (type) {
            case TYPE_RUNNING:
                return "Corrida";
            case TYPE_WALKING:
                return "Caminhada";
            case TYPE_CYCLING:
                return "Ciclismo";
            default:
                return type;
        }
    }

    @Override
    public String toString() {
        return "Workout{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", distance=" + distance +
                ", duration=" + duration +
                ", calories=" + calories +
                ", avgSpeed=" + avgSpeed +
                '}';
    }
}
