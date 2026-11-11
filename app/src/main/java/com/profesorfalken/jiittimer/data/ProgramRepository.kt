package com.profesorfalken.jiittimer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.profesorfalken.jiittimer.model.UserSettings
import com.profesorfalken.jiittimer.model.WorkoutProgram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

val Context.programsDataStore: DataStore<Preferences> by preferencesDataStore(name = "jiit_programs")

data class ProgramsState(
    val loaded: Boolean = false,
    val programs: List<WorkoutProgram> = emptyList(),
    val selectedProgramId: String? = null,
    val settings: UserSettings = UserSettings(),
) {
    val selectedProgram: WorkoutProgram?
        get() = programs.firstOrNull { it.id == selectedProgramId } ?: programs.firstOrNull()
}

/**
 * Single source of truth for workout programs and user settings.
 *
 * State updates are synchronous in memory; persistence is serialized through a
 * conflated channel so rapid stepper taps cannot reorder writes. Invalid or
 * corrupt JSON falls back to the legacy SharedPreferences data, then defaults.
 */
class ProgramRepository(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    private val dataStore = context.programsDataStore
    private val programsKey = stringPreferencesKey("programs_json")
    private val writeRequests = Channel<Unit>(Channel.CONFLATED)

    private val _state = MutableStateFlow(ProgramsState())
    val state: StateFlow<ProgramsState> = _state.asStateFlow()

    init {
        scope.launch { writeLoop() }
        scope.launch { load() }
    }

    private suspend fun load() {
        val decoded = runCatching {
            val preferences = dataStore.data.first()
            ProgramsCodec.decode(preferences[programsKey])
        }.getOrNull()

        val legacy = if (decoded == null) {
            val raw = runCatching {
                context.getSharedPreferences(LegacyWorkoutParser.PREFERENCES_NAME, Context.MODE_PRIVATE)
                    .getString(LegacyWorkoutParser.WORKOUTS_DATA_KEY, null)
            }.getOrNull()
            LegacyWorkoutParser.parse(raw)
        } else {
            emptyList()
        }

        val source = when {
            decoded != null -> decoded.programs
            legacy.isNotEmpty() -> legacy
            else -> listOf(WorkoutProgram.default())
        }
        val programs = ensureUniqueNames(source)
        val selected = decoded?.selectedProgramId
            ?.takeIf { id -> programs.any { it.id == id } }
            ?: programs.first().id

        _state.value = ProgramsState(
            loaded = true,
            programs = programs,
            selectedProgramId = selected,
            settings = decoded?.settings ?: UserSettings(),
        )

        if (decoded == null) requestPersist()
    }

    private suspend fun writeLoop() {
        for (unused in writeRequests) {
            val snapshot = _state.value
            if (!snapshot.loaded) continue
            runCatching {
                dataStore.edit { preferences ->
                    preferences[programsKey] = ProgramsCodec.encode(
                        programs = snapshot.programs,
                        selectedProgramId = snapshot.selectedProgramId,
                        settings = snapshot.settings,
                    )
                }
            }
        }
    }

    fun selectProgram(id: String) {
        if (!_state.value.loaded) return
        if (_state.value.programs.none { it.id == id }) return
        _state.update { it.copy(selectedProgramId = id) }
        requestPersist()
    }

    fun updateProgram(program: WorkoutProgram) {
        if (!_state.value.loaded) return
        if (_state.value.programs.none { it.id == program.id }) return
        val safe = program.sanitized()
        _state.update { state ->
            state.copy(programs = state.programs.map { if (it.id == safe.id) safe else it })
        }
        requestPersist()
    }

    fun addProgram(name: String, template: WorkoutProgram? = null): WorkoutProgram {
        val state = _state.value
        val base = template ?: state.selectedProgram ?: WorkoutProgram.default()
        val program = base.copy(
            id = UUID.randomUUID().toString(),
            name = uniqueName(name, state.programs),
        ).sanitized()
        _state.update {
            it.copy(
                programs = it.programs + program,
                selectedProgramId = program.id,
            )
        }
        requestPersist()
        return program
    }

    fun renameProgram(id: String, name: String): Boolean {
        val trimmed = name.trim().take(WorkoutProgram.MAX_NAME_LENGTH)
        if (trimmed.isEmpty()) return false
        val state = _state.value
        if (state.programs.none { it.id == id }) return false
        val unique = uniqueName(trimmed, state.programs, excludingId = id)
        _state.update { current ->
            current.copy(programs = current.programs.map { if (it.id == id) it.copy(name = unique) else it })
        }
        requestPersist()
        return true
    }

    fun deleteProgram(id: String): Boolean {
        val state = _state.value
        if (state.programs.size <= 1) return false
        val remaining = state.programs.filterNot { it.id == id }
        if (remaining.size == state.programs.size) return false
        val selected = if (state.selectedProgramId == id) remaining.first().id else state.selectedProgramId
        _state.update { it.copy(programs = remaining, selectedProgramId = selected) }
        requestPersist()
        return true
    }

    fun updateSettings(settings: UserSettings) {
        _state.update { it.copy(settings = settings) }
        requestPersist()
    }

    private fun requestPersist() {
        writeRequests.trySend(Unit)
    }

    private fun uniqueName(
        requested: String,
        programs: List<WorkoutProgram>,
        excludingId: String? = null,
    ): String {
        val base = requested.trim().take(WorkoutProgram.MAX_NAME_LENGTH)
            .ifBlank { WorkoutProgram.DEFAULT_NAME }
        val used = programs
            .filterNot { it.id == excludingId }
            .map { it.name.lowercase() }
            .toSet()
        if (base.lowercase() !in used) return base

        var suffix = 2
        while (true) {
            val suffixText = " ($suffix)"
            val candidate = base.take(WorkoutProgram.MAX_NAME_LENGTH - suffixText.length) + suffixText
            if (candidate.lowercase() !in used) return candidate
            suffix++
        }
    }

    private fun ensureUniqueNames(programs: List<WorkoutProgram>): List<WorkoutProgram> {
        val result = mutableListOf<WorkoutProgram>()
        programs.forEach { program ->
            result += program.copy(name = uniqueName(program.name, result))
        }
        return result
    }
}
