package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF163F40), // Dark Gold primary brand button teal
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF163F40),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFD4AF37), // Accent Gold
    onSecondary = Color(0xFF090D0E),
    secondaryContainer = Color(0xFF12191A),
    onSecondaryContainer = Color(0xFFD4AF37),
    tertiary = Color(0xFFD4AF37),
    background = Color(0xFF090D0E),
    onBackground = Color(0xFFF4F6F6),
    surface = Color(0xFF12191A),
    onSurface = Color(0xFFF4F6F6),
    surfaceVariant = Color(0xFF12191A),
    onSurfaceVariant = Color(0xFF9AA7A8),
    outline = Color(0xFF293031),
    error = Color(0xFFF43F5E)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF163F40), // Consistent primary brand button teal
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD2DBDC),
    onPrimaryContainer = Color(0xFF163F40),
    secondary = Color(0xFFD4AF37), // Accent Gold
    onSecondary = Color(0xFF090D0E),
    secondaryContainer = Color(0xFFFFFFFF),
    onSecondaryContainer = Color(0xFFD4AF37),
    tertiary = Color(0xFFD4AF37),
    background = Color(0xFFF4F6F6),
    onBackground = Color(0xFF090D0E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF090D0E),
    surfaceVariant = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF5F6C6D),
    outline = Color(0xFFD2DBDC),
    error = Color(0xFFD32F2F)
)

class ThemeState(initialDark: Boolean = true) {
    var isDarkTheme by mutableStateOf(initialDark)
}

val LocalThemeState = compositionLocalOf { ThemeState() }

object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = LocalThemeState.current.isDarkTheme,
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkGoldColors else LightGoldColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    CompositionLocalProvider(
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
