package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Geometric3DCanvas(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF00E5FF),
    secondaryColor: Color = Color(0xFFFFB300),
    accentColor: Color = Color(0xFFB388FF),
    shapeType: GeometricShapeType = GeometricShapeType.TETRAHEDRON_COMPLEX
) {
    val infiniteTransition = rememberInfiniteTransition(label = "3D_rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = minOf(size.width, size.height) * 0.38f
        val rad = Math.toRadians(angle.toDouble()).toFloat()

        // Outer ambient glowing ring
        drawCircle(
            color = primaryColor.copy(alpha = 0.12f),
            radius = radius * 1.1f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 1.5f)
        )

        // Orbital nodes
        val nodeCount = 5
        val points = mutableListOf<Offset>()
        for (i in 0 until nodeCount) {
            val nodeAngle = rad + i * (2 * Math.PI / nodeCount).toFloat()
            val x = centerX + cos(nodeAngle) * radius * 0.75f
            val y = centerY + sin(nodeAngle) * radius * 0.45f
            val offset = Offset(x, y)
            points.add(offset)

            // Draw glowing vertex
            drawCircle(
                color = if (i % 2 == 0) primaryColor.copy(alpha = 0.6f) else secondaryColor.copy(alpha = 0.6f),
                radius = 5f,
                center = offset
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = 2.5f,
                center = offset
            )
        }

        // Draw connecting futuristic polygon
        if (points.size >= 3) {
            val polyPath = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
                close()
            }
            drawPath(
                path = polyPath,
                color = primaryColor.copy(alpha = 0.05f)
            )
            drawPath(
                path = polyPath,
                color = primaryColor.copy(alpha = 0.4f),
                style = Stroke(width = 1.8f)
            )

            // Cross-connecting accents
            for (i in points.indices) {
                val nextIdx = (i + 2) % points.size
                drawLine(
                    color = accentColor.copy(alpha = 0.25f),
                    start = points[i],
                    end = points[nextIdx],
                    strokeWidth = 1.2f
                )
            }
        }
    }
}

enum class GeometricShapeType {
    TETRAHEDRON_COMPLEX,
    ORBITAL_TRIANGLES,
    PRISM_CUBE
}

