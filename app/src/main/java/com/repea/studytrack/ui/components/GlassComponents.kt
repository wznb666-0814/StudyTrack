package com.repea.studytrack.ui.components

import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.repea.studytrack.ui.theme.*

import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

data class LiquidGlassParams(
    val enabled: Boolean = true,
    val blurRadiusDp: Float = 10f,
    val refractionHeightDp: Float = 25f,
    val refractionAmountDp: Float = 30f,
    val tintAlpha: Float = 0.15f,
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
    wallpaperUri: String? = null,
    defaultWallpaperResId: Int? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        val contentOnDarkBackground = MaterialTheme.colorScheme.onBackground.luminance() > 0.5f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (contentOnDarkBackground) BackgroundDark else BackgroundLight)
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)
        ) {
            val context = LocalContext.current
            val wallpaperBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = wallpaperUri) {
                value = wallpaperUri?.takeIf { it.isNotBlank() }?.let { uriString ->
                    runCatching {
                        val uri = Uri.parse(uriString)
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source).asImageBitmap()
                    }.getOrNull()
                }
            }

            if (wallpaperBitmap != null) {
                Image(
                    bitmap = wallpaperBitmap!!,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (defaultWallpaperResId != null) {
                Image(
                    painter = painterResource(defaultWallpaperResId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 主内容保持在 backdrop 层之上，恢复为之前稳定的用法
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    elevation: Dp = 8.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onBackground.luminance() > 0.5f
    val glassColor = if (isDark) GlassDark else GlassLight
    val borderColor = if (isDark) GlassBorderDark else GlassBorderLight
    val backdrop = LocalLayerBackdrop.current
    val params = LocalLiquidGlassParams.current
    val density = LocalDensity.current

    val glassModifier = if (backdrop != null && params.enabled) {
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
                    val overlayColor = if (isDark) Color.Black else Color.White
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
        Modifier
            .shadow(elevation, shape, spotColor = Color(0x20000000), ambientColor = Color(0x10000000))
            .clip(shape)
            .background(glassColor)
            .border(1.dp, Brush.verticalGradient(listOf(borderColor, borderColor.copy(alpha = 0.1f))), shape)
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
