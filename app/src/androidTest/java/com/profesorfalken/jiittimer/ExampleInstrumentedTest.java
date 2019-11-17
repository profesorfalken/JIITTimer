package com.profesorfalken.jiittimer;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.text.format.DateUtils;

import com.profesorfalken.jiittimer.util.JiitTimeUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.profesorfalken.jiittimer", appContext.getPackageName());
    }

    @Test
    public void check_if_that_works() throws Exception {
        System.out.println(JiitTimeUtils.secondsToFormattedTime(3000));
        System.out.println(DateUtils.formatElapsedTime(3));
    }
}
