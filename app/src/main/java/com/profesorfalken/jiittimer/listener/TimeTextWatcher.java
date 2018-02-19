package com.profesorfalken.jiittimer.listener;

import android.text.Editable;
import android.text.TextWatcher;

import com.profesorfalken.jiittimer.MainActivity;

/**
 * Created by Javier on 18/02/2018.
 */

public class TimeTextWatcher implements TextWatcher {
    private int lastPosition;
    private MainActivity parent;

    public TimeTextWatcher(MainActivity parent) {
        this.parent = parent;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        this.lastPosition = start;
    }

    @Override
    public void afterTextChanged(Editable s) {
        String text = s.toString();

        int indexOfSeparator = text.indexOf(":");
        if (indexOfSeparator == -1) {
            s.insert(this.lastPosition, ":");
        }

        String[] timeData = text.split(":");

        if (timeData.length == 1 && indexOfSeparator == 0) {
            if (timeData[0].length() > 2) {
                s.delete(s.length() -1, s.length());
            }
        } else if (timeData.length == 2) {
            if (timeData[1].length() > 2) {
                s.delete(s.length() -1, s.length());
            }
        }

        if (indexOfSeparator != 0) {
            if (timeData[0].length() > 2) {
                s.delete(0, 1);
            }
        }

        this.parent.refreshTotals();
    }
}
