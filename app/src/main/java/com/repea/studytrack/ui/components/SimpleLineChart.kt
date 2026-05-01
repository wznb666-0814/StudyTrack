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
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

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
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    val typeface = remember {
        ResourcesCompat.getFont(context, R.font.font)
    }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(dataPoints) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 900))
    }

    Canvas(modifier = modifier) {
        val chartPaddingLeft = 54.dp.toPx()
        val chartPaddingRight = 20.dp.toPx()
        val chartPaddingTop = 20.dp.toPx()
        val chartPaddingBottom = 30.dp.toPx()
        val chartWidth = (size.width - chartPaddingLeft - chartPaddingRight).coerceAtLeast(1f)
        val chartHeight = (size.height - chartPaddingTop - chartPaddingBottom).coerceAtLeast(1f)
        val axis = calculateAxis(dataPoints, isRanking)
        val steps = axis.labels

        val labelPaint = android.graphics.Paint().apply {
            color = textColor.copy(alpha = 0.72f).toArgb()
            textSize = 11.sp.toPx()
            textAlign = android.graphics.Paint.Align.RIGHT
            this.typeface = typeface
            isAntiAlias = true
        }

        val pointLabelPaint = android.graphics.Paint().apply {
            color = textColor.toArgb()
            textSize = 11.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            this.typeface = typeface
            isAntiAlias = true
        }

        fun valueToY(value: Double): Float {
            val normalized = ((value - axis.min) / (axis.max - axis.min)).toFloat().coerceIn(0f, 1f)
            return if (isRanking) {
                chartPaddingTop + normalized * chartHeight
            } else {
                chartPaddingTop + (1f - normalized) * chartHeight
            }
        }

        steps.forEach { labelValue ->
            val y = valueToY(labelValue)
            drawLine(
                color = gridColor,
                start = Offset(chartPaddingLeft, y),
                end = Offset(size.width - chartPaddingRight, y),
                strokeWidth = 1.dp.toPx()
            )
            drawContext.canvas.nativeCanvas.drawText(
                formatAxisValue(labelValue),
                chartPaddingLeft - 10.dp.toPx(),
                y + 4.dp.toPx(),
                labelPaint
            )
        }

        drawLine(
            color = textColor.copy(alpha = 0.16f),
            start = Offset(chartPaddingLeft, chartPaddingTop),
            end = Offset(chartPaddingLeft, size.height - chartPaddingBottom),
            strokeWidth = 1.2.dp.toPx()
        )
        drawLine(
            color = textColor.copy(alpha = 0.16f),
            start = Offset(chartPaddingLeft, size.height - chartPaddingBottom),
            end = Offset(size.width - chartPaddingRight, size.height - chartPaddingBottom),
            strokeWidth = 1.2.dp.toPx()
        )

        if (dataPoints.size == 1) {
            val point = Offset(chartPaddingLeft + chartWidth / 2f, valueToY(dataPoints.first()))
            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = point)
            return@Canvas
        }

        val xStep = chartWidth / max(dataPoints.size - 1, 1)
        val points = dataPoints.mapIndexed { index, value ->
            Offset(
                x = chartPaddingLeft + index * xStep,
                y = valueToY(value)
            )
        }

        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { point ->
                lineTo(point.x, point.y)
            }
        }

        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(points.last().x, size.height - chartPaddingBottom)
            lineTo(points.first().x, size.height - chartPaddingBottom)
            close()
        }

        drawPath(
            path = fillPath,
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.14f), Color.Transparent)
            )
        )

        val pathMeasure = PathMeasure()
        pathMeasure.setPath(linePath, false)
        val animatedPath = Path()
        pathMeasure.getSegment(0f, pathMeasure.length * animationProgress.value, animatedPath, true)
        drawPath(
            path = animatedPath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        val showPointLabels = dataPoints.size <= 8
        points.forEachIndexed { index, point ->
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = point)
            drawCircle(color = lineColor, radius = 3.dp.toPx(), center = point)
            if (showPointLabels) {
                val labelOffset = if (index % 2 == 0) -12.dp.toPx() else 18.dp.toPx()
                val labelY = (point.y + labelOffset).coerceIn(chartPaddingTop + 8.dp.toPx(), size.height - chartPaddingBottom - 6.dp.toPx())
                drawContext.canvas.nativeCanvas.drawText(
                    formatPointValue(dataPoints[index]),
                    point.x,
                    labelY,
                    pointLabelPaint
                )
            }
        }
    }
}

private data class ChartAxis(
    val min: Double,
    val max: Double,
    val step: Double,
    val labels: List<Double>
)

private fun calculateAxis(dataPoints: List<Double>, isRanking: Boolean): ChartAxis {
    val dataMin = dataPoints.minOrNull() ?: 0.0
    val dataMax = dataPoints.maxOrNull() ?: 0.0
    val rawRange = max(dataMax - dataMin, 1.0)
    val visualPadding = if (isRanking) max(rawRange * 0.18, 1.0) else max(rawRange * 0.25, 2.0)
    val paddedMin = if (isRanking) max(1.0, dataMin - visualPadding) else max(0.0, dataMin - visualPadding)
    val paddedMax = dataMax + visualPadding
    val targetSteps = 4
    val step = niceStep((paddedMax - paddedMin) / targetSteps)
    val niceMin = if (isRanking) {
        max(1.0, floor(paddedMin / step) * step)
    } else {
        max(0.0, floor(paddedMin / step) * step)
    }
    val niceMax = ceil(paddedMax / step) * step

    val labels = buildList {
        var current = niceMin
        var guard = 0
        while (current <= niceMax + step * 0.5 && guard < 12) {
            add(current)
            current += step
            guard++
        }
        if (isEmpty()) {
            add(niceMin)
            add(niceMax)
        }
    }

    val finalMax = if (abs(niceMax - niceMin) < 0.0001) niceMin + step else niceMax
    return ChartAxis(min = niceMin, max = finalMax, step = step, labels = labels)
}

private fun niceStep(rawStep: Double): Double {
    if (rawStep <= 0) return 1.0
    val exponent = floor(ln(rawStep) / ln(10.0))
    val magnitude = 10.0.pow(exponent)
    val normalized = rawStep / magnitude
    val niceNormalized = when {
        normalized <= 1.0 -> 1.0
        normalized <= 2.0 -> 2.0
        normalized <= 5.0 -> 5.0
        else -> 10.0
    }
    return niceNormalized * magnitude
}

private fun formatAxisValue(value: Double): String {
    return if (abs(value - value.toInt().toDouble()) < 0.001) {
        value.toInt().toString()
    } else {
        String.format("%.1f", value)
    }
}

private fun formatPointValue(value: Double): String {
    return if (abs(value - value.toInt().toDouble()) < 0.001) {
        value.toInt().toString()
    } else {
        String.format("%.1f", value)
    }
}
