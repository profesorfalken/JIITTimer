package com.profesorfalken.jiittimer.data

import com.profesorfalken.jiittimer.model.WorkoutProgram
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Defensive migration for the 2018 SharedPreferences format:
 *
 * [{"Title":"Default","Data":"30|10|60|3"}]
 *
 * Invalid rows are skipped instead of crashing. The old parser threw
 * NumberFormatException/NoSuchElementException on malformed data.
 */
object LegacyWorkoutParser {
    const val PREFERENCES_NAME = "com.profesorfalken.jiittimer.JiitWorkoutData"
    const val WORKOUTS_DATA_KEY = "WorkoutsData"

    fun parse(raw: String?): List<WorkoutProgram> {
        if (raw.isNullOrBlank()) return emptyList()

        return runCatching {
            val root = Json.parseToJsonElement(raw)
            val entries = root.jsonArray
            val programs = mutableListOf<WorkoutProgram>()

            entries.forEachIndexed { index, element ->
                runCatching {
                    val row = element.jsonObject
                    val title = row["Title"]?.jsonPrimitive?.contentOrNull
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }
                        ?: "Imported ${index + 1}"
                    val data = row["Data"]?.jsonPrimitive?.contentOrNull ?: return@runCatching
                    val parts = data.split("|").map { it.trim() }
                    if (parts.size < 4) return@runCatching

                    val work = parts[0].toIntOrNull() ?: return@runCatching
                    val rest = parts[1].toIntOrNull() ?: return@runCatching
                    val cooldown = parts[2].toIntOrNull() ?: return@runCatching
                    val cycles = parts[3].toIntOrNull() ?: return@runCatching
                    if (work < 0 || rest < 0 || cooldown < 0 || cycles < 1) return@runCatching

                    programs += WorkoutProgram(
                        name = title,
                        workSeconds = work,
                        restSeconds = rest,
                        cooldownSeconds = cooldown,
                        cycles = cycles,
                    ).sanitized()
                }
            }

            deduplicateNames(programs)
        }.getOrDefault(emptyList())
    }

    private fun deduplicateNames(programs: List<WorkoutProgram>): List<WorkoutProgram> {
        val used = mutableSetOf<String>()
        return programs.map { program ->
            var candidate = program.name
            var suffix = 2
            while (!used.add(candidate.lowercase())) {
                candidate = "${program.name.take(WorkoutProgram.MAX_NAME_LENGTH - 4)} ($suffix)"
                suffix++
            }
            program.copy(name = candidate)
        }
    }
}
