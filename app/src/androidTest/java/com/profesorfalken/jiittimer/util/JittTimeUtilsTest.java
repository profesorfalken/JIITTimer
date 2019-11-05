package com.profesorfalken.jiittimer.util;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.text.format.DateUtils;
import android.util.Log;

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
public class JittTimeUtilsTest {

    @Test
    public void when_call_millisToFormattedTime_gives_goodResult() throws Exception {
        assertEquals("00:03", JiitTimeUtils.millisToFormattedTime(3000));
        assertEquals("00:00", JiitTimeUtils.millisToFormattedTime(1));
        assertEquals("00:00", JiitTimeUtils.millisToFormattedTime(-25));
        assertEquals("11:06:40", JiitTimeUtils.millisToFormattedTime(40_000_000));
        assertEquals("01:00", JiitTimeUtils.millisToFormattedTime(60_000));
        assertEquals("1:00:00", JiitTimeUtils.millisToFormattedTime(3_600_000));
        assertEquals("00:45", JiitTimeUtils.millisToFormattedTime(45_000));
    }
}
