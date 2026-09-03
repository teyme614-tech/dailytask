package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TealPrimary
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

private val CONFETTI_COLORS = listOf(
    Color(0xFFFFD700), // Gold
    Color(0xFFFF6B6B), // Coral Red
    Color(0xFF0D9488), // Teal Primary
    Color(0xFF38BDF8), // Sky Blue
    Color(0xFFF43F5E), // Rose Pink
    Color(0xFFA855F7), // Purple
    Color(0xFF10B981), // Emerald Green
    Color(0xFFFB923C)  // Orange
)

private data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    var flipProgress: Float,
    var flipSpeed: Float,
    val size: Float,
    val color: Color,
    val shapeType: Int // 0: rectangle, 1: circle, 2: star
)

@Composable
fun ConfettiCelebration(
    isActive: Boolean,
    aiPhrase: String?,
    onCelebrationFinished: () -> Unit,
    modifier: Modifier = Modifier,
    onReplayVoice: (() -> Unit)? = null
) {
    if (!isActive) return

    var animationProgress by remember { mutableFloatStateOf(0f) }
    var showBanner by remember { mutableStateOf(false) }

    val durationMs = 3800L

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("confetti_celebration_overlay")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Generate confetti particles on launch
        val particles = remember(isActive) {
            val list = mutableListOf<ConfettiParticle>()
            val rand = Random()
            val startX = widthPx / 2f
            val startY = heightPx * 0.45f

            for (i in 0 until 85) {
                val angle = (rand.nextFloat() * 140f + 200f) * (Math.PI.toFloat() / 180f)
                val speed = rand.nextFloat() * 1100f + 600f
                val vx = cos(angle) * speed
                val vy = sin(angle) * speed // Upward negative velocity

                list.add(
                    ConfettiParticle(
                        x = startX + (rand.nextFloat() - 0.5f) * 120f,
                        y = startY + (rand.nextFloat() - 0.5f) * 80f,
                        vx = vx,
                        vy = vy,
                        rotation = rand.nextFloat() * 360f,
                        rotationSpeed = (rand.nextFloat() - 0.5f) * 360f,
                        flipProgress = rand.nextFloat() * 6.28f,
                        flipSpeed = rand.nextFloat() * 6f + 2f,
                        size = rand.nextFloat() * 12f + 14f,
                        color = CONFETTI_COLORS[rand.nextInt(CONFETTI_COLORS.size)],
                        shapeType = rand.nextInt(3)
                    )
                )
            }
            list
        }

        // Animation frame loop
        LaunchedEffect(isActive) {
            showBanner = true
            val startTime = withFrameMillis { it }
            var lastTime = startTime

            while (true) {
                val currentTime = withFrameMillis { it }
                val elapsed = currentTime - startTime
                val dt = (currentTime - lastTime) / 1000f
                lastTime = currentTime

                animationProgress = (elapsed / durationMs.toFloat()).coerceIn(0f, 1f)

                // Update particle physics
                val gravity = 1500f // px/s²
                val drag = 0.985f

                particles.forEach { p ->
                    p.vy += gravity * dt
                    p.vx *= drag
                    p.vy *= drag
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.rotation += p.rotationSpeed * dt
                    p.flipProgress += p.flipSpeed * dt
                }

                if (elapsed >= durationMs) {
                    showBanner = false
                    onCelebrationFinished()
                    break
                }
            }
        }

        // Confetti Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val alpha = (1f - animationProgress).coerceIn(0f, 1f)

            particles.forEach { p ->
                val particleAlpha = (alpha * 0.95f).coerceIn(0f, 1f)
                val drawColor = p.color.copy(alpha = particleAlpha)

                // 3D flipping scale
                val flipScale = kotlin.math.abs(cos(p.flipProgress))
                val particleWidth = p.size * flipScale
                val particleHeight = p.size

                rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                    when (p.shapeType) {
                        0 -> {
                            // Rectangle ribbon
                            drawRoundRect(
                                color = drawColor,
                                topLeft = Offset(p.x - particleWidth / 2f, p.y - particleHeight / 2f),
                                size = Size(particleWidth, particleHeight * 0.7f),
                                cornerRadius = CornerRadius(2f, 2f)
                            )
                        }
                        1 -> {
                            // Circle / Oval
                            drawOval(
                                color = drawColor,
                                topLeft = Offset(p.x - particleWidth / 2f, p.y - particleHeight / 2f),
                                size = Size(particleWidth, particleHeight)
                            )
                        }
                        else -> {
                            // Star
                            val starPath = createStarPath(p.x, p.y, p.size * 0.6f * flipScale, p.size * 0.3f)
                            drawPath(path = starPath, color = drawColor)
                        }
                    }
                }
            }
        }

        // Encouraging Gulf Male AI Voice Banner Popup
        AnimatedVisibility(
            visible = showBanner && !aiPhrase.isNullOrBlank(),
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_encouragement_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    TealPrimary.copy(alpha = 0.14f),
                                    Color(0xFFFFD700).copy(alpha = 0.16f)
                                )
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(TealPrimary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "تشجيع خليجي كفو 👏",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFEAB308),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = aiPhrase ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        )
                    }

                    if (onReplayVoice != null) {
                        IconButton(
                            onClick = onReplayVoice,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(TealPrimary.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "إعادة نطق العبارة",
                                tint = TealPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun createStarPath(cx: Float, cy: Float, outerRadius: Float, innerRadius: Float): Path {
    val path = Path()
    val points = 5
    var angle = -Math.PI / 2.0
    val step = Math.PI / points

    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = (cx + cos(angle) * r).toFloat()
        val y = (cy + sin(angle) * r).toFloat()
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
        angle += step
    }
    path.close()
    return path
}
