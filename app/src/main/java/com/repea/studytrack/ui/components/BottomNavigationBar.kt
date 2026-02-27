package com.repea.studytrack.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.repea.studytrack.ui.navigation.Screen
import com.repea.studytrack.ui.theme.GlassBorderDark
import com.repea.studytrack.ui.theme.GlassBorderLight
import com.repea.studytrack.ui.theme.GlassDark
import com.repea.studytrack.ui.theme.GlassLight

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Home,
        Screen.SubjectList,
        Screen.ExamList,
        Screen.Analysis,
        Screen.Settings
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (items.any { it.route == currentRoute }) {
        val backdrop = LocalLayerBackdrop.current
        val params = LocalLiquidGlassParams.current
        val density = LocalDensity.current
        val shape = RoundedCornerShape(28.dp)
        val isDark = MaterialTheme.colorScheme.onBackground.luminance() > 0.5f
        val glassColor = if (isDark) GlassDark else GlassLight
        val borderColor = if (isDark) GlassBorderDark else GlassBorderLight

        val containerModifier = if (backdrop != null && params.enabled) {
            val blurPx = with(density) { params.blurRadiusDp.dp.toPx() }
            val refractionHeightPx = with(density) { params.refractionHeightDp.dp.toPx() }
            val refractionAmountPx = with(density) { params.refractionAmountDp.dp.toPx() }
            Modifier.drawBackdrop(
                backdrop = backdrop,
                shape = { shape as Shape },
                effects = {
                    if (params.vibrancyEnabled) vibrancy()
                    blur(blurPx)
                    lens(
                        refractionHeight = refractionHeightPx,
                        refractionAmount = refractionAmountPx,
                        chromaticAberration = params.chromaticAberration
                    )
                },
                onDrawSurface = {
                    val outline = (shape as Shape).createOutline(size, layoutDirection, this)
                    val overlayColor = if (isDark) Color.Black else Color.White
                    val tint = overlayColor.copy(alpha = params.tintAlpha)
                    val border = overlayColor.copy(alpha = params.borderAlpha)
                    when (outline) {
                        is Outline.Rounded -> {
                            val rr = outline.roundRect
                            drawRoundRect(color = tint, cornerRadius = rr.topLeftCornerRadius)
                            drawRoundRect(
                                color = border,
                                cornerRadius = rr.topLeftCornerRadius,
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                        is Outline.Rectangle -> {
                            drawRect(tint)
                            drawRect(border, style = Stroke(width = 1.dp.toPx()))
                        }
                        is Outline.Generic -> {
                            drawPath(outline.path, tint)
                            drawPath(outline.path, border, style = Stroke(width = 1.dp.toPx()))
                        }
                    }
                }
            )
        } else {
            Modifier
                .shadow(8.dp, shape, spotColor = Color(0x20000000), ambientColor = Color(0x10000000))
                .clip(shape)
                .background(glassColor)
                .border(
                    1.dp,
                    Brush.verticalGradient(listOf(borderColor, borderColor.copy(alpha = 0.1f))),
                    shape
                )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(92.dp)
                .then(containerModifier)
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.12f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
                        label = "navIconScale"
                    )

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title,
                                modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                            )
                        },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        alwaysShowLabel = false
                    )
                }
            }
        }
    }
}
