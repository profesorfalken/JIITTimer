package com.profesorfalken.jiittimer.data

import com.google.common.truth.Truth.assertThat
import com.profesorfalken.jiittimer.model.UserSettings
import com.profesorfalken.jiittimer.model.WorkoutProgram
import org.junit.Test

class ProgramsCodecTest {
    private val program = WorkoutProgram(
        id = "program-1",
        name = "Morning HIIT",
        prepareSeconds = 5,
        workSeconds = 40,
        restSeconds = 20,
        cycles = 6,
        cooldownSeconds = 60,
    )

    @Test
    fun `round trips programs selection and settings`() {
        val encoded = ProgramsCodec.encode(
            programs = listOf(program),
            selectedProgramId = program.id,
            settings = UserSettings(soundEnabled = false, vibrationEnabled = true),
        )

        val decoded = ProgramsCodec.decode(encoded)

        assertThat(decoded).isNotNull()
        assertThat(decoded!!.programs).hasSize(1)
        assertThat(decoded.programs.single()).isEqualTo(program)
        assertThat(decoded.selectedProgramId).isEqualTo(program.id)
        assertThat(decoded.settings.soundEnabled).isFalse()
        assertThat(decoded.settings.vibrationEnabled).isTrue()
    }

    @Test
    fun `repairs an invalid selected id`() {
        val encoded = ProgramsCodec.encode(
            programs = listOf(program),
            selectedProgramId = "missing",
            settings = UserSettings(),
        )

        assertThat(ProgramsCodec.decode(encoded)?.selectedProgramId).isEqualTo(program.id)
    }

    @Test
    fun `returns null for blank corrupt or empty payloads`() {
        assertThat(ProgramsCodec.decode(null)).isNull()
        assertThat(ProgramsCodec.decode("")).isNull()
        assertThat(ProgramsCodec.decode("not json")).isNull()
        assertThat(ProgramsCodec.decode("{}")).isNull()
        assertThat(
            ProgramsCodec.decode(ProgramsCodec.encode(emptyList(), null, UserSettings())),
        ).isNull()
    }

    @Test
    fun `deduplicates ids and ignores unknown fields`() {
        val raw = """
            {
              "version": 99,
              "futureField": true,
              "programs": [
                {"id":"a","name":"One","prepareSeconds":0,"workSeconds":10,"restSeconds":0,"cycles":1,"cooldownSeconds":0},
                {"id":"a","name":"Duplicate","prepareSeconds":0,"workSeconds":20,"restSeconds":0,"cycles":1,"cooldownSeconds":0}
              ],
              "selectedProgramId": "a",
              "settings": {"soundEnabled": false, "vibrationEnabled": false}
            }
        """.trimIndent()

        val decoded = ProgramsCodec.decode(raw)!!

        assertThat(decoded.programs).hasSize(1)
        assertThat(decoded.programs.single().name).isEqualTo("One")
        assertThat(decoded.settings.soundEnabled).isFalse()
        assertThat(decoded.settings.vibrationEnabled).isFalse()
    }

    @Test
    fun `generates an id when the stored id is blank`() {
        val raw = """
            {
              "programs": [
                {"id":"","name":"No id","prepareSeconds":0,"workSeconds":10,"restSeconds":0,"cycles":1,"cooldownSeconds":0}
              ]
            }
        """.trimIndent()

        val decoded = ProgramsCodec.decode(raw)!!

        assertThat(decoded.programs.single().id).isNotEmpty()
        assertThat(decoded.selectedProgramId).isEqualTo(decoded.programs.single().id)
    }
}
