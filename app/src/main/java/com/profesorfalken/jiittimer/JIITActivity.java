package com.profesorfalken.jiittimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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

import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

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

import static android.text.format.DateUtils.formatElapsedTime;

public class JIITActivity extends AppCompatActivity {

    private static final String TAG = JIITActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_jiit);
    }

}