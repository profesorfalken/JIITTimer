package com.profesorfalken.jiittimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.profesorfalken.jiittimer.ui.JiitTimerAppScreen
import com.profesorfalken.jiittimer.ui.theme.JiitTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JiitTimerTheme {
                JiitTimerAppScreen()
            }
        }
    }
}
