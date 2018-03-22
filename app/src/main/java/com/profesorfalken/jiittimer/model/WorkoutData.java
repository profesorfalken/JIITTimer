package com.profesorfalken.jiittimer.model;

public class WorkoutData {
    private int workoutTime;
    private int restTime;
    private int cooldownTime;
    private int cycles;

    public WorkoutData(int workoutTime, int restTime, int cooldownTime, int cycles) {
        this.workoutTime = workoutTime;
        this.restTime = restTime;
        this.cooldownTime = cooldownTime;
        this.cycles = cycles;
    }

    public int getWorkoutTime() {
        return workoutTime;
    }

    public void setWorkoutTime(int workoutTime) {
        this.workoutTime = workoutTime;
    }

    public int getRestTime() {
        return restTime;
    }

    public void setRestTime(int restTime) {
        this.restTime = restTime;
    }

    public int getCooldownTime() {
        return cooldownTime;
    }

    public void setCooldownTime(int cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public int getCycles() {
        return cycles;
    }

    public void setCycles(int cycles) {
        this.cycles = cycles;
    }
}
