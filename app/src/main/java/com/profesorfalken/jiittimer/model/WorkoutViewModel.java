package com.profesorfalken.jiittimer.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

public class WorkoutViewModel extends ViewModel {
    private MutableLiveData<Integer> workoutTime = null;

    public LiveData<Integer> getWorkoutTime() {
        if (workoutTime == null) {
            workoutTime = new MutableLiveData<>();
            workoutTime.setValue(0);
        }
        return workoutTime;
    }

    public void increaseWorkoutTime() {
        increaseLiveDataIntValue(workoutTime);
    }

    public void decreaseWorkoutTime() {
        decreaseLiveDataIntValue(workoutTime);
    }

    private MutableLiveData<Integer> restTime = null;

    public LiveData<Integer> getRestTime() {
        if (restTime == null) {
            restTime = new MutableLiveData<>();
            restTime.setValue(0);
        }
        return restTime;
    }

    public void increaseRestime() {
        increaseLiveDataIntValue(restTime);
    }

    public void decreaseRestTime() {
        decreaseLiveDataIntValue(restTime);
    }

    private MutableLiveData<Integer> cooldownTime = null;

    public LiveData<Integer> getCooldownTime() {
        if (cooldownTime == null) {
            cooldownTime = new MutableLiveData<>();
            cooldownTime.setValue(0);
        }
        return cooldownTime;
    }

    public void increaseCooldownTime() {
        increaseLiveDataIntValue(cooldownTime);
    }

    public void decreaseCooldownTime() {
        decreaseLiveDataIntValue(cooldownTime);
    }

    private MutableLiveData<Integer> cycles = null;

    public LiveData<Integer> getCycles() {
        if (cycles == null) {
            cycles = new MutableLiveData<>();
            cycles.setValue(0);
        }
        return cycles;
    }

    public void increaseCycles() {
        increaseLiveDataIntValue(cycles);
        Log.d("LongClickUpdateManager", "Increasing cycle to " + cycles.getValue());
    }

    public void decreaseCycles() {
        decreaseLiveDataIntValue(cycles);
    }

    private void increaseLiveDataIntValue(MutableLiveData<Integer> liveData) {
        int currentValue = liveData.getValue();
        liveData.setValue(currentValue + 1);
    }

    private void decreaseLiveDataIntValue(MutableLiveData<Integer> liveData) {
        int currentValue = liveData.getValue();
        if (currentValue > 0) {
            liveData.setValue(currentValue - 1);
        }
    }

}
