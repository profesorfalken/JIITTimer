package com.profesorfalken.jiittimer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFC2410C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBCB),
    onPrimaryContainer = Color(0xFF3A0B00),
    secondary = Color(0xFF006A60),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF9EF2E8),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = Color(0xFF4A5C92),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDBE1FF),
    onTertiaryContainer = Color(0xFF00174B),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFF8F5),
    onBackground = Color(0xFF221A16),
    surface = Color(0xFFFFF8F5),
    onSurface = Color(0xFF221A16),
    surfaceVariant = Color(0xFFF5DED5),
    onSurfaceVariant = Color(0xFF53433D),
    outline = Color(0xFF85736C),
    outlineVariant = Color(0xFFD8C2B8),
    inverseSurface = Color(0xFF382E2A),
    inverseOnSurface = Color(0xFFF1DFD8),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB59A),
    onPrimary = Color(0xFF5A1B00),
    primaryContainer = Color(0xFF7D2E0A),
    onPrimaryContainer = Color(0xFFFFDBCB),
    secondary = Color(0xFF82D5CA),
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF005049),
    onSecondaryContainer = Color(0xFF9EF2E8),
    tertiary = Color(0xFFB3C5FF),
    onTertiary = Color(0xFF1B2E60),
    tertiaryContainer = Color(0xFF324478),
    onTertiaryContainer = Color(0xFFDBE1FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A120F),
    onBackground = Color(0xFFF1DFD8),
    surface = Color(0xFF1A120F),
    onSurface = Color(0xFFF1DFD8),
    surfaceVariant = Color(0xFF53433D),
    onSurfaceVariant = Color(0xFFD8C2B8),
    outline = Color(0xFFA08C84),
    outlineVariant = Color(0xFF53433D),
    inverseSurface = Color(0xFFF1DFD8),
    inverseOnSurface = Color(0xFF382E2A),
)

@Composable
fun JiitTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = JiitTypography,
        content = content,
    )
}
