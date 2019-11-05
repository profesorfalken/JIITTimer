package com.profesorfalken.jiittimer;

import android.text.format.DateUtils;

import com.profesorfalken.jiittimer.util.JiitTimeUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void check_if_that_works() throws Exception {
        System.out.println(JiitTimeUtils.millisToFormattedTime(3000));
        System.out.println(DateUtils.formatElapsedTime(3));
    }
}