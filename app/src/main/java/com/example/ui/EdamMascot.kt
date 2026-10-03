package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalEdamThemeSpec
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Global/Window cursor position in root coordinates.
 * Used on PC (Windows, macOS, Linux) and Android/ChromeOS pointer environments so that
 * companions in [EdamExpression.IDLE] ("doing nothing in any animation") follow the cursor with their eyes.
 */
val LocalCursorPosition = compositionLocalOf<Offset?> { null }

/**
 * Emotional and activity states for the Edam mascot & companion cast.
 * Note: [IDLE] keeps the companion completely still (zero body/limb animation) while its eyes
 * track the cursor position from [LocalCursorPosition].
 */
enum class EdamExpression {
    IDLE,
    HAPPY,
    CURIOUS,
    WORKING,
    THINKING,
    CELEBRATING,
    SUCCESS,
    ENCOURAGEMENT,
    SLEEPING,
    SLEEP
}

/**
 * Distinct companion characters in the Edam learning universe.
 * All companions share the sleek, iconic spherical orb aesthetic of the hero character [EDAM],
 * elevated with thematic color palettes and elegant crests:
 * - [EDAM]: Hero character — Golden Dutch Cheese Wheel with Botanical Sprout & Dimples.
 * - [KORA]: Market Alpha Companion — Radiant Ruby Flame Orb with Golden Momentum Crest.
 * - [VEX]: Grandmaster Strategy Companion — Royal Amethyst Orb with Celestial Crown Crest.
 * - [NOVA]: Cyber Pulse Companion — Brilliant Cyan Orb with Twin Orbital Halo Crest.
 */
enum class EdamCompanionCharacter(
    val id: String,
    val displayName: String,
    val roleTitle: String,
    val bio: String,
    val speciesBadge: String,
    val rindPrimaryHex: Long,
    val rindSecondaryHex: Long,
    val rindStrokeHex: Long,
    val innerCreamHex: Long,
    val sproutPrimaryHex: Long,
    val sproutSecondaryHex: Long,
    val blushHex: Long
) {
    EDAM(
        id = "edam",
        displayName = "Edam",
        roleTitle = "AI Study Sprout (Main Mascot)",
        bio = "Your cheerful botanical cheese-wheel scholar who synthesizes curricula, flashcards, and daily streaks.",
        speciesBadge = "🌱 Sprout Wheel",
        rindPrimaryHex = 0xFFF59E0B,
        rindSecondaryHex = 0xFFD97706,
        rindStrokeHex = 0xFFB45309,
        innerCreamHex = 0xFFFEF3C7,
        sproutPrimaryHex = 0xFF10B981,
        sproutSecondaryHex = 0xFF34D399,
        blushHex = 0xFFF87171
    ),
    KORA(
        id = "kora",
        displayName = "RoboBroker",
        roleTitle = "Stock Market Business Robot",
        bio = "Your executive Wall Street businessman robot with red necktie, golden ticker crest, and candlestick analysis.",
        speciesBadge = "🤖 Robot Executive",
        rindPrimaryHex = 0xFF475569,
        rindSecondaryHex = 0xFF334155,
        rindStrokeHex = 0xFF1E293B,
        innerCreamHex = 0xFFF8FAFC,
        sproutPrimaryHex = 0xFFF59E0B,
        sproutSecondaryHex = 0xFFFBBF24,
        blushHex = 0xFFDC2626
    ),
    VEX(
        id = "vex",
        displayName = "Vex",
        roleTitle = "Grandmaster Strategy Companion",
        bio = "Your dignified royal amethyst companion who calculates multi-step tactics, chess milestones, and deep learning patterns.",
        speciesBadge = "👑 Crown Orb",
        rindPrimaryHex = 0xFF7C3AED,
        rindSecondaryHex = 0xFF6D28D9,
        rindStrokeHex = 0xFF5B21B6,
        innerCreamHex = 0xFFF5F3FF,
        sproutPrimaryHex = 0xFFFBBF24,
        sproutSecondaryHex = 0xFFFDE68A,
        blushHex = 0xFFC084FC
    ),
    NOVA(
        id = "nova",
        displayName = "Bit Virus",
        roleTitle = "Coding & Cyber Bit Mascot",
        bio = "Your mischievous computer virus bit with pixel nodes, glowing matrix terminal eyes, and syntax algorithms.",
        speciesBadge = "👾 Virus Bit",
        rindPrimaryHex = 0xFF059669,
        rindSecondaryHex = 0xFF047857,
        rindStrokeHex = 0xFF064E3B,
        innerCreamHex = 0xFFECFDF5,
        sproutPrimaryHex = 0xFF10B981,
        sproutSecondaryHex = 0xFF34D399,
        blushHex = 0xFF10B981
    );

    companion object {
        val ROBO_BROKER: EdamCompanionCharacter get() = KORA
        val BIT_VIRUS: EdamCompanionCharacter get() = NOVA
    }
}

/**
 * Animated vector mascot composable for Edam & Companions.
 * Features:
 * 1. When doing nothing ([EdamExpression.IDLE]), limbs and body freeze completely still,
 *    and eyes dynamically track the cursor pointer across Windows, macOS, Linux, and Android/ChromeOS.
 * 2. When animating ([HAPPY], [WORKING], [CELEBRATING], etc.), companion plays fluid physics animations.
 * 3. Interactive tap reactions.
 */
@Composable
fun EdamMascot(
    modifier: Modifier = Modifier,
    expression: EdamExpression = EdamExpression.HAPPY,
    character: EdamCompanionCharacter = EdamCompanionCharacter.EDAM,
    size: Dp = 120.dp,
    showTablet: Boolean = false,
    interactiveOnClick: Boolean = true,
    onClickReaction: (() -> Unit)? = null
) {
    var tapCounter by remember { mutableIntStateOf(0) }
    var mascotCenterInRoot by remember { mutableStateOf(Offset.Zero) }

    val activeExpression = remember(expression, tapCounter) {
        if (tapCounter > 0) {
            when (tapCounter % 4) {
                1 -> EdamExpression.CELEBRATING
                2 -> EdamExpression.SUCCESS
                3 -> EdamExpression.CURIOUS
                else -> expression
            }
        } else {
            expression
        }
    }

    val isCompletelyIdle = activeExpression == EdamExpression.IDLE

    // Transitions: only running when NOT completely idle
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_anim")

    val breatheScale by infiniteTransition.animateFloat(
        initialValue = if (isCompletelyIdle) 1.0f else 0.985f,
        targetValue = if (isCompletelyIdle) 1.0f else 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isCompletelyIdle) 0f else when (activeExpression) {
            EdamExpression.CELEBRATING, EdamExpression.SUCCESS -> -10f
            EdamExpression.HAPPY -> -5f
            else -> -2.5f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (activeExpression == EdamExpression.CELEBRATING) 340 else 820,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val bodyTilt by infiniteTransition.animateFloat(
        initialValue = if (isCompletelyIdle) 0f else -1.5f,
        targetValue = if (isCompletelyIdle) 0f else 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bodyTilt"
    )

    val armWave by infiniteTransition.animateFloat(
        initialValue = if (isCompletelyIdle) 0f else -12f,
        targetValue = if (isCompletelyIdle) 0f else 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (activeExpression == EdamExpression.CELEBRATING) 260 else 680,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "armWave"
    )

    val sparklePulse by infiniteTransition.animateFloat(
        initialValue = if (isCompletelyIdle) 0.5f else 0.35f,
        targetValue = if (isCompletelyIdle) 0.5f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparklePulse"
    )

    val eyeScaleY by animateFloatAsState(
        targetValue = when (activeExpression) {
            EdamExpression.HAPPY, EdamExpression.CELEBRATING, EdamExpression.SUCCESS -> 0.18f
            EdamExpression.SLEEPING, EdamExpression.SLEEP -> 0.05f
            EdamExpression.CURIOUS -> 1.15f
            else -> 1.0f
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "eyeScaleY"
    )

    // Cursor tracking calculation: when IDLE, companion eyes track the cursor
    val cursorRoot = LocalCursorPosition.current
    val (targetLookX, targetLookY) = remember(cursorRoot, mascotCenterInRoot, isCompletelyIdle) {
        if (isCompletelyIdle && cursorRoot != null && mascotCenterInRoot != Offset.Zero) {
            val dx = cursorRoot.x - mascotCenterInRoot.x
            val dy = cursorRoot.y - mascotCenterInRoot.y
            val angle = atan2(dy.toDouble(), dx.toDouble()).toFloat()
            val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()
            val maxShift = 10f
            val intensity = (dist / 220f).coerceIn(0.15f, 1.0f)
            val shiftX = cos(angle) * maxShift * intensity
            val shiftY = sin(angle) * (maxShift * 0.75f) * intensity
            Pair(shiftX, shiftY)
        } else {
            Pair(0f, 0f)
        }
    }

    val smoothLookX by animateFloatAsState(
        targetValue = targetLookX,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
        label = "smoothLookX"
    )
    val smoothLookY by animateFloatAsState(
        targetValue = targetLookY,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
        label = "smoothLookY"
    )

    val effectiveArmWave = if (isCompletelyIdle) 0f else armWave
    val effectiveBounceY = if (isCompletelyIdle) 0f else bounceY
    val effectiveTilt = if (isCompletelyIdle) 0f else bodyTilt
    val effectiveScale = if (isCompletelyIdle) 1.0f else breatheScale

    val characterDesc = "${character.displayName} — ${character.roleTitle} (${character.speciesBadge})"

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = characterDesc }
            .testTag("edam_mascot_${character.id}")
            .onGloballyPositioned { coordinates ->
                mascotCenterInRoot = coordinates.localToRoot(
                    Offset(coordinates.size.width / 2f, coordinates.size.height / 2f)
                )
            }
            .then(
                if (interactiveOnClick) {
                    Modifier.clickable {
                        tapCounter++
                        onClickReaction?.invoke()
                    }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val cx = canvasW / 2f
            val cy = canvasH / 2f + effectiveBounceY
            val baseR = canvasW.coerceAtMost(canvasH) * 0.35f

            // Shadow on ground
            val shadowW = baseR * 1.55f * (if (isCompletelyIdle) 1.0f else (2f - effectiveScale))
            val shadowH = baseR * 0.24f
            drawOval(
                color = Color(0xFF000000).copy(alpha = if (isCompletelyIdle) 0.16f else 0.12f),
                topLeft = Offset(cx - shadowW / 2f, canvasH * 0.88f),
                size = Size(shadowW, shadowH)
            )

            rotate(degrees = effectiveTilt, pivot = Offset(cx, cy)) {
                scale(scaleX = effectiveScale, scaleY = effectiveScale, pivot = Offset(cx, cy)) {
                    when (character) {
                        EdamCompanionCharacter.EDAM -> drawEdamCheeseSprout(
                            cx = cx,
                            cy = cy,
                            baseR = baseR,
                            expression = activeExpression,
                            isIdle = isCompletelyIdle,
                            armWave = effectiveArmWave,
                            eyeScaleY = eyeScaleY,
                            lookX = smoothLookX,
                            lookY = smoothLookY,
                            sparklePulse = sparklePulse,
                            showTablet = showTablet
                        )
                        EdamCompanionCharacter.KORA -> drawKoraFlameSprout(
                            cx = cx,
                            cy = cy,
                            baseR = baseR,
                            expression = activeExpression,
                            isIdle = isCompletelyIdle,
                            armWave = effectiveArmWave,
                            eyeScaleY = eyeScaleY,
                            lookX = smoothLookX,
                            lookY = smoothLookY,
                            sparklePulse = sparklePulse,
                            showTablet = showTablet
                        )
                        EdamCompanionCharacter.VEX -> drawVexCrystalSprout(
                            cx = cx,
                            cy = cy,
                            baseR = baseR,
                            expression = activeExpression,
                            isIdle = isCompletelyIdle,
                            armWave = effectiveArmWave,
                            eyeScaleY = eyeScaleY,
                            lookX = smoothLookX,
                            lookY = smoothLookY,
                            sparklePulse = sparklePulse,
                            showTablet = showTablet
                        )
                        EdamCompanionCharacter.NOVA -> drawNovaPulseSprout(
                            cx = cx,
                            cy = cy,
                            baseR = baseR,
                            expression = activeExpression,
                            isIdle = isCompletelyIdle,
                            armWave = effectiveArmWave,
                            eyeScaleY = eyeScaleY,
                            lookX = smoothLookX,
                            lookY = smoothLookY,
                            sparklePulse = sparklePulse,
                            showTablet = showTablet
                        )
                    }

                    // Expression Overlays (Sparkles, Question Mark, Zzz) when not idle
                    if (!isCompletelyIdle) {
                        drawExpressionParticles(
                            cx = cx,
                            cy = cy,
                            baseR = baseR,
                            expression = activeExpression,
                            character = character,
                            sparklePulse = sparklePulse
                        )
                    }
                }
            }
        }
    }
}

/**
 * 1. HERO CHARACTER — EDAM: Round Golden Dutch Cheese Wheel with Botanical Sprout & Dimples.
 * (Preserved completely identical to user's specified original design).
 */
private fun DrawScope.drawEdamCheeseSprout(
    cx: Float,
    cy: Float,
    baseR: Float,
    expression: EdamExpression,
    isIdle: Boolean,
    armWave: Float,
    eyeScaleY: Float,
    lookX: Float,
    lookY: Float,
    sparklePulse: Float,
    showTablet: Boolean
) {
    val rindPrimary = Color(0xFFF59E0B)
    val rindSecondary = Color(0xFFD97706)
    val rindStroke = Color(0xFFB45309)
    val innerCream = Color(0xFFFEF3C7)
    val sproutGreen = Color(0xFF10B981)
    val sproutLight = Color(0xFF34D399)
    val darkSlate = Color(0xFF111827)

    // Top Botanical Stem & Twin Emerald Leaves
    val stemTopY = cy - baseR * 1.30f
    drawLine(
        color = Color(0xFF047857),
        start = Offset(cx, cy - baseR * 0.90f),
        end = Offset(cx, stemTopY),
        strokeWidth = baseR * 0.13f,
        cap = StrokeCap.Round
    )
    rotate(degrees = -28f + (if (isIdle) 0f else armWave * 0.35f), pivot = Offset(cx, stemTopY)) {
        drawOval(
            color = sproutGreen,
            topLeft = Offset(cx - baseR * 0.48f, stemTopY - baseR * 0.18f),
            size = Size(baseR * 0.50f, baseR * 0.28f)
        )
        drawLine(
            color = sproutLight,
            start = Offset(cx - baseR * 0.08f, stemTopY - baseR * 0.04f),
            end = Offset(cx - baseR * 0.38f, stemTopY - baseR * 0.04f),
            strokeWidth = baseR * 0.04f,
            cap = StrokeCap.Round
        )
    }
    rotate(degrees = 26f - (if (isIdle) 0f else armWave * 0.35f), pivot = Offset(cx, stemTopY)) {
        drawOval(
            color = sproutLight,
            topLeft = Offset(cx - baseR * 0.02f, stemTopY - baseR * 0.18f),
            size = Size(baseR * 0.46f, baseR * 0.26f)
        )
    }

    // Little Feet
    val footY = cy + baseR * 0.96f
    drawRoundRect(
        color = rindSecondary,
        topLeft = Offset(cx - baseR * 0.52f, footY),
        size = Size(baseR * 0.38f, baseR * 0.24f),
        cornerRadius = CornerRadius(baseR * 0.12f, baseR * 0.12f)
    )
    drawRoundRect(
        color = rindSecondary,
        topLeft = Offset(cx + baseR * 0.14f, footY),
        size = Size(baseR * 0.38f, baseR * 0.24f),
        cornerRadius = CornerRadius(baseR * 0.12f, baseR * 0.12f)
    )

    // Left & Right Arms
    val leftWave = if (isIdle) 0f else if (expression == EdamExpression.CELEBRATING || expression == EdamExpression.SUCCESS) -38f - armWave else 14f
    val rightWave = if (isIdle) 0f else if (expression == EdamExpression.CELEBRATING || expression == EdamExpression.SUCCESS) 38f + armWave else -armWave
    rotate(degrees = leftWave, pivot = Offset(cx - baseR * 0.90f, cy + baseR * 0.05f)) {
        drawRoundRect(
            color = rindPrimary,
            topLeft = Offset(cx - baseR * 1.26f, cy - baseR * 0.05f),
            size = Size(baseR * 0.42f, baseR * 0.22f),
            cornerRadius = CornerRadius(baseR * 0.11f, baseR * 0.11f)
        )
    }
    rotate(degrees = rightWave, pivot = Offset(cx + baseR * 0.90f, cy + baseR * 0.05f)) {
        drawRoundRect(
            color = rindPrimary,
            topLeft = Offset(cx + baseR * 0.84f, cy - baseR * 0.05f),
            size = Size(baseR * 0.42f, baseR * 0.22f),
            cornerRadius = CornerRadius(baseR * 0.11f, baseR * 0.11f)
        )
    }

    // Round Golden Wax-Rind Cheese Sphere
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFBBF24), rindPrimary, rindSecondary),
            center = Offset(cx - baseR * 0.25f, cy - baseR * 0.25f),
            radius = baseR * 1.35f
        ),
        radius = baseR,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = rindStroke,
        radius = baseR,
        center = Offset(cx, cy),
        style = Stroke(width = baseR * 0.07f)
    )

    // Inner Cream Cheese Face
    drawCircle(
        color = innerCream,
        radius = baseR * 0.80f,
        center = Offset(cx, cy + baseR * 0.03f)
    )

    // Signature Edam Cheese Holes / Dimples
    drawCircle(
        color = Color(0xFFFDE68A),
        radius = baseR * 0.12f,
        center = Offset(cx - baseR * 0.56f, cy - baseR * 0.46f)
    )
    drawCircle(
        color = Color(0xFFFDE68A),
        radius = baseR * 0.08f,
        center = Offset(cx + baseR * 0.60f, cy + baseR * 0.44f)
    )
    drawCircle(
        color = Color(0xFFFDE68A),
        radius = baseR * 0.06f,
        center = Offset(cx - baseR * 0.62f, cy + baseR * 0.36f)
    )

    // Rosy Cheeks
    val eyeY = cy - baseR * 0.08f
    val leftEyeX = cx - baseR * 0.32f
    val rightEyeX = cx + baseR * 0.32f
    drawOval(
        color = Color(0xFFF87171).copy(alpha = 0.5f),
        topLeft = Offset(leftEyeX - baseR * 0.24f, eyeY + baseR * 0.16f),
        size = Size(baseR * 0.26f, baseR * 0.14f)
    )
    drawOval(
        color = Color(0xFFF87171).copy(alpha = 0.5f),
        topLeft = Offset(rightEyeX - baseR * 0.02f, eyeY + baseR * 0.16f),
        size = Size(baseR * 0.26f, baseR * 0.14f)
    )

    // Eyes with Cursor-Following Pupils
    drawCompanionEyes(
        leftEyeX = leftEyeX,
        rightEyeX = rightEyeX,
        eyeY = eyeY,
        baseR = baseR,
        expression = expression,
        eyeScaleY = eyeScaleY,
        lookX = lookX,
        lookY = lookY,
        scleraColor = Color.White,
        irisColor = Color(0xFF059669),
        pupilColor = darkSlate
    )

    // Mouth
    drawCompanionMouth(
        cx = cx,
        mouthY = cy + baseR * 0.25f,
        baseR = baseR,
        expression = expression,
        strokeColor = darkSlate
    )

    if (showTablet || expression == EdamExpression.WORKING) {
        drawMiniStudyTablet(cx, cy, baseR, sproutGreen, sparklePulse)
    }
}

/**
 * 2. STOCK MARKET COMPANION — ROBOBROKER: Sleek Wall Street Businessman Robot.
 * Elevated with metallic titanium/slate spherical chassis, crisp executive collar and red tie with gold clip,
 * golden stock ticker coin crest, and high-tech LED candlestick chart visor eyes with cursor tracking.
 */
private fun DrawScope.drawKoraFlameSprout(
    cx: Float,
    cy: Float,
    baseR: Float,
    expression: EdamExpression,
    isIdle: Boolean,
    armWave: Float,
    eyeScaleY: Float,
    lookX: Float,
    lookY: Float,
    sparklePulse: Float,
    showTablet: Boolean
) {
    val titaniumPrimary = Color(0xFF475569) // Slate Titanium
    val titaniumSecondary = Color(0xFF334155) // Deep Slate
    val titaniumStroke = Color(0xFF1E293B)
    val innerChest = Color(0xFFF8FAFC) // Executive White Face / Collar
    val goldCoin = Color(0xFFF59E0B) // Gold Ticker Accent
    val goldCoinLight = Color(0xFFFDE68A)
    val tieRed = Color(0xFFDC2626) // Executive Crimson Tie
    val darkSlate = Color(0xFF0F172A)

    // Top Robot Antenna with Golden Stock Coin / Ticker Crest
    val antennaTopY = cy - baseR * 1.32f
    drawLine(
        color = Color(0xFF64748B),
        start = Offset(cx, cy - baseR * 0.90f),
        end = Offset(cx, antennaTopY),
        strokeWidth = baseR * 0.12f,
        cap = StrokeCap.Round
    )
    // Golden Ticker Coin Crown
    drawCircle(
        color = goldCoin,
        radius = baseR * 0.22f,
        center = Offset(cx, antennaTopY - baseR * 0.10f)
    )
    drawCircle(
        color = goldCoinLight,
        radius = baseR * 0.15f,
        center = Offset(cx, antennaTopY - baseR * 0.10f)
    )
    // Currency / Bull mark line on coin
    drawLine(
        color = Color(0xFFB45309),
        start = Offset(cx, antennaTopY - baseR * 0.22f),
        end = Offset(cx, antennaTopY + baseR * 0.02f),
        strokeWidth = baseR * 0.05f,
        cap = StrokeCap.Round
    )

    // Metallic Little Feet
    val footY = cy + baseR * 0.96f
    drawRoundRect(
        color = titaniumSecondary,
        topLeft = Offset(cx - baseR * 0.52f, footY),
        size = Size(baseR * 0.38f, baseR * 0.24f),
        cornerRadius = CornerRadius(baseR * 0.12f, baseR * 0.12f)
    )
    drawRoundRect(
        color = titaniumSecondary,
        topLeft = Offset(cx + baseR * 0.14f, footY),
        size = Size(baseR * 0.38f, baseR * 0.24f),
        cornerRadius = CornerRadius(baseR * 0.12f, baseR * 0.12f)
    )

    // Metallic Arms with Gold Cuffs
    val leftWave = if (isIdle) 0f else if (expression == EdamExpression.CELEBRATING || expression == EdamExpression.SUCCESS) -38f - armWave else 14f
    val rightWave = if (isIdle) 0f else if (expression == EdamExpression.CELEBRATING || expression == EdamExpression.SUCCESS) 38f + armWave else -armWave
    rotate(degrees = leftWave, pivot = Offset(cx - baseR * 0.90f, cy + baseR * 0.05f)) {
        drawRoundRect(
            color = titaniumPrimary,
            topLeft = Offset(cx - baseR * 1.26f, cy - baseR * 0.05f),
            size = Size(baseR * 0.42f, baseR * 0.22f),
            cornerRadius = CornerRadius(baseR * 0.11f, baseR * 0.11f)
        )
        drawCircle(
            color = goldCoin,
            radius = baseR * 0.08f,
            center = Offset(cx - baseR * 1.15f, cy + baseR * 0.06f)
        )
    }
    rotate(degrees = rightWave, pivot = Offset(cx + baseR * 0.90f, cy + baseR * 0.05f)) {
        drawRoundRect(
            color = titaniumPrimary,
            topLeft = Offset(cx + baseR * 0.84f, cy - baseR * 0.05f),
            size = Size(baseR * 0.42f, baseR * 0.22f),
            cornerRadius = CornerRadius(baseR * 0.11f, baseR * 0.11f)
        )
        drawCircle(
            color = goldCoin,
            radius = baseR * 0.08f,
            center = Offset(cx + baseR * 1.15f, cy + baseR * 0.06f)
        )
    }

    // Titanium Robot Spherical Chassis
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF94A3B8), titaniumPrimary, titaniumSecondary),
            center = Offset(cx - baseR * 0.25f, cy - baseR * 0.25f),
            radius = baseR * 1.35f
        ),
        radius = baseR,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = titaniumStroke,
        radius = baseR,
        center = Offset(cx, cy),
        style = Stroke(width = baseR * 0.07f)
    )

    // Inner Crisp Executive Faceplate
    drawCircle(
        color = innerChest,
        radius = baseR * 0.78f,
        center = Offset(cx, cy + baseR * 0.02f)
    )

    // Executive Collar and Crimson Tie with Gold Tie-Clip
    val collarY = cy + baseR * 0.24f
    // White collar flaps
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cx - baseR * 0.32f, collarY - baseR * 0.06f),
        size = Size(baseR * 0.28f, baseR * 0.16f),
        cornerRadius = CornerRadius(baseR * 0.06f, baseR * 0.06f)
    )
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cx + baseR * 0.04f, collarY - baseR * 0.06f),
        size = Size(baseR * 0.28f, baseR * 0.16f),
        cornerRadius = CornerRadius(baseR * 0.06f, baseR * 0.06f)
    )
    // Red Silk Tie Knot & Body
    drawOval(
        color = Color(0xFFB91C1C),
        topLeft = Offset(cx - baseR * 0.10f, collarY - baseR * 0.04f),
        size = Size(baseR * 0.20f, baseR * 0.16f)
    )
    drawRoundRect(
        color = tieRed,
        topLeft = Offset(cx - baseR * 0.12f, collarY + baseR * 0.08f),
        size = Size(baseR * 0.24f, baseR * 0.38f),
        cornerRadius = CornerRadius(baseR * 0.06f, baseR * 0.06f)
    )
    // Gold Tie Clip
    drawLine(
        color = goldCoin,
        start = Offset(cx - baseR * 0.10f, collarY + baseR * 0.20f),
        end = Offset(cx + baseR * 0.14f, collarY + baseR * 0.20f),
        strokeWidth = baseR * 0.05f,
        cap = StrokeCap.Round
    )

    // Robot Ear Audio Scanners (Left & Right bolts)
    drawCircle(
        color = Color(0xFF64748B),
        radius = baseR * 0.13f,
        center = Offset(cx - baseR * 0.96f, cy)
    )
    drawCircle(
        color = goldCoin,
        radius = baseR * 0.06f,
        center = Offset(cx - baseR * 0.96f, cy)
    )
    drawCircle(
        color = Color(0xFF64748B),
        radius = baseR * 0.13f,
        center = Offset(cx + baseR * 0.96f, cy)
    )
    drawCircle(
        color = goldCoin,
        radius = baseR * 0.06f,
        center = Offset(cx + baseR * 0.96f, cy)
    )

    // Eyes: High-Tech Stock Candlestick Visor Eyes with Cursor Tracking
    val eyeY = cy - baseR * 0.12f
    val leftEyeX = cx - baseR * 0.32f
    val rightEyeX = cx + baseR * 0.32f
    drawCompanionEyes(
        leftEyeX = leftEyeX,
        rightEyeX = rightEyeX,
        eyeY = eyeY,
        baseR = baseR,
        expression = expression,
        eyeScaleY = eyeScaleY,
        lookX = lookX,
        lookY = lookY,
        scleraColor = Color(0xFF0F172A), // Dark cyber visor glass
        irisColor = Color(0xFF10B981), // Bullish Green Candlestick glow
        pupilColor = Color(0xFF34D399)
    )

    // Mouth / LED Bar
    drawCompanionMouth(
        cx = cx,
        mouthY = cy + baseR * 0.12f,
        baseR = baseR * 0.85f,
        expression = expression,
        strokeColor = darkSlate
    )

    if (showTablet || expression == EdamExpression.WORKING) {
        drawMiniStudyTablet(cx, cy, baseR, goldCoin, sparklePulse)
    }
}

/**
 * 3. VEX — Grandmaster Strategy Companion: Royal Amethyst Orb.
 * Stylistically identical in silhouette and charm to hero character [EDAM],
 * featuring a rich royal amethyst spherical body, soft lavender face, and an elegant three-prong golden celestial crystal crest.
 */
private fun DrawScope.drawVexCrystalSprout(
    cx: Float,
    cy: Float,
    baseR: Float,
    expression: EdamExpression,
    isIdle: Boolean,
    armWave: Float,
    eyeScaleY: Float,
    lookX: Float,
    lookY: Float,
    sparklePulse: Float,
    showTablet: Boolean
) {
    val rindPrimary = Color(0xFF7C3AED) // Royal Violet
    val rindSecondary = Color(0xFF6D28D9) // Deep Amethyst
    val rindStroke = Color(0xFF5B21B6)
    val innerCream = Color(0xFFF5F3FF) // Soft Lavender-Cream
    val crystalGold = Color(0xFFFBBF24) // Golden Crown Gem
    val crystalLight = Color(0xFFFDE68A)
    val darkSlate = Color(0xFF111827)

    // Top Golden Celestial Crystal Crown Crest (three sleek gemstone leaves)
    val stemTopY = cy - baseR * 1.30f
    drawLine(
        color = Color(0xFF4C1D95),
        start = Offset(cx, cy - baseR * 0.90f),
        end = Offset(cx, stemTopY),
        strokeWidth = baseR * 0.13f,
        cap = StrokeCap.Round
    )
    // Center crystal spire
    drawOval(
        color = crystalGold,
        topLeft = Offset(cx - baseR * 0.14f, stemTopY - baseR * 0.26f),
        size = Size(baseR * 0.28f, baseR * 0.38f)
    )
    // Left & right crown leaves
    rotate(degrees = -30f + (if (isIdle) 0f else armWave * 0.3f), pivot = Offset(cx, stemTopY)) {
        drawOval(
            color = crystalLight,
            topLeft = Offset(cx - baseR * 0.44f, stemTopY - baseR * 0.16f),
            size = Size(baseR * 0.44f, baseR * 0.24f)
        )
    }
    rotate(degrees = 30f - (if (isIdle) 0f else armWave * 0.3f), pivot = Offset(cx, stemTopY)) {
        drawOval(
            color = crystalGold,
            topLeft = Offset(cx, stemTopY - baseR * 0.16f),
            size = Size(baseR * 0.44f, baseR * 0.24f)
        )
    }

    // Little Feet
    val footY = cy + baseR * 0.96f
    drawRoundRect(
        color = rindSecondary,
        topLeft = Offset(cx - baseR * 0.52f, footY),
        size = Size(baseR * 0.38f, baseR * 0.24f),
        cornerRadius = CornerRadius(baseR * 0.12f, baseR * 0.12f)
    )
    drawRoundRect(
        color = rindSecondary,
        topLeft = Offset(cx + baseR * 0.14f, footY),
        size = Size(baseR * 0.38f, baseR * 0.24f),
        cornerRadius = CornerRadius(baseR * 0.12f, baseR * 0.12f)
    )

    // Left & Right Arms
    val leftWave = if (isIdle) 0f else if (expression == EdamExpression.CELEBRATING || expression == EdamExpression.SUCCESS) -38f - armWave else 14f
    val rightWave = if (isIdle) 0f else if (expression == EdamExpression.CELEBRATING || expression == EdamExpression.SUCCESS) 38f + armWave else -armWave
    rotate(degrees = leftWave, pivot = Offset(cx - baseR * 0.90f, cy + baseR * 0.05f)) {
        drawRoundRect(
            color = rindPrimary,
            topLeft = Offset(cx - baseR * 1.26f, cy - baseR * 0.05f),
            size = Size(baseR * 0.42f, baseR * 0.22f),
            cornerRadius = CornerRadius(baseR * 0.11f, baseR * 0.11f)
        )
    }
    rotate(degrees = rightWave, pivot = Offset(cx + baseR * 0.90f, cy + baseR * 0.05f)) {
        drawRoundRect(
            color = rindPrimary,
            topLeft = Offset(cx + baseR * 0.84f, cy - baseR * 0.05f),
            size = Size(baseR * 0.42f, baseR * 0.22f),
            cornerRadius = CornerRadius(baseR * 0.11f, baseR * 0.11f)
        )
    }

    // Round Royal Amethyst Sphere (matches Edam's smooth round silhouette)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFA78BFA), rindPrimary, rindSecondary),
            center = Offset(cx - baseR * 0.25f, cy - baseR * 0.25f),
            radius = baseR * 1.35f
        ),
        radius = baseR,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = rindStroke,
        radius = baseR,
        center = Offset(cx, cy),
        style = Stroke(width = baseR * 0.07f)
    )

    // Inner Lavender-Cream Face
    drawCircle(
        color = innerCream,
        radius = baseR * 0.80f,
        center = Offset(cx, cy + baseR * 0.03f)
    )

    // Subtle Amethyst Crystal Dimples
    drawCircle(
        color = Color(0xFFDDD6FE),
        radius = baseR * 0.10f,
        center = Offset(cx - baseR * 0.56f, cy - baseR * 0.46f)
    )
    drawCircle(
        color = Color(0xFFDDD6FE),
        radius = baseR * 0.07f,
        center = Offset(cx + baseR * 0.60f, cy + baseR * 0.44f)
    )

    // Soft Lilac Rosy Cheeks
    val eyeY = cy - baseR * 0.08f
    val leftEyeX = cx - baseR * 0.32f
    val rightEyeX = cx + baseR * 0.32f
    drawOval(
        color = Color(0xFFC084FC).copy(alpha = 0.5f),
        topLeft = Offset(leftEyeX - baseR * 0.24f, eyeY + baseR * 0.16f),
        size = Size(baseR * 0.26f, baseR * 0.14f)
    )
    drawOval(
        color = Color(0xFFC084FC).copy(alpha = 0.5f),
        topLeft = Offset(rightEyeX - baseR * 0.02f, eyeY + baseR * 0.16f),
        size = Size(baseR * 0.26f, baseR * 0.14f)
    )

    // Eyes with Cursor-Following Pupils (Deep Amber-Gold Iris)
    drawCompanionEyes(
        leftEyeX = leftEyeX,
        rightEyeX = rightEyeX,
        eyeY = eyeY,
        baseR = baseR,
        expression = expression,
        eyeScaleY = eyeScaleY,
        lookX = lookX,
        lookY = lookY,
        scleraColor = Color.White,
        irisColor = Color(0xFFD97706), // Deep Strategy Gold
        pupilColor = darkSlate
    )

    // Mouth
    drawCompanionMouth(
        cx = cx,
        mouthY = cy + baseR * 0.25f,
        baseR = baseR,
        expression = expression,
        strokeColor = darkSlate
    )

    if (showTablet || expression == EdamExpression.WORKING) {
        drawMiniStudyTablet(cx, cy, baseR, crystalGold, sparklePulse)
    }
}

/**
 * 4. CODING & CYBER COMPANION — BIT VIRUS: Digital Computer Virus Bit Mascot.
 * Featuring an electric matrix emerald green spherical capsid, 6 pulsing viral bit nodes,
 * twin binary glitch horns, and glowing terminal matrix eyes that track the cursor pointer.
 */
private fun DrawScope.drawNovaPulseSprout(
    cx: Float,
    cy: Float,
    baseR: Float,
    expression: EdamExpression,
    isIdle: Boolean,
    armWave: Float,
    eyeScaleY: Float,
    lookX: Float,
    lookY: Float,
    sparklePulse: Float,
    showTablet: Boolean
) {
    val virusPrimary = Color(0xFF059669) // Emerald Terminal Green
    val virusSecondary = Color(0xFF047857) // Deep Cyber Green
    val virusStroke = Color(0xFF064E3B)
    val innerMatrix = Color(0xFFECFDF5) // Mint Glitch Screen
    val neonGreen = Color(0xFF10B981) // Cyber Matrix Glow
    val neonBright = Color(0xFF34D399) // Neon Bit Pulse
    val darkSlate = Color(0xFF022C22)

    // Top Twin Binary Glitch Horns with Data Packet Nodes
    val stemTopY = cy - baseR * 1.30f
    drawLine(
        color = Color(0xFF047857),
        start = Offset(cx, cy - baseR * 0.90f),
        end = Offset(cx, stemTopY),
        strokeWidth = baseR * 0.12f,
        cap = StrokeCap.Round
    )
    // Left & Right binary antenna nodes
    val leftAntennaX = cx - baseR * 0.32f
    val rightAntennaX = cx + baseR * 0.32f
    val antennaY = stemTopY - baseR * 0.14f
    drawLine(
        color = neonGreen,
        start = Offset(cx, stemTopY),
        end = Offset(leftAntennaX, antennaY),
        strokeWidth = baseR * 0.08f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = neonGreen,
        start = Offset(cx, stemTopY),
        end = Offset(rightAntennaX, antennaY),
        strokeWidth = baseR * 0.08f,
        cap = StrokeCap.Round
    )
    // Pulsing Bit cubes on antenna tips
    drawRoundRect(
        color = neonBright,
        topLeft = Offset(leftAntennaX - baseR * 0.10f, antennaY - baseR * 0.10f),
        size = Size(baseR * 0.20f, baseR * 0.20f),
        cornerRadius = CornerRadius(baseR * 0.04f, baseR * 0.04f)
    )
    drawRoundRect(
        color = neonBright,
        topLeft = Offset(rightAntennaX - baseR * 0.10f, antennaY - baseR * 0.10f),
        size = Size(baseR * 0.20f, baseR * 0.20f),
        cornerRadius = CornerRadius(baseR * 0.04f, baseR * 0.04f)
    )

    // 6 Viral Capsid Bit Nodes around the sphere (pulsing digital virus nodes)
    val bitPulseOffset = if (isIdle) 0f else sparklePulse * (baseR * 0.06f)
    val bitSize = baseR * 0.18f
    listOf(
        Offset(cx - baseR * 0.96f - bitPulseOffset, cy - baseR * 0.45f),
        Offset(cx + baseR * 0.96f + bitPulseOffset, cy - baseR * 0.45f),
        Offset(cx - baseR * 1.05f - bitPulseOffset, cy + baseR * 0.15f),
        Offset(cx + baseR * 1.05f + bitPulseOffset, cy + baseR * 0.15f),
        Offset(cx - baseR * 0.70f, cy + baseR * 0.85f + bitPulseOffset),
        Offset(cx + baseR * 0.70f, cy + baseR * 0.85f + bitPulseOffset)
    ).forEach { nodePos ->
        drawRoundRect(
            color = neonGreen,
            topLeft = Offset(nodePos.x - bitSize / 2f, nodePos.y - bitSize / 2f),
            size = Size(bitSize, bitSize),
            cornerRadius = CornerRadius(baseR * 0.04f, baseR * 0.04f)
        )
        drawRoundRect(
            color = neonBright,
            topLeft = Offset(nodePos.x - bitSize * 0.25f, nodePos.y - bitSize * 0.25f),
            size = Size(bitSize * 0.5f, bitSize * 0.5f),
            cornerRadius = CornerRadius(baseR * 0.02f, baseR * 0.02f)
        )
    }

    // Little Matrix Feet
    val footY = cy + baseR * 0.96f
    drawRoundRect(
        color = virusSecondary,
        topLeft = Offset(cx - baseR * 0.52f, footY),
        size = Size(baseR * 0.38f, baseR * 0.24f),
        cornerRadius = CornerRadius(baseR * 0.06f, baseR * 0.06f)
    )
    drawRoundRect(
        color = virusSecondary,
        topLeft = Offset(cx + baseR * 0.14f, footY),
        size = Size(baseR * 0.38f, baseR * 0.24f),
        cornerRadius = CornerRadius(baseR * 0.06f, baseR * 0.06f)
    )

    // Left & Right Arms
    val leftWave = if (isIdle) 0f else if (expression == EdamExpression.CELEBRATING || expression == EdamExpression.SUCCESS) -38f - armWave else 14f
    val rightWave = if (isIdle) 0f else if (expression == EdamExpression.CELEBRATING || expression == EdamExpression.SUCCESS) 38f + armWave else -armWave
    rotate(degrees = leftWave, pivot = Offset(cx - baseR * 0.90f, cy + baseR * 0.05f)) {
        drawRoundRect(
            color = virusPrimary,
            topLeft = Offset(cx - baseR * 1.26f, cy - baseR * 0.05f),
            size = Size(baseR * 0.42f, baseR * 0.22f),
            cornerRadius = CornerRadius(baseR * 0.06f, baseR * 0.06f)
        )
    }
    rotate(degrees = rightWave, pivot = Offset(cx + baseR * 0.90f, cy + baseR * 0.05f)) {
        drawRoundRect(
            color = virusPrimary,
            topLeft = Offset(cx + baseR * 0.84f, cy - baseR * 0.05f),
            size = Size(baseR * 0.42f, baseR * 0.22f),
            cornerRadius = CornerRadius(baseR * 0.06f, baseR * 0.06f)
        )
    }

    // Spherical Virus Capsid Base
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(neonBright, virusPrimary, virusSecondary),
            center = Offset(cx - baseR * 0.25f, cy - baseR * 0.25f),
            radius = baseR * 1.35f
        ),
        radius = baseR,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = virusStroke,
        radius = baseR,
        center = Offset(cx, cy),
        style = Stroke(width = baseR * 0.07f)
    )

    // Inner Mint Glitch Face
    drawCircle(
        color = innerMatrix,
        radius = baseR * 0.80f,
        center = Offset(cx, cy + baseR * 0.03f)
    )

    // Cyber Bit Dimples (Square pixels)
    drawRoundRect(
        color = neonGreen.copy(alpha = 0.6f),
        topLeft = Offset(cx - baseR * 0.60f, cy - baseR * 0.48f),
        size = Size(baseR * 0.16f, baseR * 0.16f),
        cornerRadius = CornerRadius(baseR * 0.03f, baseR * 0.03f)
    )
    drawRoundRect(
        color = neonGreen.copy(alpha = 0.6f),
        topLeft = Offset(cx + baseR * 0.48f, cy + baseR * 0.40f),
        size = Size(baseR * 0.14f, baseR * 0.14f),
        cornerRadius = CornerRadius(baseR * 0.03f, baseR * 0.03f)
    )

    // Cheeks
    val eyeY = cy - baseR * 0.08f
    val leftEyeX = cx - baseR * 0.32f
    val rightEyeX = cx + baseR * 0.32f
    drawOval(
        color = neonGreen.copy(alpha = 0.40f),
        topLeft = Offset(leftEyeX - baseR * 0.24f, eyeY + baseR * 0.16f),
        size = Size(baseR * 0.26f, baseR * 0.14f)
    )
    drawOval(
        color = neonGreen.copy(alpha = 0.40f),
        topLeft = Offset(rightEyeX - baseR * 0.02f, eyeY + baseR * 0.16f),
        size = Size(baseR * 0.26f, baseR * 0.14f)
    )

    // Eyes: Glowing Matrix Terminal Eyes with Cursor-Tracking Pupils
    drawCompanionEyes(
        leftEyeX = leftEyeX,
        rightEyeX = rightEyeX,
        eyeY = eyeY,
        baseR = baseR,
        expression = expression,
        eyeScaleY = eyeScaleY,
        lookX = lookX,
        lookY = lookY,
        scleraColor = Color(0xFF022C22), // Terminal background
        irisColor = Color(0xFF10B981), // Neon matrix phosphor
        pupilColor = Color(0xFF6EE7B7)
    )

    // Digital Byte Smile
    drawCompanionMouth(
        cx = cx,
        mouthY = cy + baseR * 0.25f,
        baseR = baseR,
        expression = expression,
        strokeColor = darkSlate
    )

    if (showTablet || expression == EdamExpression.WORKING) {
        drawMiniStudyTablet(cx, cy, baseR, neonBright, sparklePulse)
    }
}

/**
 * Shared Eyes renderer with Cursor Following Pupils and expressions.
 */
private fun DrawScope.drawCompanionEyes(
    leftEyeX: Float,
    rightEyeX: Float,
    eyeY: Float,
    baseR: Float,
    expression: EdamExpression,
    eyeScaleY: Float,
    lookX: Float,
    lookY: Float,
    scleraColor: Color,
    irisColor: Color,
    pupilColor: Color
) {
    val socketRadius = baseR * 0.185f
    val pupilShiftX = (lookX * (baseR * 0.012f)).coerceIn(-socketRadius * 0.48f, socketRadius * 0.48f)
    val pupilShiftY = (lookY * (baseR * 0.012f)).coerceIn(-socketRadius * 0.42f, socketRadius * 0.42f)

    listOf(leftEyeX, rightEyeX).forEach { eyeX ->
        when (expression) {
            EdamExpression.HAPPY, EdamExpression.CELEBRATING, EdamExpression.SUCCESS -> {
                val arcW = socketRadius * 2.2f
                val arcH = socketRadius * 1.5f
                drawArc(
                    color = pupilColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(eyeX - arcW / 2f, eyeY - arcH * 0.35f),
                    size = Size(arcW, arcH),
                    style = Stroke(width = baseR * 0.08f, cap = StrokeCap.Round)
                )
            }
            EdamExpression.SLEEPING, EdamExpression.SLEEP -> {
                drawLine(
                    color = pupilColor,
                    start = Offset(eyeX - socketRadius * 0.9f, eyeY),
                    end = Offset(eyeX + socketRadius * 0.9f, eyeY),
                    strokeWidth = baseR * 0.07f,
                    cap = StrokeCap.Round
                )
            }
            else -> {
                // Sclera
                drawOval(
                    color = scleraColor,
                    topLeft = Offset(eyeX - socketRadius, eyeY - socketRadius * eyeScaleY),
                    size = Size(socketRadius * 2f, socketRadius * 2f * eyeScaleY)
                )
                drawOval(
                    color = pupilColor.copy(alpha = 0.2f),
                    topLeft = Offset(eyeX - socketRadius, eyeY - socketRadius * eyeScaleY),
                    size = Size(socketRadius * 2f, socketRadius * 2f * eyeScaleY),
                    style = Stroke(width = baseR * 0.03f)
                )

                // Iris (moves with cursor!)
                val irisR = baseR * 0.13f
                drawOval(
                    color = irisColor,
                    topLeft = Offset(
                        eyeX - irisR + pupilShiftX,
                        eyeY - irisR * eyeScaleY + pupilShiftY * eyeScaleY
                    ),
                    size = Size(irisR * 2f, irisR * 2f * eyeScaleY)
                )

                // Pupil (moves slightly further with cursor for depth parallax!)
                val pupilR = baseR * 0.08f
                drawOval(
                    color = pupilColor,
                    topLeft = Offset(
                        eyeX - pupilR + pupilShiftX * 1.18f,
                        eyeY - pupilR * eyeScaleY + pupilShiftY * 1.18f * eyeScaleY
                    ),
                    size = Size(pupilR * 2f, pupilR * 2f * eyeScaleY)
                )

                // Specular Highlight
                if (eyeScaleY > 0.4f) {
                    drawCircle(
                        color = Color.White,
                        radius = baseR * 0.042f,
                        center = Offset(
                            eyeX - baseR * 0.04f + pupilShiftX * 0.9f,
                            eyeY - baseR * 0.04f + pupilShiftY * 0.9f
                        )
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCompanionMouth(
    cx: Float,
    mouthY: Float,
    baseR: Float,
    expression: EdamExpression,
    strokeColor: Color
) {
    when (expression) {
        EdamExpression.CURIOUS, EdamExpression.THINKING -> {
            drawOval(
                color = strokeColor,
                topLeft = Offset(cx - baseR * 0.08f, mouthY - baseR * 0.04f),
                size = Size(baseR * 0.16f, baseR * 0.18f)
            )
        }
        EdamExpression.CELEBRATING, EdamExpression.SUCCESS -> {
            val smileW = baseR * 0.42f
            val smileH = baseR * 0.32f
            drawArc(
                color = strokeColor,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(cx - smileW / 2f, mouthY - smileH * 0.35f),
                size = Size(smileW, smileH)
            )
        }
        else -> {
            val smileW = baseR * 0.34f
            val smileH = baseR * 0.22f
            drawArc(
                color = strokeColor,
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(cx - smileW / 2f, mouthY - smileH * 0.45f),
                size = Size(smileW, smileH),
                style = Stroke(width = baseR * 0.07f, cap = StrokeCap.Round)
            )
        }
    }
}

private fun DrawScope.drawMiniStudyTablet(
    cx: Float,
    cy: Float,
    baseR: Float,
    accentColor: Color,
    sparklePulse: Float
) {
    val bookW = baseR * 0.76f
    val bookH = baseR * 0.44f
    val bookTop = cy + baseR * 0.44f
    drawRoundRect(
        color = Color(0xFF1F2937),
        topLeft = Offset(cx - bookW / 2f, bookTop),
        size = Size(bookW, bookH),
        cornerRadius = CornerRadius(baseR * 0.10f, baseR * 0.10f)
    )
    drawRoundRect(
        color = accentColor,
        topLeft = Offset(cx - bookW / 2f, bookTop),
        size = Size(bookW, bookH),
        cornerRadius = CornerRadius(baseR * 0.10f, baseR * 0.10f),
        style = Stroke(width = baseR * 0.05f)
    )
    drawLine(
        color = accentColor.copy(alpha = sparklePulse),
        start = Offset(cx - bookW * 0.28f, bookTop + bookH * 0.36f),
        end = Offset(cx + bookW * 0.28f, bookTop + bookH * 0.36f),
        strokeWidth = baseR * 0.05f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFFBBF24),
        start = Offset(cx - bookW * 0.28f, bookTop + bookH * 0.66f),
        end = Offset(cx + bookW * 0.12f, bookTop + bookH * 0.66f),
        strokeWidth = baseR * 0.05f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawExpressionParticles(
    cx: Float,
    cy: Float,
    baseR: Float,
    expression: EdamExpression,
    character: EdamCompanionCharacter,
    sparklePulse: Float
) {
    val primaryAccent = Color(character.sproutPrimaryHex)
    val goldAccent = Color(0xFFFBBF24)
    when (expression) {
        EdamExpression.CELEBRATING, EdamExpression.SUCCESS, EdamExpression.WORKING -> {
            val s1 = Offset(cx - baseR * 1.12f, cy - baseR * 0.86f)
            val s2 = Offset(cx + baseR * 1.14f, cy - baseR * 0.78f)
            val r = baseR * 0.13f * sparklePulse
            drawLine(goldAccent, Offset(s1.x - r, s1.y), Offset(s1.x + r, s1.y), strokeWidth = baseR * 0.05f, cap = StrokeCap.Round)
            drawLine(goldAccent, Offset(s1.x, s1.y - r), Offset(s1.x, s1.y + r), strokeWidth = baseR * 0.05f, cap = StrokeCap.Round)
            drawLine(primaryAccent, Offset(s2.x - r, s2.y), Offset(s2.x + r, s2.y), strokeWidth = baseR * 0.05f, cap = StrokeCap.Round)
            drawLine(primaryAccent, Offset(s2.x, s2.y - r), Offset(s2.x, s2.y + r), strokeWidth = baseR * 0.05f, cap = StrokeCap.Round)
        }
        EdamExpression.CURIOUS, EdamExpression.THINKING -> {
            drawCircle(
                color = primaryAccent.copy(alpha = sparklePulse),
                radius = baseR * 0.07f,
                center = Offset(cx + baseR * 0.96f, cy - baseR * 0.66f)
            )
            drawCircle(
                color = goldAccent.copy(alpha = sparklePulse),
                radius = baseR * 0.12f,
                center = Offset(cx + baseR * 1.15f, cy - baseR * 0.94f)
            )
        }
        else -> Unit
    }
}

/**
 * Reusable Duolingo-style Mascot Coach Callout Card.
 */
@Composable
fun EdamMascotCalloutCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    expression: EdamExpression = EdamExpression.HAPPY,
    character: EdamCompanionCharacter = EdamCompanionCharacter.EDAM,
    badgeText: String? = null,
    showTablet: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val themeSpec = LocalEdamThemeSpec.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("mascot_callout_card")
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EdamMascot(
                expression = expression,
                character = character,
                size = 64.dp,
                showTablet = showTablet
            )

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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
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
 * Mascot Speech Bubble Banner for feedback and status updates.
 */
@Composable
fun EdamMascotSpeechBanner(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    expression: EdamExpression = EdamExpression.HAPPY,
    character: EdamCompanionCharacter = EdamCompanionCharacter.EDAM,
    badgeText: String? = null,
    showTablet: Boolean = false
) {
    EdamMascotCalloutCard(
        title = title,
        message = message,
        modifier = modifier,
        expression = expression,
        character = character,
        badgeText = badgeText,
        showTablet = showTablet
    )
}

/**
 * Companion Selection Roster Card displaying all 4 refined companions.
 * Idle companions follow cursor pointer; clicking switches the active companion guide.
 */
@Composable
fun EdamCompanionRosterCard(
    selectedCharacter: EdamCompanionCharacter,
    onSelectCharacter: (EdamCompanionCharacter) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalEdamThemeSpec.current
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .fillMaxWidth()
            .testTag("companion_roster_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Learning Companion Cast",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Companions doing nothing (IDLE) follow your pointer with their eyes!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(selectedCharacter.rindPrimaryHex).copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color(selectedCharacter.rindPrimaryHex).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = selectedCharacter.speciesBadge,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(selectedCharacter.rindPrimaryHex),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EdamCompanionCharacter.entries.forEach { comp ->
                    val isSelected = comp == selectedCharacter
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        },
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(comp.rindPrimaryHex) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectCharacter(comp) }
                            .testTag("companion_chip_${comp.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            EdamMascot(
                                expression = if (isSelected) EdamExpression.HAPPY else EdamExpression.IDLE,
                                character = comp,
                                size = 52.dp,
                                interactiveOnClick = false
                            )
                            Text(
                                text = comp.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

