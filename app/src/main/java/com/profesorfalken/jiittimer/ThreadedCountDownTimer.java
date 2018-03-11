package com.profesorfalken.jiittimer;

import android.os.Handler;
import android.util.Log;

public abstract class ThreadedCountDownTimer {
    private long millisInFuture;
    private long countDownInterval;
    private Handler handler;
    private Runnable counter;

    public ThreadedCountDownTimer(long millisInFuture, long countDownInterval) {
        this.millisInFuture = millisInFuture;
        this.countDownInterval = countDownInterval;
        initialize();
    }

    public void start() {
        handler.postDelayed(counter, countDownInterval);
    }

    public abstract void onTick();

    public abstract void onFinish();

    public void initialize()
    {
        handler = new Handler();
        Log.v("status", "starting");
        counter = new Runnable(){
            public void run(){
                long sec = millisInFuture/1000;
                if(millisInFuture <= 0) {
                    Log.v("status", "done");
                    onFinish();
                } else {
                    Log.v("status", Long.toString(sec) + " seconds remain");
                    millisInFuture -= countDownInterval;
                    onTick();
                    handler.postDelayed(this, countDownInterval);
                }
            }
        };
    }
}