package com.profesorfalken.jiittimer;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final long COUNTDOWN_INTERVAL = 500;

    private static final long BASE_TIME = 1000;

    private long workTime = 30000;

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
    }

    public void clickGoButton(View view) {
        final TextView workTimeTextView = findViewById(R.id.workTimeTextView);

        new CountDownTimer(this.workTime, COUNTDOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "ontick: " +  millisUntilFinished);
                workTimeTextView.setText(JiitTimeUtils.millisToFormattedTime(millisUntilFinished));
            }

            public void onFinish() {
                workTimeTextView.setText("00:00");
                Toast.makeText(getApplicationContext(), "Finished!!!", Toast.LENGTH_LONG).show();
            }
        }.start();
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
}
