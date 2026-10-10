package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val CyberDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = VoidDark,
    primaryContainer = CyberBorderDark,
    onPrimaryContainer = CyberCyan,
    secondary = CyberPurple,
    onSecondary = Color.White,
    secondaryContainer = CyberSurfaceVariantDark,
    onSecondaryContainer = CyberPurple,
    tertiary = CyberTeal,
    onTertiary = VoidDark,
    background = VoidDark,
    onBackground = TextPrimaryDark,
    surface = CyberSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CyberSurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = CyberBorderDark
)

private val CyberLightColorScheme = lightColorScheme(
    primary = CyberCyanLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7F3F9),
    onPrimaryContainer = Color(0xFF00363E),
    secondary = CyberPurpleLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E5F5),
    onSecondaryContainer = Color(0xFF38006B),
    tertiary = CyberTeal,
    onTertiary = Color.Black,
    background = CyberBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = CyberSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFE2EBF5),
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFB0C4DE)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to stunning Cyberpunk Dark Theme
    dynamicColor: Boolean = false, // Keep cyber aesthetics consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CyberDarkColorScheme
        else -> CyberLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
