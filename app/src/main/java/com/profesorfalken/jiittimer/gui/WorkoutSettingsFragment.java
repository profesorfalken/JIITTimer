package com.profesorfalken.jiittimer.gui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.profesorfalken.jiittimer.databinding.WorkoutSettingsBinding;
import com.profesorfalken.jiittimer.gui.helper.LongClickUpdateManager;
import com.profesorfalken.jiittimer.model.WorkoutViewModel;
import com.profesorfalken.jiittimer.util.JiitTimeUtils;

public class WorkoutSettingsFragment extends Fragment {
    private WorkoutSettingsBinding binding = null;
    private WorkoutViewModel viewModel = null;
    LongClickUpdateManager longClickUpdater = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Create all ViewModel observers
        viewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);

        addObservers(viewModel);

        longClickUpdater = new LongClickUpdateManager(this.getActivity(), viewModel);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.binding = WorkoutSettingsBinding.inflate(inflater, container, false);

        addClickListeners();

        return binding.getRoot();
    }

    private void addClickListeners() {
        this.binding.cyclesPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.increaseCycles();
            }
        });
        this.binding.cyclesPlusButton.setOnLongClickListener(getLongClickListener());
        this.binding.cyclesPlusButton.setOnTouchListener(getOnTouchLister());
        this.binding.cyclesLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.decreaseCycles();
            }
        });
        this.binding.cyclesLessButton.setOnLongClickListener(getLongClickListener());
        this.binding.cyclesLessButton.setOnTouchListener(getOnTouchLister());


        this.binding.workTimePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.increaseWorkoutTime();
            }
        });
        this.binding.workTimePlusButton.setOnLongClickListener(getLongClickListener());
        this.binding.workTimePlusButton.setOnTouchListener(getOnTouchLister());
        this.binding.workTimeLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.decreaseWorkoutTime();
            }
        });
        this.binding.workTimeLessButton.setOnLongClickListener(getLongClickListener());
        this.binding.workTimeLessButton.setOnTouchListener(getOnTouchLister());


        this.binding.restTimePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.increaseRestime();
            }
        });
        this.binding.restTimePlusButton.setOnLongClickListener(getLongClickListener());
        this.binding.restTimePlusButton.setOnTouchListener(getOnTouchLister());
        this.binding.restTimeLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.decreaseRestTime();
            }
        });
        this.binding.restTimeLessButton.setOnLongClickListener(getLongClickListener());
        this.binding.restTimeLessButton.setOnTouchListener(getOnTouchLister());


        this.binding.cooldownPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.increaseCooldownTime();
            }
        });
        this.binding.cooldownPlusButton.setOnLongClickListener(getLongClickListener());
        this.binding.cooldownPlusButton.setOnTouchListener(getOnTouchLister());
        this.binding.cooldownLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.decreaseCooldownTime();
            }
        });
        this.binding.cooldownLessButton.setOnLongClickListener(getLongClickListener());
        this.binding.cooldownLessButton.setOnTouchListener(getOnTouchLister());


    }

    private View.OnTouchListener getOnTouchLister() {
        return new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)) {
                    longClickUpdater.stopLongClickUpdate();
                }
                return false;
            }
        };
    }

    private View.OnLongClickListener getLongClickListener() {
        return
                new View.OnLongClickListener() {
                    public boolean onLongClick(View view) {
                        longClickUpdater.startLongClickUpdate(view.getTag().toString());
                        return true;
                    }

                };

    }

    private void addObservers(WorkoutViewModel viewModel) {
        viewModel.getRestTime().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer restTimeMillis) {
                binding.restTimeEdit.setText(JiitTimeUtils.secondsToFormattedTime(restTimeMillis));
            }
        });

        viewModel.getCycles().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer cycles) {
                binding.cyclesEdit.setText(String.valueOf(cycles));
            }
        });

        viewModel.getCooldownTime().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer cooldownMillis) {
                binding.coolDownTimeEdit.setText(JiitTimeUtils.secondsToFormattedTime(cooldownMillis));
            }
        });

        viewModel.getWorkoutTime().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer workoutMillis) {
                binding.workTimeEdit.setText(JiitTimeUtils.secondsToFormattedTime(workoutMillis));
            }
        });
    }
}
