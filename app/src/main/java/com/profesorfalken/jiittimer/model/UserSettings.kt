package com.profesorfalken.jiittimer.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
)
