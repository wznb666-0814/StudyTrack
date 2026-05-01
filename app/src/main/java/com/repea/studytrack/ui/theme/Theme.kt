package com.repea.studytrack.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.repea.studytrack.repository.AppThemeStyle

val LocalAppThemeStyle = staticCompositionLocalOf { AppThemeStyle.PURE_WHITE }

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color.White,
    surface = SurfaceLight,
    surfaceVariant = SurfaceMutedLight,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF202634),
    onSurfaceVariant = Color(0xFF7B8497),
    outline = Color(0xFFD7DEEB),
    error = ErrorLight
)

@Composable
fun StudyTrackTheme(
    themeStyle: AppThemeStyle = AppThemeStyle.PURE_WHITE,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }
    }

    CompositionLocalProvider(LocalAppThemeStyle provides themeStyle) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}
