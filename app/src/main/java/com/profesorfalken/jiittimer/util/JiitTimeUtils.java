package com.profesorfalken.jiittimer.util;

import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Created by Javier on 11/02/2018.
 */

public class JiitTimeUtils {
    public static String millisToFormattedTime(long millis) {
        long roundedMillis = (millis + 999) / 1000 * 1000;

        Log.i("JiitTimeUtils", "rounded: " + roundedMillis);

        String formatted =  String.format("%1$tM:%1$tS",
                roundedMillis);

        Log.i("JiitTimeUtils", "formatted: " + formatted);

        return formatted;
    }
}
