package com.profesorfalken.jiittimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.profesorfalken.jiittimer.counter.WorkoutTask;
import com.profesorfalken.jiittimer.dialog.AddWorkoutDialog;
import com.profesorfalken.jiittimer.listener.CycleWatcher;
import com.profesorfalken.jiittimer.listener.TimeTextWatcher;
import com.profesorfalken.jiittimer.model.WorkoutData;
import com.profesorfalken.jiittimer.util.JiitTimeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final long BASE_TIME = 1000;
    private final static int REP_DELAY = 50;
    private TextView sessionTimeTextView;
    private TextView cycleCountTextView;
    private WorkoutTask[] programmedTimers;
    private long workTime = 5000;
    private long restTime = 10000;
    private final Handler repeatUpdateHandler = new Handler();
    private boolean autoIncrement = false;
    private boolean autoDecrement = false;
    private EditText workTimeEditText;
    private EditText restTimeEditText;
    private EditText cyclesEditText;
    private EditText coolDownTimeEditText;
    private Button workOutIncreaseTimeButton;
    private Button workOutDecreaseTimeButton;
    private Button restIncreaseTimeButton;
    private Button restDecreaseTimeButton;
    private Button coolDownIncreaseTimeButton;
    private Button coolDownDecreaseTimeButton;
    private Button cyclesIncreaseTimeButton;
    private Button cyclesDecreaseTimeButton;
    private Button goButton;
    private Button stopButton;
    private TextView cycleTimeTextView;
    private boolean timerActive = false;
    private Map<String, WorkoutData> allWorkoutsData = new HashMap<>();
    private JSONArray jsonWorkoutsData = null;

    private Spinner programListSpinner;

    private void decrement(EditText editText) {
        if (JiitTimeUtils.formattedTimeToSeconds(editText.getText().toString()) > 0) {
            editText.setText(JiitTimeUtils.millisToFormattedTime(JiitTimeUtils.formattedTimeToSeconds(editText.getText().toString()) * 1000 - 1000));
        }
    }

    private void increment(EditText editText) {
        editText.setText(JiitTimeUtils.millisToFormattedTime(JiitTimeUtils.formattedTimeToSeconds(editText.getText().toString()) * 1000 + 1000));
    }

    private void initComponentVariables() {
        this.workTimeEditText = findViewById(R.id.workTimeEditText);
        this.restTimeEditText = findViewById(R.id.restTimeEditText);
        this.cyclesEditText = findViewById(R.id.cyclesEditText);
        this.coolDownTimeEditText = findViewById(R.id.coolDownTimeEditText);

        this.workOutIncreaseTimeButton = findViewById(R.id.workPlus);
        this.workOutDecreaseTimeButton = findViewById(R.id.workLess);
        this.restIncreaseTimeButton = findViewById(R.id.restPlus);
        this.restDecreaseTimeButton = findViewById(R.id.restLess);
        this.coolDownIncreaseTimeButton = findViewById(R.id.cooldownPlus);
        this.coolDownDecreaseTimeButton = findViewById(R.id.cooldownLess);
        this.cyclesIncreaseTimeButton = findViewById(R.id.cyclesPlus);
        this.cyclesDecreaseTimeButton = findViewById(R.id.cyclesLess);
        this.goButton = findViewById(R.id.goButton);
        this.stopButton = findViewById(R.id.stopButton);

        this.sessionTimeTextView = findViewById(R.id.sessionTimeTextView);
        this.cycleCountTextView = findViewById(R.id.cycleCountTextView);

        this.cycleTimeTextView = findViewById(R.id.cycleTimeTextView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Prevent standby
        this.findViewById(android.R.id.content).getRootView().setKeepScreenOn(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initComponentVariables();
        //TODO: replace by configuration
        setInitTimeValues();
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
                            JiitTimeUtils.formattedTimeToSeconds(editText.getText().toString()) * 1000));
                }
            }
        };

        View.OnLongClickListener longClickIncreaseValueListener =
                new View.OnLongClickListener() {

                    public boolean onLongClick(View view) {
                        autoIncrement = true;
                        RptUpdater updater = new RptUpdater();
                        updater.setEditText((EditText) view.getTag());
                        repeatUpdateHandler.post(updater);
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
                new View.OnLongClickListener() {

                    public boolean onLongClick(View arg0) {
                        autoDecrement = true;
                        RptUpdater updater = new RptUpdater();
                        updater.setEditText((EditText) arg0.getTag());
                        repeatUpdateHandler.post(updater);
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
        this.coolDownIncreaseTimeButton.setOnLongClickListener(longClickIncreaseValueListener);
        this.coolDownIncreaseTimeButton.setOnTouchListener(longTouchIncreaseValueListener);
        this.coolDownDecreaseTimeButton.setOnLongClickListener(longClickDecreaseValueListener);
        this.coolDownDecreaseTimeButton.setOnTouchListener(longTouchDecreaseValueListener);

        //Link each button with EditText views
        this.workOutIncreaseTimeButton.setTag(workTimeEditText);
        this.workOutDecreaseTimeButton.setTag(workTimeEditText);
        this.restIncreaseTimeButton.setTag(restTimeEditText);
        this.restDecreaseTimeButton.setTag(restTimeEditText);
        this.coolDownIncreaseTimeButton.setTag(coolDownTimeEditText);
        this.coolDownDecreaseTimeButton.setTag(coolDownTimeEditText);
        this.cyclesIncreaseTimeButton.setTag(cyclesEditText);
        this.cyclesDecreaseTimeButton.setTag(cyclesEditText);

    }

    private JSONArray getJsonWorkoutsData() {
        return this.jsonWorkoutsData;
    }

    private void setInitTimeValues() {
        SharedPreferences initValues = getApplicationContext().getSharedPreferences("com.profesorfalken.jiittimer.JiitWorkoutData", Context.MODE_PRIVATE);
        String workoutsData = initValues.getString("WorkoutsData", "[{\"Title\": \"Default\", \"Data\": \"30|10|60|3\"}]");

        programListSpinner = findViewById(R.id.programListSpinner);
        ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<CharSequence>());

        programListSpinner.setAdapter(spinnerAdapter);

        programListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                SharedPreferences preferences = getApplication().getSharedPreferences("com.profesorfalken.jiittimer.JiitWorkoutData", Context.MODE_PRIVATE);
                String selectedProgram = programListSpinner.getSelectedItem().toString();

                if (getJsonWorkoutsData() != null) {
                    for (int i = 0; i < getJsonWorkoutsData().length(); i++) {
                        try {
                            JSONObject workout = getJsonWorkoutsData().getJSONObject(i);
                            if (selectedProgram.equals(workout.get("Title"))) {
                                StringTokenizer st = new StringTokenizer(((String) getJsonWorkoutsData().getJSONObject(i).get("Data")), "|");
                                WorkoutData workoutData = new WorkoutData(Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken()));
                                setWorkoutDataValues(workoutData);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        try {
            this.jsonWorkoutsData = new JSONArray(workoutsData);

            for (int i = 0; i<this.jsonWorkoutsData.length(); i++) {
                spinnerAdapter.add((String)this.jsonWorkoutsData.getJSONObject(i).get("Title"));

                StringTokenizer st = new StringTokenizer(((String) this.jsonWorkoutsData.getJSONObject(i).get("Data")), "|");
                WorkoutData workoutData = new WorkoutData(Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken()));

                allWorkoutsData.put((String)this.jsonWorkoutsData.getJSONObject(i).get("Title"), workoutData);
            }
            spinnerAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WorkoutData defaultWorkoutData = this.allWorkoutsData.get("Default");
        setWorkoutDataValues(defaultWorkoutData);
    }

    public void refreshTotals() {
        int cycles = cyclesEditText.getText().toString().length() > 0 ? Integer.valueOf(this.cyclesEditText.getText().toString()) : 0;

        int totalTimeInSeconds = (JiitTimeUtils.formattedTimeToSeconds(this.workTimeEditText.getText().toString()) +
                JiitTimeUtils.formattedTimeToSeconds(this.restTimeEditText.getText().toString()))
                * cycles + JiitTimeUtils.formattedTimeToSeconds(this.coolDownTimeEditText.getText().toString());

        this.sessionTimeTextView.setText(JiitTimeUtils.millisToFormattedTime(totalTimeInSeconds * 1000));
        this.cycleCountTextView.setText(String.format("%s/%s", "0", this.cyclesEditText.getText().toString()));
    }

    public void clickGoButton(View view) {
        saveWorkout();

        int cyclesToProgram = Integer.valueOf(this.cyclesEditText.getText().toString()).intValue();

        if (cyclesToProgram > 0) {
            toggleTimerMode();

            int workTime = JiitTimeUtils.formattedTimeToSeconds(this.workTimeEditText.getText().toString());
            int restTime = JiitTimeUtils.formattedTimeToSeconds(this.restTimeEditText.getText().toString());
            int cooldownTime = JiitTimeUtils.formattedTimeToSeconds(this.coolDownTimeEditText.getText().toString());

            if (restTime > 0) {
                cyclesToProgram *= 2;
            }
            if (cooldownTime > 0) {
                cyclesToProgram++;
            }

            this.programmedTimers = new WorkoutTask[cyclesToProgram];

            //Program timers
            int i = cyclesToProgram - 1;
            WorkoutTask next = null;

            if (cooldownTime > 0) {
                next = addProgrammedTimer(cooldownTime, i--, next);
            }

            while (i >= 0) {
                if (restTime > 0) {
                    next = addProgrammedTimer(restTime, i--, next);
                }
                next = addProgrammedTimer(workTime, i--, next);
                next.increaseCycle(this.cycleCountTextView);
            }

            this.goButton.setVisibility(View.GONE);
            this.stopButton.setVisibility(View.VISIBLE);

            this.programmedTimers[0].start();
        }
    }

    private void saveWorkout() {
        SharedPreferences preferences = this.getSharedPreferences("com.profesorfalken.jiittimer.JiitWorkoutData", Context.MODE_PRIVATE);
        String selectedProgram = programListSpinner.getSelectedItem().toString();

        boolean found = false;
        for (int i = 0; i<this.jsonWorkoutsData.length(); i++) {
            try {
                JSONObject workout = this.jsonWorkoutsData.getJSONObject(i);
                if (selectedProgram.equals(workout.get("Title"))) {
                    found = true;
                    workout.put("Data", getCurrentWorkoutData());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (!found) {
            JSONObject newWorkout = new JSONObject();
            try {
                newWorkout.put("Title", selectedProgram);
                newWorkout.put("Data", getCurrentWorkoutData());
                this.jsonWorkoutsData.put(newWorkout);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        preferences.edit().putString("WorkoutsData", this.jsonWorkoutsData.toString()).commit();
    }

    private String getCurrentWorkoutData() {
        StringBuilder currentWorkoutData = new StringBuilder();
        currentWorkoutData.append(JiitTimeUtils.formattedTimeToSeconds(this.workTimeEditText.getText().toString())).append("|");
        currentWorkoutData.append(JiitTimeUtils.formattedTimeToSeconds(this.restTimeEditText.getText().toString())).append("|");
        currentWorkoutData.append(JiitTimeUtils.formattedTimeToSeconds(this.coolDownTimeEditText.getText().toString())).append("|");
        currentWorkoutData.append(this.cyclesEditText.getText());

        return currentWorkoutData.toString();
    }

    public void clickStopButton(View view) {
        toggleTimerMode();

        resetTimers();

        //setInitTimeValues();
        refreshTotals();

        this.stopButton.setVisibility(View.GONE);
        this.goButton.setVisibility(View.VISIBLE);

        this.cycleTimeTextView.setText("READY");
    }

    private void setWorkoutDataValues(WorkoutData workoutData) {
        this.workTimeEditText.setText(JiitTimeUtils.millisToFormattedTime(workoutData.getWorkoutTime() * 1000));
        this.restTimeEditText.setText(JiitTimeUtils.millisToFormattedTime(workoutData.getRestTime() * 1000));
        this.cyclesEditText.setText(String.valueOf(workoutData.getCycles()));
        this.coolDownTimeEditText.setText(JiitTimeUtils.millisToFormattedTime(workoutData.getCooldownTime() * 1000));
    }

    private void resetTimers() {
        for (WorkoutTask timer : this.programmedTimers) {
            timer.stop();
        }
        //Dereference
        this.programmedTimers = null;
    }

    private WorkoutTask addProgrammedTimer(int time, int index, WorkoutTask next) {
        this.programmedTimers[index] = new WorkoutTask(this, time, this.cycleTimeTextView, next);
        return this.programmedTimers[index];
    }

    public void toggleTimerMode() {
        this.timerActive = !this.timerActive;

        boolean enabled = !this.timerActive;
        this.workTimeEditText.setEnabled(enabled);
        this.restTimeEditText.setEnabled(enabled);
        this.cyclesEditText.setEnabled(enabled);
        this.coolDownTimeEditText.setEnabled(enabled);
        this.workOutIncreaseTimeButton.setEnabled(enabled);
        this.workOutDecreaseTimeButton.setEnabled(enabled);
        this.restIncreaseTimeButton.setEnabled(enabled);
        this.restDecreaseTimeButton.setEnabled(enabled);
        this.coolDownIncreaseTimeButton.setEnabled(enabled);
        this.coolDownDecreaseTimeButton.setEnabled(enabled);
        this.cyclesIncreaseTimeButton.setEnabled(enabled);
        this.cyclesDecreaseTimeButton.setEnabled(enabled);

        if (enabled == true) {
            refreshTotals();
        }

        this.stopButton.setVisibility(View.GONE);
        this.goButton.setVisibility(View.VISIBLE);

        this.goButton.setEnabled(enabled);
    }

    public void decreaseTotalTime() {
        int totalTimeInSeconds = JiitTimeUtils.formattedTimeToSeconds(this.sessionTimeTextView.getText().toString());
        totalTimeInSeconds--;
        this.sessionTimeTextView.setText(JiitTimeUtils.millisToFormattedTime(totalTimeInSeconds * 1000));

        int counterTimeInSeconds = Integer.valueOf(this.cycleTimeTextView.getText().toString());
        counterTimeInSeconds--;
        this.cycleTimeTextView.setText(String.valueOf(counterTimeInSeconds));
    }

    public void increaseTime(View view) {
        TextView textView = (TextView) view.getTag();
        textView.setText(JiitTimeUtils.millisToFormattedTime(JiitTimeUtils.formattedTimeToSeconds(textView.getText().toString()) * 1000 + 1000));
    }

    public void decreaseTime(View view) {
        TextView textView = (TextView) view.getTag();
        if (JiitTimeUtils.formattedTimeToSeconds(textView.getText().toString()) > 0) {
            textView.setText(JiitTimeUtils.millisToFormattedTime(JiitTimeUtils.formattedTimeToSeconds(textView.getText().toString()) * 1000 - 1000));
        }
    }

    public void increaseCycles(View view) {
        this.cyclesEditText.setText("" + (Integer.valueOf(this.cyclesEditText.getText().toString()) + 1));
    }

    public void decreaseCycles(View view) {
        if (Integer.valueOf(this.cyclesEditText.getText().toString()) > 0) {
            this.cyclesEditText.setText("" + (Integer.valueOf(this.cyclesEditText.getText().toString()) - 1));
        }
    }

    public void createNewWorkout(MenuItem item) {
        AddWorkoutDialog dialog = new AddWorkoutDialog();
        dialog.show(getFragmentManager(), "addWorkoutDialog");
    }

    public void addNewWorkout(String workoutName) {
        ArrayAdapter<CharSequence> workoutListAdapter = ((ArrayAdapter<CharSequence>)programListSpinner.getAdapter());
        workoutListAdapter.add(workoutName);
        workoutListAdapter.notifyDataSetChanged();
        programListSpinner.setSelection(workoutListAdapter.getCount() - 1, true);
        saveWorkout();
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
        if (id == R.id.addWorkout) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class RptUpdater implements Runnable {

        private EditText editText = null;

        public void setEditText(EditText editText) {
            this.editText = editText;
        }

        public void run() {
            if (autoIncrement) {
                increment(this.editText);
                repeatUpdateHandler.postDelayed(this, REP_DELAY);
            } else if (autoDecrement) {
                decrement(this.editText);
                repeatUpdateHandler.postDelayed(this, REP_DELAY);
            }
        }
    }
}
