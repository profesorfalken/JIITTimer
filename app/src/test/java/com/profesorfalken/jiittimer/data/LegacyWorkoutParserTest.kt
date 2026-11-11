package com.profesorfalken.jiittimer.data

import com.google.common.truth.Truth.assertThat
import com.profesorfalken.jiittimer.model.WorkoutProgram
import org.junit.Test

class LegacyWorkoutParserTest {
    @Test
    fun `parses the 2018 JSON format`() {
        val raw = """
            [
              {"Title":"Default","Data":"30|10|60|3"},
              {"Title":"Tabata","Data":"20|10|0|8"}
            ]
        """.trimIndent()

        val programs = LegacyWorkoutParser.parse(raw)

        assertThat(programs).hasSize(2)
        assertThat(programs[0].name).isEqualTo("Default")
        assertThat(programs[0].prepareSeconds).isEqualTo(5)
        assertThat(programs[0].workSeconds).isEqualTo(30)
        assertThat(programs[0].restSeconds).isEqualTo(10)
        assertThat(programs[0].cooldownSeconds).isEqualTo(60)
        assertThat(programs[0].cycles).isEqualTo(3)

        assertThat(programs[1].name).isEqualTo("Tabata")
        assertThat(programs[1].workSeconds).isEqualTo(20)
        assertThat(programs[1].restSeconds).isEqualTo(10)
        assertThat(programs[1].cooldownSeconds).isEqualTo(0)
        assertThat(programs[1].cycles).isEqualTo(8)
    }

    @Test
    fun `skips a malformed row instead of crashing`() {
        val raw = """
            [
              {"Title":"Broken","Data":"30|10|60|"},
              {"Title":"Missing","Data":"30|10"},
              {"Title":"Text","Data":"a|b|c|d"},
              {"Title":"Valid","Data":"40|15|30|4"}
            ]
        """.trimIndent()

        val programs = LegacyWorkoutParser.parse(raw)

        assertThat(programs).hasSize(1)
        assertThat(programs[0].name).isEqualTo("Valid")
        assertThat(programs[0].workSeconds).isEqualTo(40)
    }

    @Test
    fun `rejects negative values and zero cycles`() {
        val raw = """
            [
              {"Title":"Negative","Data":"-1|10|60|3"},
              {"Title":"Zero","Data":"30|10|60|0"}
            ]
        """.trimIndent()

        assertThat(LegacyWorkoutParser.parse(raw)).isEmpty()
    }

    @Test
    fun `clamps migrated values to current limits`() {
        val raw = """
            [{"Title":"Extreme","Data":"1|0|9999|999"}]
        """.trimIndent()

        val program = LegacyWorkoutParser.parse(raw).single()

        assertThat(program.workSeconds).isEqualTo(WorkoutProgram.MIN_WORK)
        assertThat(program.cooldownSeconds).isEqualTo(WorkoutProgram.MAX_COOLDOWN)
        assertThat(program.cycles).isEqualTo(WorkoutProgram.MAX_CYCLES)
    }

    @Test
    fun `deduplicates names`() {
        val raw = """
            [
              {"Title":"Default","Data":"30|10|60|3"},
              {"Title":"Default","Data":"20|10|0|8"},
              {"Title":"Default","Data":"40|20|0|4"}
            ]
        """.trimIndent()

        val names = LegacyWorkoutParser.parse(raw).map { it.name }

        assertThat(names).containsExactly("Default", "Default (2)", "Default (3)").inOrder()
    }

    @Test
    fun `falls back to defaults for blank malformed or non-array JSON`() {
        assertThat(LegacyWorkoutParser.parse(null)).isEmpty()
        assertThat(LegacyWorkoutParser.parse("")).isEmpty()
        assertThat(LegacyWorkoutParser.parse("not json")).isEmpty()
        assertThat(LegacyWorkoutParser.parse("{\"Title\":\"Default\"}")).isEmpty()
        assertThat(LegacyWorkoutParser.parse("[null, 3, {}]")).isEmpty()
    }
}
