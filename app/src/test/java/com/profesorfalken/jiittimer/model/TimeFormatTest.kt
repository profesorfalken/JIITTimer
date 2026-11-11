package com.profesorfalken.jiittimer.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TimeFormatTest {
    @Test
    fun `formats minutes and seconds`() {
        assertThat(TimeFormat.clock(0)).isEqualTo("00:00")
        assertThat(TimeFormat.clock(59)).isEqualTo("00:59")
        assertThat(TimeFormat.clock(60)).isEqualTo("01:00")
        assertThat(TimeFormat.clock(3599)).isEqualTo("59:59")
    }

    @Test
    fun `formats hours when needed`() {
        assertThat(TimeFormat.clock(3600)).isEqualTo("1:00:00")
        assertThat(TimeFormat.clock(3661)).isEqualTo("1:01:01")
        assertThat(TimeFormat.clock(36000)).isEqualTo("10:00:00")
    }

    @Test
    fun `never displays negative time`() {
        assertThat(TimeFormat.clock(-5)).isEqualTo("00:00")
    }

    @Test
    fun `ceiling seconds never shows zero early`() {
        assertThat(TimeFormat.ceilingSeconds(0)).isEqualTo(0)
        assertThat(TimeFormat.ceilingSeconds(1)).isEqualTo(1)
        assertThat(TimeFormat.ceilingSeconds(999)).isEqualTo(1)
        assertThat(TimeFormat.ceilingSeconds(1_000)).isEqualTo(1)
        assertThat(TimeFormat.ceilingSeconds(1_001)).isEqualTo(2)
        assertThat(TimeFormat.ceilingSeconds(30_000)).isEqualTo(30)
    }
}
