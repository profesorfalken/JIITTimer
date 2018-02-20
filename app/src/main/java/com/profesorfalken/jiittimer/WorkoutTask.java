package com.profesorfalken.jiittimer;

import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.profesorfalken.jiittimer.util.JiitTimeUtils;

/**
 * Created by Javier on 11/02/2018.
 */

class WorkoutTask {
    private static final long COUNTDOWN_INTERVAL = 500;

    private final CountDownTimer timer;
    private final long duration;
    private final TextView textViewToUpdate;
    private final WorkoutTask next;
    private boolean finished;

    public WorkoutTask(final long duration, final TextView textViewToUpdate, final WorkoutTask next) {
        this.duration = duration;
        this.textViewToUpdate = textViewToUpdate;
        this.next = next;

        this.timer = new CountDownTimer(duration, COUNTDOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                textViewToUpdate.setText(JiitTimeUtils.millisToFormattedTime(millisUntilFinished));
            }

            public void onFinish() {
                textViewToUpdate.setText("00:00");
                if (next != null) {
                    next.start();
                } else {
                    Log.i("WorkoutTask", "The End");
                }
            }
        };
    }



    private void start() {
        if (timer != null && !finished) {
            timer.start();
        }
    }
}
