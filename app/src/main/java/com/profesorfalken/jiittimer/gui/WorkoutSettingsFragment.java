package com.profesorfalken.jiittimer.gui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.profesorfalken.jiittimer.databinding.WorkoutSettingsBinding;

public class WorkoutSettingsFragment extends Fragment {
    private WorkoutSettingsBinding binding = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.binding = WorkoutSettingsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }
}
