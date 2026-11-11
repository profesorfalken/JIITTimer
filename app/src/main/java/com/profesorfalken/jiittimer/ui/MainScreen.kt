package com.profesorfalken.jiittimer.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.profesorfalken.jiittimer.R
import com.profesorfalken.jiittimer.model.TimeFormat
import com.profesorfalken.jiittimer.model.UserSettings
import com.profesorfalken.jiittimer.model.WorkoutProgram
import com.profesorfalken.jiittimer.timer.IntervalType
import com.profesorfalken.jiittimer.timer.TimerState
import com.profesorfalken.jiittimer.timer.TimerStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JiitTimerAppScreen(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    val startWorkout = rememberStartWorkoutAction { viewModel.startWorkout() }

    KeepScreenOn(state.timer.status == TimerStatus.Running)

    LaunchedEffect(viewModel, context) {
        viewModel.events.collect { event ->
            val message = when (event) {
                UiEvent.CannotDeleteLastProgram -> context.getString(R.string.cannot_delete_last_program)
                UiEvent.ServiceStartFailed -> context.getString(R.string.service_start_failed)
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                actions = {
                    if (state.timer.status == TimerStatus.Idle) {
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.settings),
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> LoadingScreen()
                state.timer.status == TimerStatus.Idle -> SetupScreen(
                    state = state,
                    onSelectProgram = viewModel::selectProgram,
                    onAdjust = viewModel::adjust,
                    onAddProgram = { showAddDialog = true },
                    onRenameProgram = { showRenameDialog = true },
                    onDeleteProgram = { showDeleteDialog = true },
                    onStart = startWorkout,
                )
                else -> TimerScreen(
                    state = state.timer,
                    program = state.selectedProgram,
                    onPause = viewModel::pauseWorkout,
                    onResume = viewModel::resumeWorkout,
                    onSkip = viewModel::skipInterval,
                    onStop = viewModel::stopWorkout,
                    onRepeat = viewModel::repeatWorkout,
                )
            }
        }
    }

    val selected = state.selectedProgram
    if (showAddDialog) {
        ProgramNameDialog(
            title = stringResource(R.string.add_program),
            initialName = "",
            confirmLabel = stringResource(R.string.create),
            existingNames = state.programs.map { it.name },
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addProgram(name)
                showAddDialog = false
            },
        )
    }

    if (showRenameDialog && selected != null) {
        ProgramNameDialog(
            title = stringResource(R.string.rename_program),
            initialName = selected.name,
            confirmLabel = stringResource(R.string.save),
            existingNames = state.programs.filterNot { it.id == selected.id }.map { it.name },
            onDismiss = { showRenameDialog = false },
            onConfirm = { name ->
                viewModel.renameSelectedProgram(name)
                showRenameDialog = false
            },
        )
    }

    if (showDeleteDialog && selected != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_program)) },
            text = { Text(stringResource(R.string.delete_program_confirmation, selected.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSelectedProgram()
                        showDeleteDialog = false
                    },
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            settings = state.settings,
            onSoundChange = viewModel::setSoundEnabled,
            onVibrationChange = viewModel::setVibrationEnabled,
            onDismiss = { showSettingsDialog = false },
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SetupScreen(
    state: MainUiState,
    onSelectProgram: (String) -> Unit,
    onAdjust: (ProgramField, Int) -> Unit,
    onAddProgram: () -> Unit,
    onRenameProgram: () -> Unit,
    onDeleteProgram: () -> Unit,
    onStart: () -> Unit,
) {
    val program = state.selectedProgram ?: return

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= 700.dp

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ProgramSelector(
                    program = program,
                    programs = state.programs,
                    onSelectProgram = onSelectProgram,
                    onAddProgram = onAddProgram,
                    onRenameProgram = onRenameProgram,
                    onDeleteProgram = onDeleteProgram,
                    canDelete = state.programs.size > 1,
                )

            val cards: @Composable (Modifier) -> Unit = { modifier ->
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TimeStepperCard(
                        label = stringResource(R.string.prepare),
                        seconds = program.prepareSeconds,
                        minimum = WorkoutProgram.MIN_PREPARE,
                        maximum = WorkoutProgram.MAX_PREPARE,
                        accent = MaterialTheme.colorScheme.tertiary,
                        onDecrease = { onAdjust(ProgramField.Prepare, -1) },
                        onIncrease = { onAdjust(ProgramField.Prepare, 1) },
                    )
                    TimeStepperCard(
                        label = stringResource(R.string.work),
                        seconds = program.workSeconds,
                        minimum = WorkoutProgram.MIN_WORK,
                        maximum = WorkoutProgram.MAX_WORK,
                        accent = MaterialTheme.colorScheme.primary,
                        onDecrease = { onAdjust(ProgramField.Work, -1) },
                        onIncrease = { onAdjust(ProgramField.Work, 1) },
                    )
                    TimeStepperCard(
                        label = stringResource(R.string.rest),
                        seconds = program.restSeconds,
                        minimum = WorkoutProgram.MIN_REST,
                        maximum = WorkoutProgram.MAX_REST,
                        accent = MaterialTheme.colorScheme.secondary,
                        onDecrease = { onAdjust(ProgramField.Rest, -1) },
                        onIncrease = { onAdjust(ProgramField.Rest, 1) },
                    )
                }
            }

            val rightCards: @Composable (Modifier) -> Unit = { modifier ->
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CycleStepperCard(
                        cycles = program.cycles,
                        onDecrease = { onAdjust(ProgramField.Cycles, -1) },
                        onIncrease = { onAdjust(ProgramField.Cycles, 1) },
                    )
                    TimeStepperCard(
                        label = stringResource(R.string.cool_down),
                        seconds = program.cooldownSeconds,
                        minimum = WorkoutProgram.MIN_COOLDOWN,
                        maximum = WorkoutProgram.MAX_COOLDOWN,
                        accent = MaterialTheme.colorScheme.secondary,
                        onDecrease = { onAdjust(ProgramField.Cooldown, -1) },
                        onIncrease = { onAdjust(ProgramField.Cooldown, 1) },
                    )
                    SummaryCard(program = program)
                }
            }

                if (wide) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        cards(Modifier.weight(1f))
                        rightCards(Modifier.weight(1f))
                    }
                } else {
                    cards(Modifier.fillMaxWidth())
                    rightCards(Modifier.fillMaxWidth())
                }

                Text(
                    text = stringResource(R.string.local_data_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                )
            }

            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(60.dp)
                        .testTag("start_workout"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.start_workout),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgramSelector(
    program: WorkoutProgram,
    programs: List<WorkoutProgram>,
    onSelectProgram: (String) -> Unit,
    onAddProgram: () -> Unit,
    onRenameProgram: () -> Unit,
    onDeleteProgram: () -> Unit,
    canDelete: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 10.dp, end = 8.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.program),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(
                            text = program.name,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = stringResource(R.string.choose_program),
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        programs.forEach { candidate ->
                            DropdownMenuItem(
                                text = { Text(candidate.name) },
                                onClick = {
                                    onSelectProgram(candidate.id)
                                    expanded = false
                                },
                                leadingIcon = {
                                    if (candidate.id == program.id) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onAddProgram,
                modifier = Modifier.testTag("add_program"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_program),
                )
            }
            IconButton(onClick = onRenameProgram) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.rename_program),
                )
            }
            IconButton(onClick = onDeleteProgram, enabled = canDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.delete_program),
                )
            }
        }
    }
}

@Composable
private fun TimeStepperCard(
    label: String,
    seconds: Int,
    minimum: Int,
    maximum: Int,
    accent: Color,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.14f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = accent,
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = TimeFormat.clock(seconds.toLong()),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StepperButton(
                        isIncrease = false,
                        enabled = seconds > minimum,
                        label = label,
                        accent = accent,
                        onClick = onDecrease,
                    )
                    StepperButton(
                        isIncrease = true,
                        enabled = seconds < maximum,
                        label = label,
                        accent = accent,
                        onClick = onIncrease,
                    )
                }
            }
        }
    }
}

@Composable
private fun CycleStepperCard(
    cycles: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.cycles).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = cycles.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StepperButton(
                        isIncrease = false,
                        enabled = cycles > WorkoutProgram.MIN_CYCLES,
                        label = stringResource(R.string.cycles),
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = onDecrease,
                    )
                    StepperButton(
                        isIncrease = true,
                        enabled = cycles < WorkoutProgram.MAX_CYCLES,
                        label = stringResource(R.string.cycles),
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = onIncrease,
                    )
                }
            }
        }
    }
}

@Composable
private fun StepperButton(
    isIncrease: Boolean,
    enabled: Boolean,
    label: String,
    accent: Color,
    onClick: () -> Unit,
) {
    val description = if (isIncrease) {
        stringResource(R.string.increase_value, label)
    } else {
        stringResource(R.string.decrease_value, label)
    }
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = accent.copy(alpha = 0.18f),
            contentColor = accent,
        ),
    ) {
        Icon(
            imageVector = if (isIncrease) Icons.Filled.Add else Icons.Filled.Remove,
            contentDescription = description,
        )
    }
}

@Composable
private fun SummaryCard(program: WorkoutProgram) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.total_session).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = TimeFormat.clock(program.totalSeconds.toLong()),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.session_breakdown,
                    program.cycles,
                    program.workSeconds,
                    program.restSeconds,
                    program.prepareSeconds,
                    program.cooldownSeconds,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TimerScreen(
    state: TimerState,
    program: WorkoutProgram?,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit,
    onRepeat: () -> Unit,
) {
    var showStopConfirmation by rememberSaveable { mutableStateOf(false) }

    if (state.status == TimerStatus.Finished) {
        FinishedScreen(
            state = state,
            programName = program?.name ?: state.programName,
            onDone = onStop,
            onRepeat = onRepeat,
        )
        return
    }

    val accent = intervalColor(state.interval?.type)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag("timer_screen"),
    ) {
        val landscape = maxWidth > maxHeight && maxWidth >= 600.dp
        val compact = maxHeight < 520.dp
        val timeSize = when {
            landscape -> 64.sp
            compact -> 60.sp
            else -> 88.sp
        }

        if (landscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                TimerDisplay(
                    state = state,
                    accent = accent,
                    timeSize = timeSize,
                    modifier = Modifier.weight(1.1f),
                )
                TimerControls(
                    state = state,
                    accent = accent,
                    onPause = onPause,
                    onResume = onResume,
                    onSkip = onSkip,
                    onStop = { showStopConfirmation = true },
                    modifier = Modifier.weight(0.9f),
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                TimerDisplay(
                    state = state,
                    accent = accent,
                    timeSize = timeSize,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(if (compact) 16.dp else 32.dp))
                TimerControls(
                    state = state,
                    accent = accent,
                    onPause = onPause,
                    onResume = onResume,
                    onSkip = onSkip,
                    onStop = { showStopConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showStopConfirmation) {
        AlertDialog(
            onDismissRequest = { showStopConfirmation = false },
            title = { Text(stringResource(R.string.stop_workout_title)) },
            text = { Text(stringResource(R.string.stop_workout_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStopConfirmation = false
                        onStop()
                    },
                    modifier = Modifier.testTag("confirm_stop"),
                ) {
                    Text(stringResource(R.string.stop))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirmation = false }) {
                    Text(stringResource(R.string.keep_going))
                }
            },
        )
    }
}

@Composable
private fun TimerDisplay(
    state: TimerState,
    accent: Color,
    timeSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = state.programName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        Surface(
            shape = CircleShape,
            color = accent.copy(alpha = 0.16f),
        ) {
            Text(
                text = intervalLabel(state.interval?.type).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        val remainingDescription = pluralStringResource(
            R.plurals.timer_remaining_seconds,
            state.remainingSeconds.toInt(),
            state.remainingSeconds,
        )
        Text(
            text = TimeFormat.clock(state.remainingSeconds),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = timeSize,
                lineHeight = timeSize * 1.05f,
            ),
            fontWeight = FontWeight.Bold,
            color = accent,
            maxLines = 1,
            modifier = Modifier.semantics {
                contentDescription = remainingDescription
            },
        )
        if (state.currentCycle != null) {
            Text(
                text = stringResource(
                    R.string.round_progress,
                    state.currentCycle ?: 0,
                    state.totalCycles,
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { state.intervalProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape),
            color = accent,
            trackColor = accent.copy(alpha = 0.16f),
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(
                    R.string.total_remaining,
                    TimeFormat.clock(state.totalRemainingSeconds),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = when {
                    state.status == TimerStatus.Paused -> stringResource(R.string.paused)
                    state.currentCycle != null -> stringResource(R.string.work_in_progress)
                    else -> stringResource(R.string.timer_running)
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = accent,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = nextIntervalText(state),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TimerControls(
    state: TimerState,
    accent: Color,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val paused = state.status == TimerStatus.Paused

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = { if (paused) onResume() else onPause() },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("pause_resume"),
        ) {
            Icon(
                imageVector = if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(if (paused) R.string.resume else R.string.pause),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("skip"),
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = null,
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.skip))
            }
            OutlinedButton(
                onClick = onStop,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("stop_workout"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = null,
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.stop))
            }
        }
        if (paused) {
            Text(
                text = stringResource(R.string.paused_hint),
                style = MaterialTheme.typography.bodySmall,
                color = accent,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FinishedScreen(
    state: TimerState,
    programName: String,
    onDone: () -> Unit,
    onRepeat: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("finished_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(80.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.workout_complete),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = programName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = TimeFormat.clock(state.totalDurationMs / 1000L),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.total_duration),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
        ) {
            Text(
                text = stringResource(R.string.done),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onRepeat,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Icon(imageVector = Icons.Filled.Repeat, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.repeat_workout))
        }
    }
}

@Composable
private fun SettingsDialog(
    settings: UserSettings,
    onSoundChange: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.settings_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                SettingRow(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = stringResource(R.string.sound),
                    checked = settings.soundEnabled,
                    onCheckedChange = onSoundChange,
                )
                HorizontalDivider()
                SettingRow(
                    icon = Icons.Filled.Vibration,
                    title = stringResource(R.string.vibration),
                    checked = settings.vibrationEnabled,
                    onCheckedChange = onVibrationChange,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ProgramNameDialog(
    title: String,
    initialName: String,
    confirmLabel: String,
    existingNames: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var error by remember { mutableStateOf<String?>(null) }
    val requiredError = stringResource(R.string.program_name_required)
    val duplicateError = stringResource(R.string.program_name_duplicate)

    fun submit() {
        val trimmed = name.trim()
        error = when {
            trimmed.isEmpty() -> requiredError
            existingNames.any { it.equals(trimmed, ignoreCase = true) } -> duplicateError
            else -> null
        }
        if (error == null) onConfirm(trimmed)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it.take(WorkoutProgram.MAX_NAME_LENGTH)
                    error = null
                },
                label = { Text(stringResource(R.string.program_name_label)) },
                singleLine = true,
                isError = error != null,
                supportingText = {
                    error?.let { Text(it) }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("program_name_input"),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { submit() },
                modifier = Modifier.testTag("confirm_name"),
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun rememberStartWorkoutAction(onStart: () -> Unit): () -> Unit {
    val context = LocalContext.current
    var pendingStart by rememberSaveable { mutableStateOf(false) }
    var permissionAsked by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        if (pendingStart) {
            pendingStart = false
            onStart()
        }
    }

    return {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED

        if (needsPermission && !permissionAsked) {
            permissionAsked = true
            pendingStart = true
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onStart()
        }
    }
}

@Composable
private fun KeepScreenOn(enabled: Boolean) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    DisposableEffect(enabled, activity) {
        if (enabled) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@Composable
private fun intervalLabel(type: IntervalType?): String = when (type) {
    IntervalType.Prepare -> stringResource(R.string.interval_prepare)
    IntervalType.Work -> stringResource(R.string.interval_work)
    IntervalType.Rest -> stringResource(R.string.interval_rest)
    IntervalType.Cooldown -> stringResource(R.string.interval_cooldown)
    null -> stringResource(R.string.app_name)
}

@Composable
private fun intervalColor(type: IntervalType?): Color = when (type) {
    IntervalType.Work -> MaterialTheme.colorScheme.primary
    IntervalType.Rest -> MaterialTheme.colorScheme.secondary
    IntervalType.Prepare -> MaterialTheme.colorScheme.tertiary
    IntervalType.Cooldown -> MaterialTheme.colorScheme.secondary
    null -> MaterialTheme.colorScheme.primary
}

@Composable
private fun nextIntervalText(state: TimerState): String {
    val next = state.nextInterval ?: return stringResource(R.string.last_interval)
    return stringResource(
        R.string.next_up,
        intervalLabel(next.type),
        TimeFormat.clock(TimeFormat.ceilingSeconds(next.durationMs)),
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
