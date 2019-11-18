package com.profesorfalken.jiittimer.gui.helper;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.profesorfalken.jiittimer.R;
import com.profesorfalken.jiittimer.model.WorkoutViewModel;

public class LongClickUpdateManager {

    private final Handler repeatUpdateHandler = new Handler(Looper.getMainLooper());
    private static final int REP_DELAY = 50;
    private volatile boolean autoIncrement = false;
    private volatile boolean autoDecrement = false;

    //Fields initialized in constructor
    private WorkoutViewModel viewModel;
    private String workoutTimeTag;
    private String restTimeTag;
    private String cooldownTimeTag;
    private String cyclesTag;


    public LongClickUpdateManager(Context context, WorkoutViewModel viewModel) {
        this.viewModel = viewModel;

        workoutTimeTag = context.getResources().getString(R.string.tag_workout_time_field);
        restTimeTag = context.getResources().getString(R.string.tag_rest_time_field);
        cooldownTimeTag = context.getResources().getString(R.string.tag_cooldown_time_field);
        cyclesTag = context.getResources().getString(R.string.tag_cycles_field);
    }
    
    public void startLongClickUpdate(String fieldTag, boolean decrementOnUpdate) {
        if (!autoIncrement && !autoDecrement) {
            autoIncrement = !decrementOnUpdate;
            autoDecrement = !autoIncrement;
            Log.d("LongClickUpdateManager", "Creating updater!!");
            LongClickUpdater updater = new LongClickUpdater(fieldTag);
            repeatUpdateHandler.post(updater);
        }
    }

    public void stopLongClickUpdate() {
        autoIncrement = autoDecrement = false;
    }


    class LongClickUpdater implements Runnable {
        private String fieldTag;

        public LongClickUpdater(String fieldTag) {
            this.fieldTag = fieldTag;
            Log.d("LongClickUpdateManager", "Thread created");
        }

        public void run() {
            if (autoIncrement) {
                increaseValue();
                Log.d("LongClickUpdateManager", "Updating!! " + this.hashCode());
                repeatUpdateHandler.postDelayed(this, REP_DELAY);
            } else if (autoDecrement) {
                decreaseValue();
                repeatUpdateHandler.postDelayed(this, REP_DELAY);
            }

            Log.d("LongClickUpdateManager", "End updater");
        }

        private void increaseValue() {
            if (fieldTag.equals(workoutTimeTag)) {
                viewModel.increaseWorkoutTime();
            } else if (fieldTag.equals(restTimeTag)) {
                viewModel.increaseRestime();
            } else if (fieldTag.equals(cooldownTimeTag)) {
                viewModel.increaseCooldownTime();
            } else if (fieldTag.equals(cyclesTag)) {
                viewModel.increaseCycles();
            } else {
                throw new IllegalStateException("Unexpected value: " + fieldTag);
            }
        }

        private void decreaseValue() {
            if (fieldTag.equals(workoutTimeTag)) {
                viewModel.decreaseWorkoutTime();
            } else if (fieldTag.equals(restTimeTag)) {
                viewModel.decreaseRestTime();
            } else if (fieldTag.equals(cooldownTimeTag)) {
                viewModel.decreaseCooldownTime();
            } else if (fieldTag.equals(cyclesTag)) {
                viewModel.decreaseCycles();
            } else {
                throw new IllegalStateException("Unexpected value: " + fieldTag);
            }
        }
    }
}

