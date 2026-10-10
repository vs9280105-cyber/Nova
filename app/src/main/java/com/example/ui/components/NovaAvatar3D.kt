package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.NovaState
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberTeal

@Composable
fun NovaAvatar3D(
    state: NovaState,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    compact: Boolean = false,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NovaAvatarAnimation")

    // Vertical floating motion (gentle hover)
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingOffset"
    )

    // Holographic 360-degree rotation
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    NovaState.THINKING -> 2000
                    NovaState.LISTENING -> 3500
                    NovaState.SPEAKING -> 4000
                    else -> 8000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    // Pulse scale for energy/soundwave states
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = when (state) {
            NovaState.SPEAKING -> 1.08f
            NovaState.LISTENING -> 1.05f
            NovaState.THINKING -> 1.03f
            else -> 1.01f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    NovaState.SPEAKING -> 450
                    NovaState.LISTENING -> 600
                    else -> 1800
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Reactive glow colors
    val primaryAuraColor = when (state) {
        NovaState.LISTENING -> CyberCyan
        NovaState.SPEAKING -> CyberTeal
        NovaState.THINKING -> CyberPurple
        NovaState.ERROR -> CyberMagenta
        NovaState.IDLE -> CyberCyan.copy(alpha = 0.85f)
    }

    val secondaryAuraColor = when (state) {
        NovaState.LISTENING -> CyberTeal
        NovaState.SPEAKING -> CyberMagenta
        NovaState.THINKING -> CyberCyan
        NovaState.ERROR -> Color(0xFFFF4D4D)
        NovaState.IDLE -> CyberPurple.copy(alpha = 0.7f)
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("nova_avatar_3d")
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Holographic Rotating Aura Rings
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scale(pulseScale)
        ) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val radius = (this.size.minDimension / 2) - 8.dp.toPx()

            // Outer soft glow halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryAuraColor.copy(alpha = if (state == NovaState.SPEAKING) 0.35f else 0.2f),
                        secondaryAuraColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.25f
                ),
                radius = radius * 1.25f,
                center = center
            )

            // Dynamic segmented holographic ring
            val sweep = when (state) {
                NovaState.THINKING -> 90f
                NovaState.LISTENING -> 120f
                NovaState.SPEAKING -> 160f
                else -> 70f
            }

            drawArc(
                color = primaryAuraColor,
                startAngle = ringRotation,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = if (compact) 2.5.dp.toPx() else 4.dp.toPx(), cap = StrokeCap.Round)
            )

            drawArc(
                color = secondaryAuraColor,
                startAngle = ringRotation + 180f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = if (compact) 2.dp.toPx() else 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Extra orbital sparks for listening or speaking
            if (state == NovaState.SPEAKING || state == NovaState.LISTENING) {
                drawCircle(
                    color = primaryAuraColor,
                    radius = 3.dp.toPx(),
                    center = Offset(
                        center.x + (radius * kotlin.math.cos(Math.toRadians(ringRotation.toDouble()))).toFloat(),
                        center.y + (radius * kotlin.math.sin(Math.toRadians(ringRotation.toDouble()))).toFloat()
                    )
                )
                drawCircle(
                    color = secondaryAuraColor,
                    radius = 3.dp.toPx(),
                    center = Offset(
                        center.x + (radius * kotlin.math.cos(Math.toRadians((ringRotation + 180).toDouble()))).toFloat(),
                        center.y + (radius * kotlin.math.sin(Math.toRadians((ringRotation + 180).toDouble()))).toFloat()
                    )
                )
            }
        }

        // Avatar Image with Floating motion and cyber frame
        val innerSize = if (compact) size * 0.8f else size * 0.82f
        Box(
            modifier = Modifier
                .size(innerSize)
                .offset(y = floatOffset.dp)
                .shadow(elevation = 16.dp, shape = CircleShape, spotColor = primaryAuraColor)
                .border(
                    width = if (compact) 2.dp else 3.5.dp,
                    brush = Brush.sweepGradient(listOf(primaryAuraColor, secondaryAuraColor, primaryAuraColor)),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            val drawableRes = if (compact) R.drawable.nova_3d_girl_avatar else R.drawable.nova_3d_girl_full
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = "Nova 3D AI Companion Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // State indicator badge (if not compact)
        if (!compact) {
            val statusText = when (state) {
                NovaState.IDLE -> "NOVA ONLINE"
                NovaState.LISTENING -> "LISTENING..."
                NovaState.THINKING -> "PROCESSING..."
                NovaState.SPEAKING -> "SPEAKING..."
                NovaState.ERROR -> "ATTENTION"
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCC070A10))
                    .border(1.dp, primaryAuraColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusText,
                    color = primaryAuraColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
