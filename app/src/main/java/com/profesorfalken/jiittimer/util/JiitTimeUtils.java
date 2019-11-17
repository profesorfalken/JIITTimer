package com.profesorfalken.jiittimer.util;

import android.text.format.DateUtils;

public class JiitTimeUtils {

    public static String secondsToFormattedTime(long seconds) {
        return DateUtils.formatElapsedTime(seconds);
    }

    public static int formattedTimeToSeconds(String formattedTime) {
        int indexOfSeparator = formattedTime.indexOf(":");
        if (indexOfSeparator == -1 || formattedTime.length() == 1) {
            formattedTime = "00:00";
        } else if (indexOfSeparator == 0) {
            formattedTime = "00" + formattedTime;
        } else if (indexOfSeparator == formattedTime.length() - 1) {
            formattedTime += "00";
        }

        String[] units = formattedTime.split(":");
        int minutes = Integer.parseInt(units[0]);
        int seconds = Integer.parseInt(units[1]);

        return 60 * minutes + seconds;
    }
}
