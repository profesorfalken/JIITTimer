package com.profesorfalken.jiittimer.listener;

import android.text.Editable;
import android.text.TextWatcher;

import com.profesorfalken.jiittimer.MainActivity;

/**
 * Created by Javier on 18/02/2018.
 */

public class CycleWatcher implements TextWatcher {
    private MainActivity parent;

    public CycleWatcher(MainActivity parent) {
        this.parent = parent;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        this.parent.refreshTotals();
    }
}
