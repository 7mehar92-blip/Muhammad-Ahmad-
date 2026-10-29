package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPurple
import kotlin.math.roundToInt

@Composable
fun FloatingControlBall(
    isScanning: Boolean,
    isAutoActive: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(260f) }
    var offsetY by remember { mutableFloatStateOf(160f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(76.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX = (offsetX + dragAmount.x).coerceIn(10f, 750f)
                    offsetY = (offsetY + dragAmount.y).coerceIn(60f, 1500f)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress() }
                )
            }
            .testTag("floating_control_ball"),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing energy ring
        Canvas(modifier = Modifier.size(76.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val ringRadius = (size.width / 2.3f) * if (isScanning) pulseScale else 1.05f
            drawCircle(
                color = if (isScanning) CyberCyan.copy(alpha = 0.5f) else CyberPurple.copy(alpha = 0.35f),
                radius = ringRadius
            )
        }

        // Inner glowing core button
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(16.dp, CircleShape, spotColor = CyberCyan)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF8A5BFF),
                            Color(0xFF5414C7),
                            Color(0xFF260563)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // High-tech AI center icon and label
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Floating Agent",
                    tint = CyberCyan,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "AI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.offset(y = 14.dp)
                )
            }

            // Status indicator dot
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-10).dp, y = 10.dp)
                    .clip(CircleShape)
                    .background(if (isAutoActive) CyberGreen else Color.Gray)
            )
        }
    }
}
