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

import com.profesorfalken.jiittimer.listener.CycleWatcher;
import com.profesorfalken.jiittimer.listener.TimeTextWatcher;
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

    private EditText workTimeEditText;
    private EditText restTimeEditText;
    private EditText cyclesEditText;
    private EditText coolDownTimeEditText;
    private Button workOutIncreaseTimeButton;
    private Button workOutDecreaseTimeButton;
    private Button restIncreaseTimeButton;
    private Button restDecreaseTimeButton;
    private Button cooldownIncreaseTimeButton;
    private Button cooldownDecreaseTimeButton;

    TextView sessionTimeTextView = findViewById(R.id.sessionTimeTextView);
    TextView cycleCountTextView = findViewById(R.id.cycleCountTextView);

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

    private void initComponentVariables () {
        this.workTimeEditText = findViewById(R.id.workTimeEditText);
        this.restTimeEditText = findViewById(R.id.restTimeEditText);
        this.cyclesEditText = findViewById(R.id.cyclesEditText);
        this.coolDownTimeEditText = findViewById(R.id.coolDownTimeEditText);

        this.workOutIncreaseTimeButton = findViewById(R.id.workPlus);
        this.workOutDecreaseTimeButton = findViewById(R.id.workLess);
        this.restIncreaseTimeButton = findViewById(R.id.restPlus);
        this.restDecreaseTimeButton = findViewById(R.id.restLess);
        this.cooldownIncreaseTimeButton = findViewById(R.id.cooldownPlus);
        this.cooldownDecreaseTimeButton = findViewById(R.id.cooldownPlus);

        this.sessionTimeTextView = findViewById(R.id.sessionTimeTextView);
        this.cycleCountTextView = findViewById(R.id.cycleCountTextView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initComponentVariables();
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

        View.OnLongClickListener longClickIncreaseValueListener =
                new View.OnLongClickListener(){

                    public boolean onLongClick(View arg0) {
                        autoIncrement = true;
                        RptUpdater updater = new RptUpdater();
                        updater.setEditText((EditText) arg0.getTag());
                        repeatUpdateHandler.post( updater );
                        return false;
                    }
                };




        View.OnTouchListener longTouchIncreaseValueListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                        && autoIncrement) {
                    autoIncrement = false;
                }
                return false;
            }
        };

        View.OnLongClickListener longClickDecreaseValueListener =
                new View.OnLongClickListener(){

                    public boolean onLongClick(View arg0) {
                        autoDecrement = true;
                        RptUpdater updater = new RptUpdater();
                        updater.setEditText((EditText) arg0.getTag());
                        repeatUpdateHandler.post( updater );
                        return false;
                    }
                };




        View.OnTouchListener longTouchDecreaseValueListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                        && autoDecrement) {
                    autoDecrement = false;
                }
                return false;
            }
        };

        TimeTextWatcher timeTextWatcher = new TimeTextWatcher(this);

        //Set TextWatcher
        this.workTimeEditText.addTextChangedListener(timeTextWatcher);
        this.restTimeEditText.addTextChangedListener(timeTextWatcher);
        this.coolDownTimeEditText.addTextChangedListener(timeTextWatcher);

        //Set cycleWatcher
        this.cyclesEditText.addTextChangedListener(new CycleWatcher(this));

        //Set onFocusChange Listener
        this.workTimeEditText.setOnFocusChangeListener(onFocusChangeListener);
        this.restTimeEditText.setOnFocusChangeListener(onFocusChangeListener);
        this.coolDownTimeEditText.setOnFocusChangeListener(onFocusChangeListener);

        //Set listener for increase and decrease buttons

        this.workOutIncreaseTimeButton.setOnLongClickListener(longClickIncreaseValueListener);
        this.workOutIncreaseTimeButton.setOnTouchListener(longTouchIncreaseValueListener);
        this.workOutDecreaseTimeButton.setOnLongClickListener(longClickDecreaseValueListener);
        this.workOutDecreaseTimeButton.setOnTouchListener(longTouchDecreaseValueListener);
        this.restIncreaseTimeButton.setOnLongClickListener(longClickIncreaseValueListener);
        this.restIncreaseTimeButton.setOnTouchListener(longTouchIncreaseValueListener);
        this.restDecreaseTimeButton.setOnLongClickListener(longClickDecreaseValueListener);
        this.restDecreaseTimeButton.setOnTouchListener(longTouchDecreaseValueListener);
        this.cooldownIncreaseTimeButton.setOnLongClickListener(longClickIncreaseValueListener);
        this.cooldownIncreaseTimeButton.setOnTouchListener(longTouchIncreaseValueListener);
        this.cooldownDecreaseTimeButton.setOnLongClickListener(longClickDecreaseValueListener);
        this.cooldownDecreaseTimeButton.setOnTouchListener(longTouchDecreaseValueListener);

        //Link each button with EditText views
        this.workOutIncreaseTimeButton.setTag(workTimeEditText);
        this.workOutDecreaseTimeButton.setTag(workTimeEditText);
        this.restIncreaseTimeButton.setTag(restTimeEditText);
        this.restDecreaseTimeButton.setTag(restTimeEditText);
        this.cooldownIncreaseTimeButton.setTag(coolDownTimeEditText);
        this.cooldownDecreaseTimeButton.setTag(coolDownTimeEditText);

    }

    private void fillDefaultPrograms() {
        Spinner programListSpinner = (Spinner) findViewById(R.id.programListSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.default_hiit_programs, android.R.layout.simple_spinner_dropdown_item);
        programListSpinner.setAdapter(adapter);
    }

    private void setInitTimeValues() {
        this.workTimeEditText.setText("00:30");
        this.restTimeEditText.setText("00:10");
        this.cyclesEditText.setText("3");
        this.coolDownTimeEditText.setText("01:00");
    }

    public void refreshTotals() {
        int cycles = cyclesEditText.getText().toString().length() > 0 ? Integer.valueOf(this.cyclesEditText.getText().toString()) : 0;

        int totalTimeInSeconds = (JiitTimeUtils.FormattedTimeToSeconds(this.workTimeEditText.getText().toString()) +
                JiitTimeUtils.FormattedTimeToSeconds(this.restTimeEditText.getText().toString()))
                * cycles + JiitTimeUtils.FormattedTimeToSeconds(this.coolDownTimeEditText.getText().toString());

        this.sessionTimeTextView.setText(JiitTimeUtils.millisToFormattedTime(totalTimeInSeconds * 1000));
        this.cycleCountTextView.setText(String.format("%s/%s", "0", this.cyclesEditText.getText().toString()));
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

    public void increaseTime(View view) {
        TextView textView = (TextView) view.getTag();
        textView.setText(JiitTimeUtils.millisToFormattedTime(JiitTimeUtils.FormattedTimeToSeconds(textView.getText().toString()) * 1000 + 1000));
    }

    public void decreaseTime(View view) {
        TextView textView = (TextView) view.getTag();
        textView.setText(JiitTimeUtils.millisToFormattedTime(JiitTimeUtils.FormattedTimeToSeconds(textView.getText().toString()) * 1000 - 1000));
    }

    public void increaseCycles(View view) {
        this.cyclesEditText.setText("" + (Integer.valueOf(this.cyclesEditText.getText().toString()) + 1));
    }

    public void decreaseCycles(View view) {
        this.cyclesEditText.setText("" + (Integer.valueOf(this.cyclesEditText.getText().toString()) - 1));
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
