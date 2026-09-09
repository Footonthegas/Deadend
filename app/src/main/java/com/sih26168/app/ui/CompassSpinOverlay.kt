package com.sih26168.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sih26168.app.ui.theme.mist100
import com.sih26168.app.ui.theme.lineTeal700
import com.sih26168.app.ui.theme.signalAmber
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CompassSpinOverlay(
    roughHeadingDeg: Double?,
    progress: Float,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rotate your phone in a full circle",
                color = mist100,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )

            Text(
                text = "This gives a rough starting direction",
                color = mist100.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(top = 16.dp)
            )

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(top = 32.dp)
            ) {
                val density = LocalDensity.current.density
                val strokeWidthPx = 8f * density
                val radius = 60f * density - strokeWidthPx

                val infiniteTransition = rememberInfiniteTransition(label = "hintRotation")
                val hintRotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(8000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "hintRotation"
                )

                val needleRotation by animateFloatAsState(
                    targetValue = roughHeadingDeg?.toFloat() ?: 0f,
                    animationSpec = tween(100, easing = LinearEasing),
                    label = "needleRotation"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f

                    // Track circle
                    drawCircle(
                        color = mist100.copy(alpha = 0.2f),
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )

                    // Filled arc for coverage progress
                    if (progress > 0f) {
                        drawArc(
                            color = signalAmber,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                    }

                    // Rotating arrow hint around track
                    val arrowLength = radius + strokeWidthPx
                    val angleRad = Math.toRadians((hintRotation - 90).toDouble()).toFloat()
                    val arrowEndX = centerX + arrowLength * cos(angleRad)
                    val arrowEndY = centerY + arrowLength * sin(angleRad)
                    drawLine(
                        color = lineTeal700,
                        start = Offset(centerX, centerY),
                        end = Offset(arrowEndX, arrowEndY),
                        strokeWidth = 2f * density
                    )

                    // Live needle (inside the ring)
                    val needleLength = radius * 0.6f
                    val needleAngleRad = Math.toRadians((needleRotation - 90).toDouble()).toFloat()
                    val needleEndX = centerX + needleLength * cos(needleAngleRad)
                    val needleEndY = centerY + needleLength * sin(needleAngleRad)
                    drawLine(
                        color = signalAmber,
                        start = Offset(centerX, centerY),
                        end = Offset(needleEndX, needleEndY),
                        strokeWidth = 3f * density
                    )
                }

                // N label at top of ring
                Text(
                    text = "N",
                    color = mist100,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-12).dp)
                )
            }

            val headingText = roughHeadingDeg?.let {
                val direction = getDirection(it)
                "approx. ${it.roundToInt()}° $direction"
            } ?: "approx. --° --"

            Text(
                text = headingText,
                color = mist100,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 24.dp)
            )
        }

        val interactionSource = remember { MutableInteractionSource() }
        Text(
            text = "Skip",
            color = mist100.copy(alpha = 0.6f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(safeDrawingPadding())
                .padding(top = 16.dp, end = 16.dp)
                .clickable(interactionSource = interactionSource, indication = null) { onSkip() }
        )
    }
}

private fun getDirection(degrees: Double): String {
    val dirs = listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
    val index = ((degrees / 22.5) + 0.5).toInt() % 16
    return dirs[index]
}
