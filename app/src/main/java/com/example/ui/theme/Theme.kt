package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Custom Vercel design tokens
data class VercelCustomColors(
    val border: Color,
    val surfaceSubtle: Color,
    val badgeBg: Color,
    val textMuted: Color,
    val monoText: Color
)

val LocalVercelColors = staticCompositionLocalOf {
    VercelCustomColors(
        border = VercelBorderLight,
        surfaceSubtle = VercelGray50,
        badgeBg = VercelGray100,
        textMuted = VercelGray500,
        monoText = VercelBlack
    )
}

private val VercelDarkColorScheme = darkColorScheme(
    primary = VercelWhite,
    onPrimary = VercelBlack,
    primaryContainer = VercelGray900,
    onPrimaryContainer = VercelWhite,
    secondary = VercelGray300,
    onSecondary = VercelBlack,
    background = VercelBlack,
    onBackground = VercelWhite,
    surface = VercelBlack,
    onSurface = VercelWhite,
    surfaceVariant = VercelNearBlack,
    onSurfaceVariant = VercelGray400,
    outline = VercelBorderDark,
    error = VercelRed,
    onError = VercelWhite
)

private val VercelLightColorScheme = lightColorScheme(
    primary = VercelBlack,
    onPrimary = VercelWhite,
    primaryContainer = VercelGray100,
    onPrimaryContainer = VercelBlack,
    secondary = VercelGray700,
    onSecondary = VercelWhite,
    background = VercelWhite,
    onBackground = VercelBlack,
    surface = VercelWhite,
    onSurface = VercelBlack,
    surfaceVariant = VercelGray50,
    onSurfaceVariant = VercelGray600,
    outline = VercelBorderLight,
    error = VercelRed,
    onError = VercelWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VercelDarkColorScheme else VercelLightColorScheme

    val customColors = if (darkTheme) {
        VercelCustomColors(
            border = VercelBorderDark,
            surfaceSubtle = VercelNearBlack,
            badgeBg = VercelGray900,
            textMuted = VercelGray400,
            monoText = VercelWhite
        )
    } else {
        VercelCustomColors(
            border = VercelBorderLight,
            surfaceSubtle = VercelGray50,
            badgeBg = VercelGray100,
            textMuted = VercelGray500,
            monoText = VercelBlack
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalVercelColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = VercelTypography,
            shapes = VercelShapes,
            content = content
        )
    }
}

