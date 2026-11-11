package com.profesorfalken.jiittimer.data

import com.profesorfalken.jiittimer.model.UserSettings
import com.profesorfalken.jiittimer.model.WorkoutProgram
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

data class ProgramsSnapshot(
    val programs: List<WorkoutProgram>,
    val selectedProgramId: String,
    val settings: UserSettings,
)

/**
 * Versioned JSON codec for the DataStore payload. Corrupt or partial data is
 * rejected as a whole and the repository falls back to legacy data/defaults,
 * so a bad write can never brick the app at startup again.
 */
object ProgramsCodec {
    private const val CURRENT_VERSION = 1

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    @Serializable
    private data class ProgramsFile(
        val version: Int = CURRENT_VERSION,
        val programs: List<WorkoutProgram> = emptyList(),
        val selectedProgramId: String? = null,
        val settings: UserSettings = UserSettings(),
    )

    fun encode(
        programs: List<WorkoutProgram>,
        selectedProgramId: String?,
        settings: UserSettings,
    ): String = json.encodeToString(
        ProgramsFile(
            version = CURRENT_VERSION,
            programs = programs,
            selectedProgramId = selectedProgramId,
            settings = settings,
        ),
    )

    fun decode(raw: String?): ProgramsSnapshot? {
        if (raw.isNullOrBlank()) return null

        return runCatching {
            val file = json.decodeFromString<ProgramsFile>(raw)
            val programs = file.programs
                .map { program ->
                    val safe = program.sanitized()
                    if (safe.id.isBlank()) safe.copy(id = UUID.randomUUID().toString()) else safe
                }
                .distinctBy { it.id }
            if (programs.isEmpty()) return null

            val selected = file.selectedProgramId
                ?.takeIf { id -> programs.any { it.id == id } }
                ?: programs.first().id

            ProgramsSnapshot(
                programs = programs,
                selectedProgramId = selected,
                settings = file.settings,
            )
        }.getOrNull()
    }
}
