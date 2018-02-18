package com.profesorfalken.jiittimer;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

    private Handler repeatUpdateHandler = new Handler();

    private boolean autoIncrement = false;
    private boolean autoDecrement = false;

    private static int REP_DELAY = 50;

    class RptUpdater implements Runnable {

        private EditText editText = null;

        public void setEditText (EditText editText) {
            this.editText = editText;
        }

        public void run() {
            if( autoIncrement ){
                increment(this.editText);
                repeatUpdateHandler.postDelayed( this, REP_DELAY );
            } else if( autoDecrement ){
                decrement(this.editText);
                repeatUpdateHandler.postDelayed( this, REP_DELAY );
            }
        }
    }

    public void decrement(EditText editText){
        editText.setText(JiitTimeUtils.millisToFormattedTime(JiitTimeUtils.FormattedTimeToSeconds(editText.getText().toString()) * 1000 - 1000) );
    }
    public void increment(EditText editText){
        editText.setText(JiitTimeUtils.millisToFormattedTime(JiitTimeUtils.FormattedTimeToSeconds(editText.getText().toString()) * 1000 + 1000) );
    }



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
        TextWatcher timerWatchers = new TextWatcher() {
            private int lastPosition;

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


                    /* else if (indexOfSeparator == 0) {
                    s.insert(0, "00");
                } else if (indexOfSeparator == text.length() - 1) {
                    s.append("00");
                }*/

                refreshTotals();
            }
        };

        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    EditText editText = (EditText) v;
                    editText.setText(JiitTimeUtils.millisToFormattedTime(
                            JiitTimeUtils.FormattedTimeToSeconds(editText.getText().toString()) * 1000));
                }
            }
        };

        View.OnLongClickListener longClickListener =
                new View.OnLongClickListener(){

                    public boolean onLongClick(View arg0) {
                        autoIncrement = true;
                        RptUpdater updater = new RptUpdater();
                        updater.setEditText((EditText) arg0.getTag());
                        repeatUpdateHandler.post( updater );
                        return false;
                    }
                };




        View.OnTouchListener longTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                        && autoIncrement) {
                    autoIncrement = false;
                }
                return false;
            }
        };





        EditText workTimeEditText = findViewById(R.id.workTimeEditText);
        EditText restTimeEditText = findViewById(R.id.restTimeEditText);
        //EditText cyclesEditText = findViewById(R.id.cyclesEditText);
        EditText coolDownTimeEditText = findViewById(R.id.coolDownTimeEditText);

        workTimeEditText.addTextChangedListener(timerWatchers);
        restTimeEditText.addTextChangedListener(timerWatchers);
        //cyclesEditText.addTextChangedListener(timerWatchers);
        coolDownTimeEditText.addTextChangedListener(timerWatchers);

        workTimeEditText.setOnFocusChangeListener(onFocusChangeListener);
        restTimeEditText.setOnFocusChangeListener(onFocusChangeListener);
        coolDownTimeEditText.setOnFocusChangeListener(onFocusChangeListener);

        Button workOutIncreaseButton = findViewById(R.id.workPlus);
        workOutIncreaseButton.setTag(workTimeEditText);
        workOutIncreaseButton.setOnLongClickListener(longClickListener);
        workOutIncreaseButton.setOnTouchListener(longTouchListener);
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
        cycleCountTextView.setText(String.format("%s/%s", "0", cyclesEditText.getText().toString()));
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

    public void increaseWorkTime(View view) {
        TextView workTimeTextView = findViewById(R.id.workTimeEditText);
        workTimeTextView.setText(JiitTimeUtils.millisToFormattedTime(JiitTimeUtils.FormattedTimeToSeconds(workTimeTextView.getText().toString()) * 1000 + 1000));
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
