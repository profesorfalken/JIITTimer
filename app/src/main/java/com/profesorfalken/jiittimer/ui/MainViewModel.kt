package com.profesorfalken.jiittimer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.profesorfalken.jiittimer.JiitTimerApp
import com.profesorfalken.jiittimer.data.ProgramRepository
import com.profesorfalken.jiittimer.model.UserSettings
import com.profesorfalken.jiittimer.model.WorkoutProgram
import com.profesorfalken.jiittimer.service.WorkoutTimerService
import com.profesorfalken.jiittimer.timer.TimerEngine
import com.profesorfalken.jiittimer.timer.TimerState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class ProgramField {
    Prepare,
    Work,
    Rest,
    Cycles,
    Cooldown,
}

enum class UiEvent {
    CannotDeleteLastProgram,
    ServiceStartFailed,
}

data class MainUiState(
    val isLoading: Boolean = true,
    val programs: List<WorkoutProgram> = emptyList(),
    val selectedProgram: WorkoutProgram? = null,
    val settings: UserSettings = UserSettings(),
    val timer: TimerState = TimerState(),
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as JiitTimerApp).container
    private val repository: ProgramRepository = container.repository
    private val engine: TimerEngine = container.timerEngine

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    val uiState: StateFlow<MainUiState> = combine(
        repository.state,
        engine.state,
    ) { programsState, timerState ->
        MainUiState(
            isLoading = !programsState.loaded,
            programs = programsState.programs,
            selectedProgram = programsState.selectedProgram,
            settings = programsState.settings,
            timer = timerState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(),
    )

    fun selectProgram(id: String) = repository.selectProgram(id)

    fun adjust(field: ProgramField, delta: Int) {
        val program = uiState.value.selectedProgram ?: return
        val step = WorkoutProgram.TIME_STEP_SECONDS * delta
        val updated = when (field) {
            ProgramField.Prepare -> program.copy(prepareSeconds = program.prepareSeconds + step)
            ProgramField.Work -> program.copy(workSeconds = program.workSeconds + step)
            ProgramField.Rest -> program.copy(restSeconds = program.restSeconds + step)
            ProgramField.Cycles -> program.copy(cycles = program.cycles + delta)
            ProgramField.Cooldown -> program.copy(cooldownSeconds = program.cooldownSeconds + step)
        }
        repository.updateProgram(updated)
    }

    fun addProgram(name: String) {
        val template = uiState.value.selectedProgram
        repository.addProgram(name = name, template = template)
    }

    fun renameSelectedProgram(name: String) {
        val id = uiState.value.selectedProgram?.id ?: return
        repository.renameProgram(id, name)
    }

    fun deleteSelectedProgram() {
        val id = uiState.value.selectedProgram?.id ?: return
        if (!repository.deleteProgram(id)) {
            _events.tryEmit(UiEvent.CannotDeleteLastProgram)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        repository.updateSettings(uiState.value.settings.copy(soundEnabled = enabled))
    }

    fun setVibrationEnabled(enabled: Boolean) {
        repository.updateSettings(uiState.value.settings.copy(vibrationEnabled = enabled))
    }

    fun startWorkout() {
        val program = uiState.value.selectedProgram ?: return
        if (uiState.value.timer.isActive) return
        repository.updateProgram(program)
        engine.start(program)
        runCatching { WorkoutTimerService.start(getApplication()) }
            .onFailure { _events.tryEmit(UiEvent.ServiceStartFailed) }
    }

    fun repeatWorkout() {
        val id = engine.state.value.programId
        val program = repository.state.value.programs.firstOrNull { it.id == id }
            ?: uiState.value.selectedProgram
            ?: return
        repository.updateProgram(program)
        engine.start(program)
        runCatching { WorkoutTimerService.start(getApplication()) }
            .onFailure { _events.tryEmit(UiEvent.ServiceStartFailed) }
    }

    fun pauseWorkout() = engine.pause()

    fun resumeWorkout() = engine.resume()

    fun skipInterval() = engine.skip()

    fun stopWorkout() = engine.stop()
}
