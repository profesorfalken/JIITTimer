package com.profesorfalken.jiittimer;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.profesorfalken.jiittimer.util.JiitTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WorkoutDialogFragment.OnFragmentInteractionListener {

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

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.default_hiit_programs, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);

        TextView workTimeTextView = findViewById(R.id.workTimeTextView);
        workTimeTextView.setText(JiitTimeUtils.millisToFormattedTime(this.workTime));

        TextView restTimeTextView = findViewById(R.id.restTimeTextView);
        restTimeTextView.setText(JiitTimeUtils.millisToFormattedTime(this.restTime));

        TextView cyclesTextView = findViewById(R.id.cyclesTextView);
        cyclesTextView.setText(String.format("%d", cycles));
    }

    public void clickGoButton(View view) {

        showWorkoutDialog();
        final TextView workTimeTextView = findViewById(R.id.workTimeTextView);
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

        this.programmedTimers[0].start();
    }

    public void clickOnIncreaseWorkTime(View view) {
        this.workTime += BASE_TIME;
        TextView workTimeTextView = findViewById(R.id.workTimeTextView);
        workTimeTextView.setText(JiitTimeUtils.millisToFormattedTime(this.workTime));
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

    private void showWorkoutDialog() {
        FragmentManager fm = getSupportFragmentManager();
        WorkoutDialogFragment workoutDialog = WorkoutDialogFragment.newInstance("fds", "fdsfds");
        workoutDialog.show(fm, "fragment_workout");
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
