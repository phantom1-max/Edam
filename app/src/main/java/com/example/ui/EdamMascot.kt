package com.example.ui

import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class EdamExpression(val label: String) {
    IDLE("Calm & Ready"),
    HAPPY("Happy & Friendly"),
    THINKING("Thinking & Preparing"),
    WORKING("Focused & Crafting"),
    SUCCESS("Celebrating Progress"),
    ENCOURAGEMENT("Supportive & Encouraging"),
    CURIOUS("Curious & Exploring"),
    SLEEP("Resting Calmly")
}

enum class EdamCompanionCharacter(
    val id: String,
    val displayName: String,
    val roleTitle: String,
    val bio: String,
    val hoodPrimaryHex: Long,
    val hoodSecondaryHex: Long,
    val sproutPrimaryHex: Long,
    val sproutSecondaryHex: Long,
    val visorGlowHex: Long
) {
    EDAM(
        id = "edam",
        displayName = "Edam",
        roleTitle = "Personal Learning Companion",
        bio = "Friendly, curious, and supportive — guides your daily learning one step at a time.",
        hoodPrimaryHex = 0xFF1F2937,
        hoodSecondaryHex = 0xFF111827,
        sproutPrimaryHex = 0xFFF59E0B,
        sproutSecondaryHex = 0xFFFBBF24,
        visorGlowHex = 0xFFFEF3C7
    ),
    KORA(
        id = "kora",
        displayName = "Kora",
        roleTitle = "Market & Quant Strategist",
        bio = "Analyzes equities, options, bonds, and macro candles with calm precision.",
        hoodPrimaryHex = 0xFF134E4A,
        hoodSecondaryHex = 0xFF0F2926,
        sproutPrimaryHex = 0xFF10B981,
        sproutSecondaryHex = 0xFF34D399,
        visorGlowHex = 0xFFD1FAE5
    ),
    VEX(
        id = "vex",
        displayName = "Vex",
        roleTitle = "Grandmaster Chess Tactician",
        bio = "Trains your calculation from novice fundamentals to Grandmaster sacrifices.",
        hoodPrimaryHex = 0xFF1E1B4B,
        hoodSecondaryHex = 0xFF0F0D29,
        sproutPrimaryHex = 0xFFFBBF24,
        sproutSecondaryHex = 0xFFFDE047,
        visorGlowHex = 0xFFFEF9C3
    ),
    NOVA(
        id = "nova",
        displayName = "Nova",
        roleTitle = "Streak & Speed Drill Coach",
        bio = "Keeps your daily habit alive with rapid flashcards and league challenges.",
        hoodPrimaryHex = 0xFF3B1D38,
        hoodSecondaryHex = 0xFF1F0F1D,
        sproutPrimaryHex = 0xFFFB923C,
        sproutSecondaryHex = 0xFFFDBA74,
        visorGlowHex = 0xFFFFEDD5
    )
}

@Composable
fun rememberReducedMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            scale == 0f
        } catch (_: Exception) {
            false
        }
    }
}

/**
 * Reusable, lightweight animated Edam mascot & companion character composable.
 * Recreates the official Edam character sheet design (dark charcoal hood, warm ivory face/body,
 * deep visor with glowing expressive eyes, amber-orange two-leaf sprout, backpack straps, and
 * optional study tablet) with smooth 60fps Compose Canvas animations and reduced-motion support.
 */
@Composable
fun EdamMascot(
    expression: EdamExpression = EdamExpression.IDLE,
    character: EdamCompanionCharacter = EdamCompanionCharacter.EDAM,
    size: Dp = 88.dp,
    showTablet: Boolean = (expression == EdamExpression.WORKING || expression == EdamExpression.IDLE),
    contentDesc: String = "${character.displayName} mascot (${expression.label})",
    modifier: Modifier = Modifier
) {
    val reducedMotion = rememberReducedMotionEnabled()
    val transition = rememberInfiniteTransition(label = "edam_mascot_transition")

    val breathPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_phase"
    )

    val eyeScanPhase by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eye_scan_phase"
    )

    val blinkTicker by transition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blink_ticker"
    )

    val activeBreath = if (reducedMotion) 0.5f else breathPhase
    val activeScan = if (reducedMotion) 0f else eyeScanPhase
    val isBlinking = !reducedMotion && (blinkTicker in 92f..97f) &&
        expression != EdamExpression.SLEEP &&
        expression != EdamExpression.SUCCESS

    val hoodPrimary = Color(character.hoodPrimaryHex)
    val hoodSecondary = Color(character.hoodSecondaryHex)
    val sproutPrimary = Color(character.sproutPrimaryHex)
    val sproutSecondary = Color(character.sproutSecondaryHex)
    val eyeGlowColor = Color(character.visorGlowHex)
    val ivoryFace = Color(0xFFEDE9E4)
    val ivoryShadow = Color(0xFFDCD6CD)
    val visorDark = Color(0xFF111827)

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = contentDesc }
            .testTag("edam_mascot_${character.id}_${expression.name.lowercase()}")
    ) {
        val w = this.size.width
        val h = this.size.height
        val floatOffsetY = (activeBreath - 0.5f) * (h * 0.035f)
        val sproutAngle = (activeBreath - 0.5f) * 8f

        // 1. Soft ground shadow
        drawOval(
            color = Color.Black.copy(alpha = 0.16f),
            topLeft = Offset(w * 0.22f, h * 0.89f),
            size = Size(w * 0.56f, h * 0.07f)
        )

        // 2. Subtle ambient aura for SUCCESS / WORKING / ENCOURAGEMENT
        if (expression == EdamExpression.SUCCESS || expression == EdamExpression.WORKING) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        sproutPrimary.copy(alpha = 0.22f * activeBreath + 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.48f),
                    radius = w * 0.48f
                ),
                center = Offset(w * 0.5f, h * 0.48f),
                radius = w * 0.48f
            )
        }

        withTransform({
            translate(left = 0f, top = floatOffsetY)
        }) {
            // 3. Cute Warm Ivory Feet
            drawRoundRect(
                color = ivoryShadow,
                topLeft = Offset(w * 0.31f, h * 0.78f),
                size = Size(w * 0.15f, h * 0.13f),
                cornerRadius = CornerRadius(w * 0.07f, w * 0.07f)
            )
            drawRoundRect(
                color = ivoryFace,
                topLeft = Offset(w * 0.54f, h * 0.78f),
                size = Size(w * 0.15f, h * 0.13f),
                cornerRadius = CornerRadius(w * 0.07f, w * 0.07f)
            )

            // 4. Backpack side peek (warm espresso leather)
            drawRoundRect(
                color = Color(0xFF3E2F26),
                topLeft = Offset(w * 0.16f, h * 0.52f),
                size = Size(w * 0.14f, h * 0.24f),
                cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
            )
            drawRoundRect(
                color = sproutPrimary,
                topLeft = Offset(w * 0.18f, h * 0.61f),
                size = Size(w * 0.035f, h * 0.05f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // 5. Hoodie Torso
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(hoodPrimary, hoodSecondary),
                    startY = h * 0.54f,
                    endY = h * 0.84f
                ),
                topLeft = Offset(w * 0.24f, h * 0.55f),
                size = Size(w * 0.52f, h * 0.27f),
                cornerRadius = CornerRadius(w * 0.16f, w * 0.16f)
            )

            // 6. Signature Two-Leaf Amber Sprout on top of the Hood
            rotate(degrees = sproutAngle, pivot = Offset(w * 0.50f, h * 0.19f)) {
                // Left leaf (larger warm amber leaf)
                val leftLeaf = Path().apply {
                    moveTo(w * 0.50f, h * 0.19f)
                    cubicTo(
                        w * 0.36f, h * 0.17f,
                        w * 0.33f, h * 0.05f,
                        w * 0.42f, h * 0.05f
                    )
                    cubicTo(
                        w * 0.49f, h * 0.05f,
                        w * 0.51f, h * 0.13f,
                        w * 0.50f, h * 0.19f
                    )
                    close()
                }
                drawPath(
                    path = leftLeaf,
                    brush = Brush.linearGradient(
                        colors = listOf(sproutSecondary, sproutPrimary),
                        start = Offset(w * 0.35f, h * 0.05f),
                        end = Offset(w * 0.50f, h * 0.19f)
                    )
                )

                // Right leaf (slightly smaller angled amber leaf)
                val rightLeaf = Path().apply {
                    moveTo(w * 0.50f, h * 0.19f)
                    cubicTo(
                        w * 0.51f, h * 0.11f,
                        w * 0.58f, h * 0.06f,
                        w * 0.63f, h * 0.09f
                    )
                    cubicTo(
                        w * 0.66f, h * 0.13f,
                        w * 0.59f, h * 0.18f,
                        w * 0.50f, h * 0.19f
                    )
                    close()
                }
                drawPath(
                    path = rightLeaf,
                    brush = Brush.linearGradient(
                        colors = listOf(sproutSecondary, sproutPrimary),
                        start = Offset(w * 0.63f, h * 0.07f),
                        end = Offset(w * 0.50f, h * 0.19f)
                    )
                )
            }

            // 7. Outer Rounded Hood (#1F2937)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(hoodPrimary, hoodSecondary),
                    startY = h * 0.16f,
                    endY = h * 0.64f
                ),
                topLeft = Offset(w * 0.14f, h * 0.17f),
                size = Size(w * 0.72f, h * 0.46f),
                cornerRadius = CornerRadius(w * 0.28f, w * 0.28f)
            )

            // Subtle hood rim highlight
            drawRoundRect(
                color = Color.White.copy(alpha = 0.10f),
                topLeft = Offset(w * 0.16f, h * 0.185f),
                size = Size(w * 0.68f, h * 0.43f),
                cornerRadius = CornerRadius(w * 0.26f, w * 0.26f),
                style = Stroke(width = w * 0.015f)
            )

            // 8. Warm Ivory Inner Face Frame (#EDE9E4)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(ivoryFace, ivoryShadow),
                    startY = h * 0.23f,
                    endY = h * 0.59f
                ),
                topLeft = Offset(w * 0.20f, h * 0.23f),
                size = Size(w * 0.60f, h * 0.36f),
                cornerRadius = CornerRadius(w * 0.21f, w * 0.21f)
            )

            // 9. Deep Charcoal Visor Screen (#111827) inside the Ivory Frame
            if (expression != EdamExpression.SLEEP) {
                drawRoundRect(
                    color = visorDark,
                    topLeft = Offset(w * 0.25f, h * 0.275f),
                    size = Size(w * 0.50f, h * 0.265f),
                    cornerRadius = CornerRadius(w * 0.16f, w * 0.16f)
                )
            }

            // 10. Expressive Glowing Eyes based on EdamExpression
            drawEdamEyes(
                expression = expression,
                isBlinking = isBlinking,
                scanOffset = activeScan * (w * 0.028f),
                eyeColor = eyeGlowColor,
                visorDark = visorDark,
                sproutPrimary = sproutPrimary,
                w = w,
                h = h
            )

            // 11. Arms / Hands / Study Tablet based on state
            when {
                expression == EdamExpression.SUCCESS -> {
                    // Raised celebratory arms on both sides
                    drawRoundRect(
                        color = hoodPrimary,
                        topLeft = Offset(w * 0.10f, h * 0.46f),
                        size = Size(w * 0.13f, h * 0.20f),
                        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
                    )
                    drawCircle(
                        color = ivoryFace,
                        radius = w * 0.055f,
                        center = Offset(w * 0.165f, h * 0.45f)
                    )
                    drawRoundRect(
                        color = hoodPrimary,
                        topLeft = Offset(w * 0.77f, h * 0.46f),
                        size = Size(w * 0.13f, h * 0.20f),
                        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
                    )
                    drawCircle(
                        color = ivoryFace,
                        radius = w * 0.055f,
                        center = Offset(w * 0.835f, h * 0.45f)
                    )
                }

                expression == EdamExpression.THINKING -> {
                    // Left arm relaxed, right hand thoughtfully on chin
                    drawCircle(
                        color = ivoryFace,
                        radius = w * 0.058f,
                        center = Offset(w * 0.58f, h * 0.58f)
                    )
                }

                showTablet -> {
                    // Holding the dark Edam Tablet with the golden two-leaf logo
                    rotate(degrees = -5f, pivot = Offset(w * 0.54f, h * 0.69f)) {
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(w * 0.36f, h * 0.57f),
                            size = Size(w * 0.36f, h * 0.24f),
                            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.18f),
                            topLeft = Offset(w * 0.36f, h * 0.57f),
                            size = Size(w * 0.36f, h * 0.24f),
                            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
                            style = Stroke(width = w * 0.012f)
                        )
                        // Mini amber sprout emblem on tablet back
                        drawCircle(
                            color = sproutPrimary,
                            radius = w * 0.028f,
                            center = Offset(w * 0.54f, h * 0.69f)
                        )
                    }
                    // Warm ivory hands holding the tablet sides
                    drawCircle(
                        color = ivoryFace,
                        radius = w * 0.052f,
                        center = Offset(w * 0.35f, h * 0.68f)
                    )
                    drawCircle(
                        color = ivoryFace,
                        radius = w * 0.052f,
                        center = Offset(w * 0.71f, h * 0.68f)
                    )
                }

                else -> {
                    // Cozy resting ivory hands
                    drawCircle(
                        color = ivoryFace,
                        radius = w * 0.05f,
                        center = Offset(w * 0.29f, h * 0.69f)
                    )
                    drawCircle(
                        color = ivoryFace,
                        radius = w * 0.05f,
                        center = Offset(w * 0.71f, h * 0.69f)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawEdamEyes(
    expression: EdamExpression,
    isBlinking: Boolean,
    scanOffset: Float,
    eyeColor: Color,
    visorDark: Color,
    sproutPrimary: Color,
    w: Float,
    h: Float
) {
    val leftEyeX = w * 0.39f + scanOffset
    val rightEyeX = w * 0.61f + scanOffset
    val eyeY = h * 0.405f
    val strokeW = w * 0.036f

    if (expression == EdamExpression.SLEEP) {
        // Calm sleeping closed eyes directly on the warm ivory face (like "Relaxed" on reference sheet)
        drawArc(
            color = visorDark,
            startAngle = 15f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(w * 0.32f, h * 0.36f),
            size = Size(w * 0.13f, h * 0.08f),
            style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round)
        )
        drawArc(
            color = visorDark,
            startAngle = 15f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(w * 0.55f, h * 0.36f),
            size = Size(w * 0.13f, h * 0.08f),
            style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round)
        )
        return
    }

    if (isBlinking) {
        drawLine(
            color = eyeColor,
            start = Offset(leftEyeX - w * 0.05f, eyeY),
            end = Offset(leftEyeX + w * 0.05f, eyeY),
            strokeWidth = strokeW * 0.8f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = eyeColor,
            start = Offset(rightEyeX - w * 0.05f, eyeY),
            end = Offset(rightEyeX + w * 0.05f, eyeY),
            strokeWidth = strokeW * 0.8f,
            cap = StrokeCap.Round
        )
        return
    }

    when (expression) {
        EdamExpression.IDLE,
        EdamExpression.HAPPY,
        EdamExpression.SUCCESS -> {
            // Signature happy glowing crescent arcs (^ ^)
            drawArc(
                color = eyeColor,
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(leftEyeX - w * 0.058f, eyeY - h * 0.045f),
                size = Size(w * 0.116f, h * 0.095f),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            drawArc(
                color = eyeColor,
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(rightEyeX - w * 0.058f, eyeY - h * 0.045f),
                size = Size(w * 0.116f, h * 0.095f),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }

        EdamExpression.WORKING,
        EdamExpression.CURIOUS -> {
            // Curious / focused glowing oval eyes (O O)
            drawOval(
                color = eyeColor,
                topLeft = Offset(leftEyeX - w * 0.042f, eyeY - h * 0.05f),
                size = Size(w * 0.084f, h * 0.10f),
                style = Stroke(width = strokeW * 0.9f)
            )
            drawOval(
                color = eyeColor,
                topLeft = Offset(rightEyeX - w * 0.042f, eyeY - h * 0.05f),
                size = Size(w * 0.084f, h * 0.10f),
                style = Stroke(width = strokeW * 0.9f)
            )
            // Subtle amber sparkle on top-left when curious
            if (expression == EdamExpression.CURIOUS) {
                drawCircle(
                    color = sproutPrimary,
                    radius = w * 0.025f,
                    center = Offset(w * 0.82f, h * 0.20f)
                )
            }
        }

        EdamExpression.THINKING -> {
            // Gently looking aside up-right with a golden idea star
            val shiftX = w * 0.025f
            val shiftY = -h * 0.015f
            drawArc(
                color = eyeColor,
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(leftEyeX + shiftX - w * 0.052f, eyeY + shiftY - h * 0.04f),
                size = Size(w * 0.104f, h * 0.085f),
                style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round)
            )
            drawArc(
                color = eyeColor,
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(rightEyeX + shiftX - w * 0.052f, eyeY + shiftY - h * 0.04f),
                size = Size(w * 0.104f, h * 0.085f),
                style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round)
            )
            // 4-point thinking sparkle above left hood
            drawCircle(
                color = sproutPrimary,
                radius = w * 0.03f,
                center = Offset(w * 0.22f, h * 0.16f)
            )
        }

        EdamExpression.ENCOURAGEMENT -> {
            // Friendly wink / supportive expression (^ -)
            drawArc(
                color = eyeColor,
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(leftEyeX - w * 0.055f, eyeY - h * 0.042f),
                size = Size(w * 0.11f, h * 0.09f),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            drawArc(
                color = eyeColor,
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(rightEyeX - w * 0.05f, eyeY - h * 0.02f),
                size = Size(w * 0.10f, h * 0.06f),
                style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round)
            )
        }

        EdamExpression.SLEEP -> Unit
    }
}

/**
 * Reusable Companion Callout Banner that pairs an animated Edam (or companion character)
 * with contextual guidance, tips, loading feedback, or celebration.
 */
@Composable
fun EdamMascotCalloutCard(
    title: String,
    message: String,
    expression: EdamExpression = EdamExpression.HAPPY,
    character: EdamCompanionCharacter = EdamCompanionCharacter.EDAM,
    badgeText: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color(character.sproutPrimaryHex).copy(alpha = 0.45f)),
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(character.sproutPrimaryHex).copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                EdamMascot(
                    expression = expression,
                    character = character,
                    size = 68.dp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color(character.sproutPrimaryHex).copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = Color(character.sproutPrimaryHex),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Interactive Horizontal Showcase of Edam & Companion Characters (Edam, Kora, Vex, Nova).
 */
@Composable
fun EdamCompanionCastStrip(
    selectedCharacter: EdamCompanionCharacter,
    onSelectCharacter: (EdamCompanionCharacter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("edam_companion_cast_strip"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Edam Learning Companions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Tap to switch guide",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EdamCompanionCharacter.entries.forEach { companion ->
                val isSelected = companion == selectedCharacter
                Surface(
                    onClick = { onSelectCharacter(companion) },
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) {
                            Color(companion.sproutPrimaryHex)
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("companion_card_${companion.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        EdamMascot(
                            expression = if (isSelected) EdamExpression.HAPPY else EdamExpression.IDLE,
                            character = companion,
                            size = 52.dp,
                            showTablet = isSelected
                        )
                        Text(
                            text = companion.displayName,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = companion.roleTitle.substringBefore(" "),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EdamCompanionRosterCard(
    selectedCharacter: EdamCompanionCharacter,
    onSelectCharacter: (EdamCompanionCharacter) -> Unit,
    modifier: Modifier = Modifier
) {
    EdamCompanionCastStrip(
        selectedCharacter = selectedCharacter,
        onSelectCharacter = onSelectCharacter,
        modifier = modifier
    )
}

@Composable
fun EdamMascotSpeechBanner(
    title: String,
    message: String,
    expression: EdamExpression = EdamExpression.HAPPY,
    character: EdamCompanionCharacter = EdamCompanionCharacter.EDAM,
    badgeText: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EdamMascotCalloutCard(
        title = title,
        message = message,
        expression = expression,
        character = character,
        badgeText = badgeText,
        onClick = onClick,
        modifier = modifier
    )
}

