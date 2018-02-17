package com.profesorfalken.jiittimer;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.profesorfalken.jiittimer.util.JiitTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final long BASE_TIME = 1000;

    private WorkoutTask[] programmedTimers;

    private long workTime = 5000;

    private long restTime = 10000;

    private int cycles = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TODO: replace by configuration
        setInitTimeValues();
        fillDefaultPrograms();
        refreshTotals();

        initEvents();

/*
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.default_hiit_programs, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);

        TextView workTimeTextView = findViewById(R.id.workTimeTextView);
        workTimeTextView.setText(JiitTimeUtils.millisToFormattedTime(this.workTime));

        TextView restTimeTextView = findViewById(R.id.restTimeTextView);
        restTimeTextView.setText(JiitTimeUtils.millisToFormattedTime(this.restTime));

        TextView cyclesTextView = findViewById(R.id.cyclesTextView);
        cyclesTextView.setText(String.format("%d", cycles));*/
    }

    private void initEvents() {
        TextWatcher timerWatchers =  new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshTotals();
            }
        };

        EditText workTimeEditText = findViewById(R.id.workTimeEditText);
        EditText restTimeEditText = findViewById(R.id.restTimeEditText);
        EditText cyclesEditText = findViewById(R.id.cyclesEditText);
        EditText coolDownTimeEditText = findViewById(R.id.coolDownTimeEditText);

        workTimeEditText.addTextChangedListener(timerWatchers);
        restTimeEditText.addTextChangedListener(timerWatchers);
        cyclesEditText.addTextChangedListener(timerWatchers);
        coolDownTimeEditText.addTextChangedListener(timerWatchers);
    }

    private void fillDefaultPrograms() {
        Spinner programListSpinner = (Spinner) findViewById(R.id.programListSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.default_hiit_programs, android.R.layout.simple_spinner_dropdown_item);
        programListSpinner.setAdapter(adapter);
    }

    private void setInitTimeValues() {
        EditText workTimeEditText = findViewById(R.id.workTimeEditText);
        EditText restTimeEditText = findViewById(R.id.restTimeEditText);
        EditText cyclesEditText = findViewById(R.id.cyclesEditText);
        EditText coolDownTimeEditText = findViewById(R.id.coolDownTimeEditText);

        workTimeEditText.setText("00:30");
        restTimeEditText.setText("00:10");
        cyclesEditText.setText("3");
        coolDownTimeEditText.setText("01:00");
    }

    private void refreshTotals() {
        EditText workTimeEditText = findViewById(R.id.workTimeEditText);
        EditText restTimeEditText = findViewById(R.id.restTimeEditText);
        EditText cyclesEditText = findViewById(R.id.cyclesEditText);
        EditText coolDownTimeEditText = findViewById(R.id.coolDownTimeEditText);

        TextView sessionTimeTextView = findViewById(R.id.sessionTimeTextView);
        TextView cycleCountTextView = findViewById(R.id.cycleCountTextView);

        int cycles = cyclesEditText.getText().toString().length() > 0 ? Integer.valueOf(cyclesEditText.getText().toString()) : 0;

        int totalTimeInSeconds = (JiitTimeUtils.FormattedTimeToSeconds(workTimeEditText.getText().toString()) +
                JiitTimeUtils.FormattedTimeToSeconds(restTimeEditText.getText().toString()))
                * cycles + JiitTimeUtils.FormattedTimeToSeconds(coolDownTimeEditText.getText().toString());

        sessionTimeTextView.setText(JiitTimeUtils.millisToFormattedTime(totalTimeInSeconds * 1000));
        cycleCountTextView.setText(String.format("%s/%s", "0" , cyclesEditText.getText().toString()));
    }

    public void clickGoButton(View view) {
      /*  final TextView workTimeTextView = findViewById(R.id.workTimeTextView);
        final TextView restTimeTextView = findViewById(R.id.restTimeTextView);

        int cyclesToProgram = this.cycles;
        if (this.restTime >= 1000) {
            cyclesToProgram *= 2;
        }

        this.programmedTimers = new WorkoutTask[cyclesToProgram];

        //Program timers
        int i = cyclesToProgram -1;
        WorkoutTask next = null;
        while (i >= 0) {
            if (this.restTime >= 1000) {
                this.programmedTimers[i] = new WorkoutTask(this.restTime, restTimeTextView, next);
                next = this.programmedTimers[i--];
            }
            this.programmedTimers[i] = new WorkoutTask(this.workTime, workTimeTextView, next);
            next = this.programmedTimers[i--];
        }

        this.programmedTimers[0].start();*/
    }

    public void clickOnIncreaseWorkTime(View view) {
      /*  this.workTime += BASE_TIME;
        TextView workTimeTextView = findViewById(R.id.workTimeTextView);
        workTimeTextView.setText(JiitTimeUtils.millisToFormattedTime(this.workTime));*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
