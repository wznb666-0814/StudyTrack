package com.repea.studytrack.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = BackgroundDark,
    surface = GlassDark,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.LightGray,
    outline = Color.LightGray
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = BackgroundLight,
    surface = GlassLight,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun StudyTrackTheme(
    darkTheme: Boolean = false,
    contentOnDarkBackground: Boolean? = null,
    primaryOverride: Color? = null,
    content: @Composable () -> Unit
) {
    val foregroundForDarkBackground = contentOnDarkBackground ?: darkTheme
    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val overrideOnSurface = if (foregroundForDarkBackground) Color.White else Color.Black
    val overrideOnSurfaceVariant = if (foregroundForDarkBackground) Color.White.copy(alpha = 0.72f) else Color.Black.copy(alpha = 0.72f)
    val overrideOutline = if (foregroundForDarkBackground) Color.White.copy(alpha = 0.36f) else Color.Black.copy(alpha = 0.28f)

    val baseWithForeground = baseScheme.copy(
        onSurface = overrideOnSurface,
        onBackground = overrideOnSurface,
        onSurfaceVariant = overrideOnSurfaceVariant,
        outline = overrideOutline
    )
    val colorScheme = if (primaryOverride != null) {
        val onPrimary = if (primaryOverride.luminance() > 0.55f) Color.Black else Color.White
        baseWithForeground.copy(
            primary = primaryOverride,
            secondary = primaryOverride,
            tertiary = primaryOverride,
            onPrimary = onPrimary,
            onSecondary = onPrimary,
            onTertiary = onPrimary
        )
    } else {
        baseWithForeground
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !foregroundForDarkBackground
            controller.isAppearanceLightNavigationBars = !foregroundForDarkBackground
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
