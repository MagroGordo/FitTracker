package com.example.fittracker.database;

import com.example.fittracker.database.entities.Goal;
import com.example.fittracker.database.entities.Workout;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Helper para calcular progresso de objetivos diários
 */
public class GoalProgressHelper {

    /**
     * Resultado do cálculo de progresso
     */
    public static class ProgressResult {
        private final int percentage;
        private final double currentDistance;
        private final double currentCalories;
        private final double targetDistance;
        private final double targetCalories;
        private final boolean isCompleted;

        public ProgressResult(int percentage, double currentDistance, double currentCalories,
                              double targetDistance, double targetCalories) {
            this.percentage = Math.min(100, Math.max(0, percentage));
            this.currentDistance = currentDistance;
            this.currentCalories = currentCalories;
            this.targetDistance = targetDistance;
            this.targetCalories = targetCalories;
            this.isCompleted = percentage >= 100;
        }

        public int getPercentage() { return percentage; }
        public double getCurrentDistance() { return currentDistance; }
        public double getCurrentCalories() { return currentCalories; }
        public double getTargetDistance() { return targetDistance; }
        public double getTargetCalories() { return targetCalories; }
        public boolean isCompleted() { return isCompleted; }

        public String getFormattedProgress() {
            return percentage + "%";
        }

        public String getFormattedDistance() {
            return String.format(Locale.getDefault(), "%.2f / %.2f km",
                    currentDistance, targetDistance);
        }

        public String getFormattedCalories() {
            return String.format(Locale.getDefault(), "%.0f / %.0f kcal",
                    currentCalories, targetCalories);
        }
    }

    /**
     * Calcula o progresso do objetivo com base nos treinos de hoje
     */
    public static ProgressResult calculateProgress(Goal goal, List<Workout> todayWorkouts) {
        if (goal == null) {
            return new ProgressResult(0, 0, 0, 0, 0);
        }

        double targetDistance = goal.getDailyDistance();
        double targetCalories = goal.getDailyCalories();

        if (todayWorkouts == null || todayWorkouts.isEmpty()) {
            return new ProgressResult(0, 0, 0, targetDistance, targetCalories);
        }

        // Soma distância e calorias dos treinos de hoje
        double currentDistance = 0;
        double currentCalories = 0;

        for (Workout workout : todayWorkouts) {
            currentDistance += workout.getDistance();
            currentCalories += workout.getCalories();
        }

        // Calcula progresso (média entre distância e calorias)
        double distanceProgress = (currentDistance / targetDistance) * 100;
        double caloriesProgress = (currentCalories / targetCalories) * 100;
        int averageProgress = (int) Math.round((distanceProgress + caloriesProgress) / 2);

        return new ProgressResult(averageProgress, currentDistance, currentCalories,
                targetDistance, targetCalories);
    }

    /**
     * Verifica se uma data é hoje
     */
    public static boolean isToday(Date date) {
        if (date == null) return false;

        Calendar today = Calendar.getInstance();
        Calendar check = Calendar.getInstance();
        check.setTime(date);

        return today.get(Calendar.YEAR) == check.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == check.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Filtra apenas os treinos de hoje
     */
    public static List<Workout> filterTodayWorkouts(List<Workout> allWorkouts) {
        if (allWorkouts == null) return null;

        java.util.ArrayList<Workout> todayWorkouts = new java.util.ArrayList<>();
        for (Workout workout : allWorkouts) {
            Date workoutDate = workout.getDate();
            if (workoutDate == null) workoutDate = workout.getStartTime();

            if (isToday(workoutDate)) {
                todayWorkouts.add(workout);
            }
        }
        return todayWorkouts;
    }

    /**
     * Normaliza uma data para o início do dia (00:00:00)
     */
    public static Date truncateToStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}