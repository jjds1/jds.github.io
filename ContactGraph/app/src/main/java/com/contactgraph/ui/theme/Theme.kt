package com.contactgraph.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4FC3F7),
    secondary = Color(0xFFFF6F00),
    background = Color(0xFF0A0E1A),
    surface = Color(0xFF12182B),
    surfaceVariant = Color(0xFF1E2740),
    onBackground = Color(0xFFE8EAF6),
    onSurface = Color(0xFFE8EAF6),
    onSurfaceVariant = Color(0xFF9EA7C4),
)

@Composable
fun ContactGraphTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content,
    )
}
