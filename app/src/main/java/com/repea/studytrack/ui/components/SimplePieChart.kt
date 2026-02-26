package com.repea.studytrack.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

data class PieChartData(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun SimplePieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier,
    thickness: Float = 60f, // Thicker lines
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    if (total == 0f) return

    val animationProgress = remember { Animatable(0f) }
    val textColorInt = textColor.toArgb()

    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 1000))
    }

    Box(modifier = modifier.aspectRatio(1f)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.minDimension / 2) - thickness
            
            var startAngle = -90f

            data.forEach { item ->
                val sweepAngle = (item.value / total) * 360f * animationProgress.value
                
                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = thickness)
                )

                // Draw Label if segment is large enough
                if (sweepAngle > 15f && animationProgress.value > 0.9f) {
                    val angleInRadians = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                    val labelRadius = radius + thickness + 20f
                    val labelX = center.x + labelRadius * cos(angleInRadians).toFloat()
                    val labelY = center.y + labelRadius * sin(angleInRadians).toFloat()

                    drawContext.canvas.nativeCanvas.drawText(
                        "${item.label} ${(item.value / total * 100).toInt()}%",
                        labelX,
                        labelY,
                        android.graphics.Paint().apply {
                            color = textColorInt
                            textSize = 12.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }

                startAngle += sweepAngle
            }
        }
    }
}
