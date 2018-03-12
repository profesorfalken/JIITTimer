package com.profesorfalken.jiittimer.counter;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.profesorfalken.jiittimer.MainActivity;

public class WorkoutTask {
    private static final long COUNTDOWN_INTERVAL = 1000;

    private final ThreadedCountDownTimer timer;
    private final long duration;
    private final TextView textViewToUpdate;
    private boolean increaseCycle;
    private TextView cycleCountTextView;

    public WorkoutTask(final Activity contextActivity, final long duration, final TextView textViewToUpdate, final WorkoutTask next) {
        this.duration = duration;
        this.textViewToUpdate = textViewToUpdate;

        this.timer = new ThreadedCountDownTimer((duration-1) * 1000, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick() {
                ((MainActivity) contextActivity).decreaseTotalTime();
                Log.i("WorkoutTask", "Onclick captured!");
            }

            @Override
            public void onFinish() {
                if (next != null) {
                    ((MainActivity) contextActivity).decreaseTotalTime();
                    next.start();
                } else {
                    textViewToUpdate.setText("FINISH!");
                    ((MainActivity) contextActivity).toggleTimerMode();
                }
                Log.i("WorkoutTask", "OnFinish captured!");
            }
        };
    }

    public void increaseCycle(TextView cycleCountTextView) {
        this.increaseCycle = true;
        this.cycleCountTextView = cycleCountTextView;
    }

    public void start() {
        if (timer != null) {
            if (this.increaseCycle) {
                String cycleText = this.cycleCountTextView.getText().toString();
                String[] cycleData = cycleText.split("/");

                this.cycleCountTextView.setText(String.valueOf(Integer.valueOf(cycleData[0]) + 1) + "/" + cycleData[1]);
            }

            this.textViewToUpdate.setText(String.valueOf(this.duration));

            timer.start();
        }
    }
}