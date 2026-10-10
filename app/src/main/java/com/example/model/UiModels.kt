package com.example.model

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.vector.ImageVector

enum class NovaState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null
)

data class QuickSettingTile(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val actionIntent: String
)

data class GroundingSource(
    val title: String,
    val url: String
)
