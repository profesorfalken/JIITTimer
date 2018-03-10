package com.profesorfalken.jiittimer;

import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

class WorkoutTask {
    private static final long COUNTDOWN_INTERVAL = 500;

    private final CountDownTimer timer;
    private final long duration;
    private final TextView textViewToUpdate;
    private final WorkoutTask next;
    private boolean finished;
    private boolean increaseCycle;
    private TextView cycleCountTextView;

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

    public void increaseCycle(TextView cycleCountTextView) {
        this.increaseCycle = true;
        this.cycleCountTextView = cycleCountTextView;
    }

    public void start() {
        if (timer != null && !finished) {
            if (this.increaseCycle) {
                String cycleText = this.cycleCountTextView.getText().toString();
                String[] cycleData = cycleText.split("/");

                this.cycleCountTextView.setText(String.valueOf(Integer.valueOf(cycleData[0]) + 1) + "/" + cycleData[1]);
            }

            timer.start();
        }
    }
}