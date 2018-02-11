package com.profesorfalken.jiittimer;

import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.profesorfalken.jiittimer.util.JiitTimeUtils;

/**
 * Created by Javier on 11/02/2018.
 */

public class WorkoutTask {
    private static final long COUNTDOWN_INTERVAL = 500;

    private CountDownTimer timer;
    private long duration;
    private TextView textViewToUpdate;
    private WorkoutTask next;
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



    public void start() {
        if (timer != null && !finished) {
            timer.start();
        }
    }
}
