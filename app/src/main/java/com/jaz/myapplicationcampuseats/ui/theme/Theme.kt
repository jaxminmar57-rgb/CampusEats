package com.jaz.myapplicationcampuseats.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// CampusEats siempre usa tema oscuro (la app es dark-only)
private val CampusEatsColorScheme = darkColorScheme(
    primary       = GreenPrimary,
    onPrimary     = Color.White,
    primaryContainer = GreenDark,

    secondary     = TealReady,
    onSecondary   = Color.White,

    tertiary      = OrangeWarning,
    onTertiary    = Color.White,

    background    = DarkBackground,
    onBackground  = Color.White,

    surface       = DarkSurface,
    onSurface     = Color.White,

    surfaceVariant = DarkSurface2,
    onSurfaceVariant = Color(0xFFB0B0B0),

    error         = RedError,
    onError       = Color.White,

    outline       = Color.White.copy(alpha = 0.2f),
    outlineVariant = Color.White.copy(alpha = 0.1f)
)

@Composable
fun CampusEatsTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = CampusEatsColorScheme

    // Status bar oscura
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
