package com.repea.studytrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.repea.studytrack.repository.AppThemeStyle
import com.repea.studytrack.ui.theme.BackgroundLight
import com.repea.studytrack.ui.theme.BlobColor1
import com.repea.studytrack.ui.theme.BlobColor2
import com.repea.studytrack.ui.theme.BlobColor3
import com.repea.studytrack.ui.theme.BlobColor4
import com.repea.studytrack.ui.theme.GlassBorderLight
import com.repea.studytrack.ui.theme.GlassLight
import com.repea.studytrack.ui.theme.LocalAppThemeStyle
import coil.compose.AsyncImage

data class LiquidGlassParams(
    val blurRadiusDp: Float = 0f,
    val refractionHeightDp: Float = 15f,
    val refractionAmountDp: Float = 20f,
    val tintAlpha: Float = 0f,
    val borderAlpha: Float = 0.20f,
    val vibrancyEnabled: Boolean = true,
    val chromaticAberration: Boolean = true
)

val LocalLayerBackdrop = compositionLocalOf<LayerBackdrop?> { null }
val LocalLiquidGlassParams = compositionLocalOf { LiquidGlassParams() }

@Composable
fun ProvideLiquidGlass(
    backdrop: LayerBackdrop?,
    params: LiquidGlassParams = LiquidGlassParams(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLayerBackdrop provides backdrop,
        LocalLiquidGlassParams provides params,
        content = content
    )
}

@Composable
fun LiquidBackground(
    modifier: Modifier = Modifier,
    backdrop: LayerBackdrop? = null,
    themeStyle: AppThemeStyle = AppThemeStyle.PURE_WHITE,
    customWallpaperUri: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )

            if (themeStyle == AppThemeStyle.LIQUID_GLASS) {
                if (!customWallpaperUri.isNullOrBlank()) {
                    AsyncImage(
                        model = customWallpaperUri,
                        contentDescription = "自定义壁纸",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.24f))
                    )
                }
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .offset(x = (-48).dp, y = 72.dp)
                        .blur(42.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(BlobColor1.copy(alpha = 0.42f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .offset(x = 160.dp, y = 180.dp)
                        .blur(56.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(BlobColor2.copy(alpha = 0.46f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .offset(x = 96.dp, y = 520.dp)
                        .blur(36.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(BlobColor3.copy(alpha = 0.26f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .offset(x = (-24).dp, y = 620.dp)
                        .blur(28.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(BlobColor4.copy(alpha = 0.56f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )
            }
        }

        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    elevation: Dp = 10.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val themeStyle = LocalAppThemeStyle.current
    val backdrop = LocalLayerBackdrop.current
    val params = LocalLiquidGlassParams.current
    val density = LocalDensity.current

    val glassModifier = if (themeStyle == AppThemeStyle.LIQUID_GLASS && backdrop != null) {
        val blurPx = with(density) { params.blurRadiusDp.dp.toPx() }
        val refractionHeightPx = with(density) { params.refractionHeightDp.dp.toPx() }
        val refractionAmountPx = with(density) { params.refractionAmountDp.dp.toPx() }
        Modifier
            .drawBackdrop(
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
                    val overlayColor = Color.White
                    when (outline) {
                        is Outline.Rounded -> {
                            val rr = outline.roundRect
                            drawRoundRect(
                                color = overlayColor.copy(alpha = params.tintAlpha),
                                cornerRadius = rr.topLeftCornerRadius
                            )
                            drawRoundRect(
                                color = overlayColor.copy(alpha = params.borderAlpha),
                                cornerRadius = rr.topLeftCornerRadius,
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                        is Outline.Rectangle -> {
                            drawRect(overlayColor.copy(alpha = params.tintAlpha))
                            drawRect(
                                color = overlayColor.copy(alpha = params.borderAlpha),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                        is Outline.Generic -> {
                            drawPath(outline.path, overlayColor.copy(alpha = params.tintAlpha))
                            drawPath(
                                path = outline.path,
                                color = overlayColor.copy(alpha = params.borderAlpha),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                    }
                }
            )
    } else {
        if (themeStyle == AppThemeStyle.PURE_WHITE) {
            Modifier
                .shadow(
                    elevation = 18.dp,
                    shape = shape,
                    spotColor = Color(0x260B1020),
                    ambientColor = Color(0x180B1020)
                )
                .clip(shape)
                .background(BackgroundLight)
                .border(
                    1.dp,
                    Color(0xFFE9EEF7),
                    shape
                )
        } else {
            Modifier
                .shadow(
                    elevation = elevation,
                    shape = shape,
                    spotColor = Color(0x120B1020),
                    ambientColor = Color(0x080B1020)
                )
                .clip(shape)
                .background(GlassLight)
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            GlassBorderLight.copy(alpha = 0.9f),
                            GlassBorderLight.copy(alpha = 0.16f)
                        )
                    ),
                    shape
                )
        }
    }

    Box(
        modifier = modifier
            .then(glassModifier)
            .padding(contentPadding)
    ) {
        content()
    }
}

@Composable
fun GlassDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        GlassCard(contentPadding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), content = content)
        }
    }
}
