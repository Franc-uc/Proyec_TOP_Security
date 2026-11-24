package com.example.proyec_top.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores Definidos
val NavyDark = Color(0xFF081726)
val NavyMid = Color(0xFF0F2942)
val CyanGlow = Color(0xFF00C6FF)
val EmeraldOk = Color(0xFF059669)
val AmberWarn = Color(0xFFD97706)

private val TopSecurityColorScheme = lightColorScheme(
    primary = NavyMid,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E8FF),
    onPrimaryContainer = Color(0xFF07192B),
    secondary = Color(0xFF0088CC),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9F2FF),
    onSecondaryContainer = Color(0xFF002B42),
    tertiary = AmberWarn,
    onTertiary = Color.White,
    background = Color(0xFFEEF3F8),
    onBackground = Color(0xFF102030),
    surface = Color.White,
    onSurface = Color(0xFF102030),
    surfaceVariant = Color(0xFFE1E9F2),
    onSurfaceVariant = Color(0xFF435568),
    outline = Color(0xFF8FA3B8),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun TopSecurityTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TopSecurityColorScheme,
        content = content
    )
}