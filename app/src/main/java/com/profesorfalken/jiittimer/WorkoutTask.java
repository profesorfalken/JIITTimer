package com.profesorfalken.jiittimer;

import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

import com.profesorfalken.jiittimer.util.JiitTimeUtils;

class WorkoutTask {
    private static final long COUNTDOWN_INTERVAL = 500;

    private final CountDownTimer timer;
    private final long duration;
    private final TextView textViewToUpdate;
    private final WorkoutTask next;
    private boolean finished;
    private boolean increateCycle;

    public WorkoutTask(final long duration, final TextView textViewToUpdate, final WorkoutTask next) {
        this.duration = duration;
        this.textViewToUpdate = textViewToUpdate;
        this.next = next;

        this.timer = new CountDownTimer(duration * 1000, COUNTDOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                textViewToUpdate.setText(String.format("%d", millisUntilFinished / 1000));
            }

            public void onFinish() {
                textViewToUpdate.setText("0");
                if (next != null) {
                    next.start();
                } else {
                    Log.i("WorkoutTask", "The End");
                }
            }
        };
    }

    public void increaseCycle() {
        this.increateCycle = true;
    }

    public void start() {
        if (timer != null && !finished) {
            if (this.increateCycle) {

            }

            timer.start();
        }
    }
}