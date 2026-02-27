package com.repea.studytrack.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.repea.studytrack.R
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SimpleLineChart(
    dataPoints: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    isRanking: Boolean = false
) {
    if (dataPoints.isEmpty()) return

    val context = LocalContext.current
    val typeface = remember {
        ResourcesCompat.getFont(context, R.font.font)
    }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(dataPoints) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 1000))
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()
        
        val actualMax = dataPoints.maxOrNull() ?: 100.0
        
        // For ranking, lower is better (top of chart), so minVal is at top.
        // For score, higher is better (top of chart), so maxVal is at top.
        val maxVal = if (isRanking) actualMax.coerceAtLeast(10.0) else actualMax.coerceAtLeast(10.0)
        val minVal = if (isRanking) 1.0 else 0.0
        val range = (maxVal - minVal).coerceAtLeast(1.0)
        
        // Draw Y Axis Labels & Grid（使用全局字体）
        val textPaint = android.graphics.Paint().apply {
            color = textColor.toArgb()
            textSize = 12.sp.toPx()
            textAlign = android.graphics.Paint.Align.RIGHT
            this.typeface = typeface
        }
        
        val ySteps = if (height < 180.dp.toPx()) 3 else 5
        var lastLabelY: Float? = null
        for (i in 0..ySteps) {
            val fraction = i.toFloat() / ySteps
            val yVal = minVal + (range * fraction)
            
            // If ranking, minVal (1) is at top (padding), maxVal is at bottom (height-padding)
            // If score, maxVal is at top (padding), minVal is at bottom (height-padding)
            val y = if (isRanking) {
                padding + fraction * (height - 2 * padding)
            } else {
                height - padding - fraction * (height - 2 * padding)
            }
            
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1.dp.toPx()
            )
            
            val canDrawLabel = lastLabelY == null || abs(y - lastLabelY!!) > 16f
            if (canDrawLabel) {
                drawContext.canvas.nativeCanvas.drawText(
                    yVal.roundToInt().toString(),
                    padding - 10f,
                    y + 10f,
                    textPaint
                )
                lastLabelY = y
            }
        }

        // Draw Axes
        drawLine(
            color = textColor.copy(alpha = 0.5f),
            start = Offset(padding, padding),
            end = Offset(padding, height - padding),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = textColor.copy(alpha = 0.5f),
            start = Offset(padding, height - padding),
            end = Offset(width - padding, height - padding),
            strokeWidth = 2.dp.toPx()
        )

        if (dataPoints.size < 2) {
             // Draw single point
             if (dataPoints.isNotEmpty()) {
                 val value = dataPoints[0]
                 val y = if (isRanking) {
                     padding + ((value - minVal) / range * (height - 2 * padding)).toFloat()
                 } else {
                     height - padding - ((value - minVal) / range * (height - 2 * padding)).toFloat()
                 }
                 drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(width/2, y))
             }
             return@Canvas
        }

        val xStep = (width - 2 * padding) / (dataPoints.size - 1)
        val points = dataPoints.mapIndexed { index, value ->
            val x = padding + index * xStep
            val y = if (isRanking) {
                padding + ((value - minVal) / range * (height - 2 * padding)).toFloat()
            } else {
                height - padding - ((value - minVal) / range * (height - 2 * padding)).toFloat()
            }
            Offset(x, y)
        }

        // Create Path
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }

        // Animate Path (Simple clip logic or path measure)
        // Using PathMeasure for drawing partial path
        val pathMeasure = PathMeasure()
        pathMeasure.setPath(path, false)
        val pathLength = pathMeasure.length
        val animatedPath = Path()
        // If length is 0 (all points same), just draw line
        if (pathLength == 0f) {
             // draw line
        } else {
             pathMeasure.getSegment(0f, pathLength * animationProgress.value, animatedPath, true)
             drawPath(
                path = animatedPath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Draw Points
        if (animationProgress.value > 0.95f) {
            points.forEachIndexed { index, point ->
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
                val offsetY = if (index % 2 == 0) -18f else 18f
                val labelY = (point.y + offsetY).coerceIn(padding, height - padding)
                drawContext.canvas.nativeCanvas.drawText(
                    dataPoints[index].toInt().toString(),
                    point.x,
                    labelY,
                    textPaint.apply { 
                        textAlign = android.graphics.Paint.Align.CENTER 
                        color = textColor.toArgb()
                    }
                )
            }
        }
    }
}
