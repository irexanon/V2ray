package com.hollow.vpn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hollow.vpn.ui.theme.SignalMediumDark
import com.hollow.vpn.ui.theme.SignalMediumLight
import com.hollow.vpn.ui.theme.SignalStrongDark
import com.hollow.vpn.ui.theme.SignalStrongLight
import com.hollow.vpn.ui.theme.SignalWeakDark
import com.hollow.vpn.ui.theme.SignalWeakLight
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// 44-column x 16-row stylized world continent dot matrix
private val WORLD_DOT_MASK = listOf(
    ".....######...........##.......#########....",
    "...##########........####...##############..",
    "..###########.......######################..",
    "...#########.........#####################..",
    "....#######..........####################...",
    ".....####............#########..########....",
    ".......###...........########....######.....",
    "........###...........######......####......",
    ".........#####........#######......##.......",
    ".........#######.....#########..............",
    "..........#######....########.......###.....",
    "..........######......######.......#####....",
    "...........####........####........######...",
    "...........###..........##..........####....",
    "............#...............................",
    "............................................"
)

private val WORLD_DOT_POINTS: List<Offset> by lazy {
    val rows = WORLD_DOT_MASK.size
    val cols = WORLD_DOT_MASK.first().length
    buildList {
        for (r in 0 until rows) {
            val row = WORLD_DOT_MASK[r]
            for (c in 0 until cols) {
                if (row[c] == '#') {
                    add(Offset((c + 0.5f) / cols.toFloat(), (r + 0.5f) / rows.toFloat()))
                }
            }
        }
    }
}

private fun countryMapCoordinate(countryCode: String): Offset {
    return when (countryCode.uppercase()) {
        "US" -> Offset(0.18f, 0.28f)
        "CA" -> Offset(0.19f, 0.16f)
        "BR" -> Offset(0.29f, 0.68f)
        "GB", "IE" -> Offset(0.46f, 0.19f)
        "FR", "NL", "BE" -> Offset(0.49f, 0.22f)
        "DE", "CH", "AT", "PL" -> Offset(0.52f, 0.22f)
        "IT" -> Offset(0.53f, 0.29f)
        "SE", "FI", "NO" -> Offset(0.54f, 0.12f)
        "UA", "RO" -> Offset(0.57f, 0.24f)
        "RU" -> Offset(0.68f, 0.15f)
        "TR", "AE" -> Offset(0.61f, 0.34f)
        "IN" -> Offset(0.71f, 0.44f)
        "SG", "MY", "TH" -> Offset(0.77f, 0.55f)
        "HK", "TW", "CN" -> Offset(0.81f, 0.36f)
        "KR", "JP" -> Offset(0.87f, 0.29f)
        "AU" -> Offset(0.85f, 0.74f)
        else -> Offset(0.52f, 0.24f)
    }
}

/**
 * Generates a smooth Material 3 Expressive multi-lobed scalloped shape path.
 */
private fun buildExpressiveScallopPath(
    center: Offset,
    baseRadius: Float,
    lobeAmplitude: Float,
    lobes: Int = 10
): Path {
    val steps = 120
    return Path().apply {
        for (i in 0..steps) {
            val angle = (2.0 * PI * i) / steps
            val r = baseRadius + lobeAmplitude * cos(lobes * angle).toFloat()
            val x = center.x + r * cos(angle).toFloat()
            val y = center.y + r * sin(angle).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

/**
 * Pure Material 3 Expressive surface backdrop with optional animated world dot matrix.
 */
@Composable
fun HollowAtmosphereBackground(
    modifier: Modifier = Modifier,
    isConnected: Boolean = false,
    isConnecting: Boolean = false,
    showWorldMap: Boolean = false,
    activeCountryCode: String = "DE",
    content: @Composable BoxScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f

    val auraTint by animateColorAsState(
        targetValue = when {
            isConnected -> scheme.tertiary
            isConnecting -> scheme.primary
            else -> scheme.primary
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "m3_aura_tint"
    )

    val mapMotion = rememberInfiniteTransition(label = "m3_world_map_motion")
    val sweepProgress by mapMotion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isConnecting) 2400 else 5800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "m3_map_sweep"
    )
    val beaconPulse by mapMotion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isConnecting) 1400 else 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "m3_map_beacon"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(scheme.background, scheme.surfaceContainerLow)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft tonal ambient wash
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        auraTint.copy(alpha = if (isConnected) (if (isDark) 0.18f else 0.12f) else (if (isDark) 0.11f else 0.08f)),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.28f),
                    radius = w * 0.80f
                ),
                radius = w * 0.80f,
                center = Offset(w * 0.5f, h * 0.28f)
            )

            if (showWorldMap) {
                val mapLeft = w * 0.04f
                val mapTop = h * 0.08f
                val mapW = w * 0.92f
                val mapH = h * 0.34f
                val dotRadius = (w * 0.0062f).coerceIn(1.8.dp.toPx(), 3.0.dp.toPx())
                val targetNorm = countryMapCoordinate(activeCountryCode)
                val targetCenter = Offset(mapLeft + targetNorm.x * mapW, mapTop + targetNorm.y * mapH)

                for (pt in WORLD_DOT_POINTS) {
                    val px = mapLeft + pt.x * mapW
                    val py = mapTop + pt.y * mapH
                    val distToWave = abs(pt.x - sweepProgress)
                    val waveBoost = 1f - (distToWave / 0.16f).coerceIn(0f, 1f)
                    val nearTarget = hypot(pt.x - targetNorm.x, pt.y - targetNorm.y) < 0.09f

                    val dotColor = when {
                        nearTarget && (isConnected || isConnecting) ->
                            auraTint.copy(alpha = 0.58f + 0.35f * waveBoost)
                        else ->
                            scheme.outlineVariant.copy(alpha = (if (isDark) 0.45f else 0.65f) + waveBoost * 0.28f)
                    }
                    drawCircle(
                        color = dotColor,
                        radius = dotRadius * (1f + waveBoost * 0.25f),
                        center = Offset(px, py)
                    )
                }

                if (isConnected || isConnecting) {
                    val originCenter = Offset(mapLeft + mapW * 0.42f, mapTop + mapH * 0.36f)
                    if (hypot(originCenter.x - targetCenter.x, originCenter.y - targetCenter.y) > 20f) {
                        val ctrl = Offset(
                            x = (originCenter.x + targetCenter.x) / 2f,
                            y = minOf(originCenter.y, targetCenter.y) - mapH * 0.24f
                        )
                        val routePath = Path().apply {
                            moveTo(originCenter.x, originCenter.y)
                            quadraticTo(ctrl.x, ctrl.y, targetCenter.x, targetCenter.y)
                        }
                        drawPath(
                            path = routePath,
                            color = auraTint.copy(alpha = 0.44f),
                            style = Stroke(
                                width = 1.8.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(10f, 10f),
                                    phase = -sweepProgress * 120f
                                ),
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }

                val rippleAlpha = (1f - beaconPulse) * if (isConnected || isConnecting) 0.55f else 0.28f
                val rippleRadius = 6.dp.toPx() + beaconPulse * 24.dp.toPx()
                drawCircle(
                    color = auraTint.copy(alpha = rippleAlpha),
                    radius = rippleRadius,
                    center = targetCenter,
                    style = Stroke(width = 1.6.dp.toPx())
                )
                drawCircle(
                    color = auraTint.copy(alpha = if (isConnected || isConnecting) 0.95f else 0.65f),
                    radius = 4.dp.toPx(),
                    center = targetCenter
                )
            }
        }
        content()
    }
}

/**
 * Material 3 Expressive Hero Scallop / Morphing Power Button.
 */
@Composable
fun TunnelPowerReactor(
    isConnected: Boolean,
    isConnecting: Boolean,
    onToggleClick: () -> Unit,
    modifier: Modifier = Modifier,
    reactorSize: Dp = 198.dp
) {
    val scheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "m3_expressive_press_scale"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isConnected -> scheme.tertiary
            isConnecting -> scheme.primaryContainer
            else -> scheme.surfaceContainerHigh
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "m3_core_container_color"
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            isConnected -> scheme.onTertiary
            isConnecting -> scheme.onPrimaryContainer
            else -> scheme.primary
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "m3_core_content_color"
    )
    val ringTint by animateColorAsState(
        targetValue = when {
            isConnected -> scheme.tertiary
            isConnecting -> scheme.primary
            else -> scheme.primaryContainer
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "m3_ring_tint"
    )

    val cornerRadius by animateDpAsState(
        targetValue = when {
            isPressed -> 24.dp
            isConnected -> 48.dp
            isConnecting -> 30.dp
            else -> 32.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "m3_expressive_corner_morph"
    )

    val motion = rememberInfiniteTransition(label = "m3_reactor_motion")
    val rotation by motion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isConnecting) 3200 else 18000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "m3_scallop_rotation"
    )
    val pulse by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "m3_sonar_pulse"
    )

    val buttonSize = reactorSize * 0.50f

    Box(
        modifier = modifier.size(reactorSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxR = size.minDimension * 0.44f

            // Expanding expressive ripple when connected or connecting
            if (isConnected || isConnecting) {
                val expandR = maxR * (0.72f + pulse * 0.34f)
                drawCircle(
                    color = ringTint.copy(alpha = (1f - pulse) * 0.32f),
                    radius = expandR,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Rotating Material 3 Expressive 10-lobe scalloped silhouette
            val scallopRotation = if (isConnected || isConnecting) rotation else rotation * 0.25f
            rotate(degrees = scallopRotation, pivot = center) {
                val outerScallop = buildExpressiveScallopPath(
                    center = center,
                    baseRadius = maxR * 0.85f,
                    lobeAmplitude = maxR * if (isConnecting) 0.085f else 0.055f,
                    lobes = 10
                )
                drawPath(
                    path = outerScallop,
                    color = ringTint.copy(alpha = if (isConnected) 0.22f else 0.55f)
                )
                drawPath(
                    path = outerScallop,
                    color = ringTint.copy(alpha = if (isConnected) 0.55f else 0.35f),
                    style = Stroke(width = 1.6.dp.toPx())
                )
            }
        }

        if (isConnecting) {
            CircularProgressIndicator(
                modifier = Modifier.size(buttonSize + 20.dp),
                color = scheme.primary,
                trackColor = scheme.surfaceContainerHighest,
                strokeWidth = 3.5.dp,
                strokeCap = StrokeCap.Round
            )
        }

        val actionDesc = when {
            isConnected -> "Disconnect VPN tunnel"
            isConnecting -> "Cancel VPN connection"
            else -> "Connect VPN tunnel"
        }

        Surface(
            modifier = Modifier
                .scale(pressScale)
                .size(buttonSize)
                .clip(RoundedCornerShape(cornerRadius))
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    role = Role.Button,
                    onClick = onToggleClick
                )
                .semantics { contentDescription = actionDesc }
                .testTag("hero_power_button"),
            shape = RoundedCornerShape(cornerRadius),
            color = containerColor,
            border = BorderStroke(
                width = 1.5.dp,
                color = if (isConnected) scheme.tertiary.copy(alpha = 0.4f) else scheme.outlineVariant.copy(alpha = 0.6f)
            ),
            tonalElevation = 6.dp,
            shadowElevation = if (isConnected) 8.dp else 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.PowerSettingsNew,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(buttonSize * 0.40f)
                )
            }
        }
    }
}

/**
 * Material 3 Expressive origami emblem card using dynamic colorScheme roles.
 */
@Composable
fun HollowVpnLogoCard(
    modifier: Modifier = Modifier,
    cardSize: Dp = 88.dp
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.size(cardSize),
        shape = RoundedCornerShape(28.dp),
        color = scheme.primaryContainer,
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(19.dp)
            ) {
                val w = size.width
                val h = size.height

                val upperWing = Path().apply {
                    moveTo(w * 0.06f, h * 0.46f)
                    lineTo(w * 0.94f, h * 0.10f)
                    lineTo(w * 0.50f, h * 0.55f)
                    close()
                }
                drawPath(path = upperWing, color = scheme.primary)

                val centerFold = Path().apply {
                    moveTo(w * 0.50f, h * 0.55f)
                    lineTo(w * 0.94f, h * 0.10f)
                    lineTo(w * 0.42f, h * 0.84f)
                    close()
                }
                drawPath(path = centerFold, color = scheme.onPrimaryContainer)

                val lowerWing = Path().apply {
                    moveTo(w * 0.50f, h * 0.55f)
                    lineTo(w * 0.94f, h * 0.10f)
                    lineTo(w * 0.72f, h * 0.90f)
                    close()
                }
                drawPath(path = lowerWing, color = scheme.tertiary)
            }
        }
    }
}

/**
 * Material 3 Expressive morphing scalloped container + rounded CircularProgressIndicator.
 */
@Composable
fun FetchingSpinnerRing(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val scheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "m3_expressive_loader")
    val rot by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "m3_loader_rot"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val r = this.size.minDimension * 0.44f
            rotate(degrees = rot, pivot = center) {
                drawPath(
                    path = buildExpressiveScallopPath(
                        center = center,
                        baseRadius = r,
                        lobeAmplitude = r * 0.12f,
                        lobes = 8
                    ),
                    color = scheme.primaryContainer
                )
            }
        }
        CircularProgressIndicator(
            modifier = Modifier.size(size * 0.52f),
            color = scheme.primary,
            trackColor = scheme.surfaceContainerHighest,
            strokeWidth = 3.dp,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * 3-bar cellular/latency signal indicator harmonized with Material 3 light/dark luminance.
 */
@Composable
fun SignalStrengthBars(
    pingMs: Int,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f
    val activeColor = when {
        pingMs in 1..89 -> if (isDark) SignalStrongDark else SignalStrongLight
        pingMs in 90..179 -> if (isDark) SignalMediumDark else SignalMediumLight
        else -> if (isDark) SignalWeakDark else SignalWeakLight
    }
    val activeBars = when {
        pingMs in 1..89 -> 3
        pingMs in 90..179 -> 2
        else -> 1
    }

    Row(
        modifier = modifier
            .height(14.dp)
            .width(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val heights = listOf(6.dp, 10.dp, 14.dp)
        heights.forEachIndexed { index, barHeight ->
            val barColor = if (index < activeBars) activeColor else scheme.surfaceContainerHighest
            Box(
                modifier = Modifier
                    .width(3.4.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(barColor)
            )
        }
    }
}

/**
 * Crisp circular country flag icon drawn with vector Canvas.
 */
@Composable
fun CircularCountryFlag(
    countryCode: String,
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Canvas(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    ) {
        val w = this.size.width
        val h = this.size.height
        when (countryCode.uppercase()) {
            "DE" -> {
                drawRect(color = Color(0xFF1A1A1A), topLeft = Offset(0f, 0f), size = Size(w, h / 3f))
                drawRect(color = Color(0xFFD92B2B), topLeft = Offset(0f, h / 3f), size = Size(w, h / 3f))
                drawRect(color = Color(0xFFF4C430), topLeft = Offset(0f, 2f * h / 3f), size = Size(w, h / 3f))
            }
            "SG" -> {
                drawRect(color = Color(0xFFE52535), topLeft = Offset(0f, 0f), size = Size(w, h / 2f))
                drawRect(color = Color(0xFFFFFFFF), topLeft = Offset(0f, h / 2f), size = Size(w, h / 2f))
                drawCircle(color = Color.White, radius = w * 0.14f, center = Offset(w * 0.34f, h * 0.26f))
                drawCircle(color = Color(0xFFE52535), radius = w * 0.11f, center = Offset(w * 0.39f, h * 0.26f))
            }
            "US" -> {
                val stripeCount = 7
                val stripeH = h / stripeCount
                for (i in 0 until stripeCount) {
                    drawRect(
                        color = if (i % 2 == 0) Color(0xFFD12B38) else Color.White,
                        topLeft = Offset(0f, i * stripeH),
                        size = Size(w, stripeH)
                    )
                }
                drawRect(color = Color(0xFF1E3A8A), topLeft = Offset(0f, 0f), size = Size(w * 0.52f, h * 0.54f))
            }
            "NL" -> {
                drawRect(color = Color(0xFFC8102E), topLeft = Offset(0f, 0f), size = Size(w, h / 3f))
                drawRect(color = Color.White, topLeft = Offset(0f, h / 3f), size = Size(w, h / 3f))
                drawRect(color = Color(0xFF1D4ED8), topLeft = Offset(0f, 2f * h / 3f), size = Size(w, h / 3f))
            }
            "GB" -> {
                drawRect(color = Color(0xFF0A2A66), size = Size(w, h))
                drawLine(Color.White, Offset(0f, 0f), Offset(w, h), strokeWidth = w * 0.22f)
                drawLine(Color.White, Offset(w, 0f), Offset(0f, h), strokeWidth = w * 0.22f)
                drawLine(Color(0xFFC8102E), Offset(0f, 0f), Offset(w, h), strokeWidth = w * 0.10f)
                drawLine(Color(0xFFC8102E), Offset(w, 0f), Offset(0f, h), strokeWidth = w * 0.10f)
                drawRect(Color.White, topLeft = Offset(0f, h * 0.36f), size = Size(w, h * 0.28f))
                drawRect(Color.White, topLeft = Offset(w * 0.36f, 0f), size = Size(w * 0.28f, h))
                drawRect(Color(0xFFC8102E), topLeft = Offset(0f, h * 0.42f), size = Size(w, h * 0.16f))
                drawRect(Color(0xFFC8102E), topLeft = Offset(w * 0.42f, 0f), size = Size(w * 0.16f, h))
            }
            "CA" -> {
                drawRect(color = Color(0xFFD81E28), topLeft = Offset(0f, 0f), size = Size(w * 0.26f, h))
                drawRect(color = Color.White, topLeft = Offset(w * 0.26f, 0f), size = Size(w * 0.48f, h))
                drawRect(color = Color(0xFFD81E28), topLeft = Offset(w * 0.74f, 0f), size = Size(w * 0.26f, h))
                drawCircle(color = Color(0xFFD81E28), radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.5f))
            }
            "FR" -> {
                drawRect(color = Color(0xFF1D428A), topLeft = Offset(0f, 0f), size = Size(w / 3f, h))
                drawRect(color = Color.White, topLeft = Offset(w / 3f, 0f), size = Size(w / 3f, h))
                drawRect(color = Color(0xFFE02B38), topLeft = Offset(2f * w / 3f, 0f), size = Size(w / 3f, h))
            }
            "JP" -> {
                drawRect(color = Color.White, size = Size(w, h))
                drawCircle(color = Color(0xFFBC002D), radius = w * 0.28f, center = Offset(w / 2f, h / 2f))
            }
            "IN" -> {
                drawRect(color = Color(0xFFFF9933), topLeft = Offset(0f, 0f), size = Size(w, h / 3f))
                drawRect(color = Color.White, topLeft = Offset(0f, h / 3f), size = Size(w, h / 3f))
                drawRect(color = Color(0xFF138808), topLeft = Offset(0f, 2f * h / 3f), size = Size(w, h / 3f))
                drawCircle(color = Color(0xFF000080), radius = w * 0.11f, center = Offset(w / 2f, h / 2f))
            }
            "AU" -> {
                drawRect(color = Color(0xFF0A2A66), size = Size(w, h))
                drawRect(color = Color(0xFFC8102E), topLeft = Offset(0f, h * 0.18f), size = Size(w * 0.48f, h * 0.12f))
                drawRect(color = Color(0xFFC8102E), topLeft = Offset(w * 0.18f, 0f), size = Size(w * 0.12f, h * 0.48f))
                drawCircle(color = Color.White, radius = w * 0.06f, center = Offset(w * 0.25f, h * 0.72f))
                drawCircle(color = Color.White, radius = w * 0.05f, center = Offset(w * 0.72f, h * 0.32f))
                drawCircle(color = Color.White, radius = w * 0.05f, center = Offset(w * 0.72f, h * 0.68f))
                drawCircle(color = Color.White, radius = w * 0.05f, center = Offset(w * 0.58f, h * 0.50f))
            }
            "CH" -> {
                drawRect(color = Color(0xFFD52B1E), size = Size(w, h))
                drawRect(color = Color.White, topLeft = Offset(w * 0.24f, h * 0.41f), size = Size(w * 0.52f, h * 0.18f))
                drawRect(color = Color.White, topLeft = Offset(w * 0.41f, h * 0.24f), size = Size(w * 0.18f, h * 0.52f))
            }
            "SE" -> {
                drawRect(color = Color(0xFF006AA7), size = Size(w, h))
                drawRect(color = Color(0xFFFECC02), topLeft = Offset(0f, h * 0.40f), size = Size(w, h * 0.20f))
                drawRect(color = Color(0xFFFECC02), topLeft = Offset(w * 0.32f, 0f), size = Size(w * 0.18f, h))
            }
            "BR" -> {
                drawRect(color = Color(0xFF009C3B), size = Size(w, h))
                drawCircle(color = Color(0xFFFFDF00), radius = w * 0.30f, center = Offset(w / 2f, h / 2f))
                drawCircle(color = Color(0xFF002776), radius = w * 0.18f, center = Offset(w / 2f, h / 2f))
            }
            "FI" -> {
                drawRect(color = Color.White, size = Size(w, h))
                drawRect(color = Color(0xFF003580), topLeft = Offset(0f, h * 0.38f), size = Size(w, h * 0.24f))
                drawRect(color = Color(0xFF003580), topLeft = Offset(w * 0.30f, 0f), size = Size(w * 0.22f, h))
            }
            "RU" -> {
                drawRect(color = Color.White, topLeft = Offset(0f, 0f), size = Size(w, h / 3f))
                drawRect(color = Color(0xFF0039A6), topLeft = Offset(0f, h / 3f), size = Size(w, h / 3f))
                drawRect(color = Color(0xFFD52B1E), topLeft = Offset(0f, 2f * h / 3f), size = Size(w, h / 3f))
            }
            "PL" -> {
                drawRect(color = Color.White, topLeft = Offset(0f, 0f), size = Size(w, h / 2f))
                drawRect(color = Color(0xFFDC143C), topLeft = Offset(0f, h / 2f), size = Size(w, h / 2f))
            }
            "AT" -> {
                drawRect(color = Color(0xFFED2939), topLeft = Offset(0f, 0f), size = Size(w, h / 3f))
                drawRect(color = Color.White, topLeft = Offset(0f, h / 3f), size = Size(w, h / 3f))
                drawRect(color = Color(0xFFED2939), topLeft = Offset(0f, 2f * h / 3f), size = Size(w, h / 3f))
            }
            "IT" -> {
                drawRect(color = Color(0xFF009246), topLeft = Offset(0f, 0f), size = Size(w / 3f, h))
                drawRect(color = Color.White, topLeft = Offset(w / 3f, 0f), size = Size(w / 3f, h))
                drawRect(color = Color(0xFFCE2B37), topLeft = Offset(2f * w / 3f, 0f), size = Size(w / 3f, h))
            }
            "TR", "HK" -> {
                drawRect(color = Color(0xFFE30A17), size = Size(w, h))
                drawCircle(color = Color.White, radius = w * 0.22f, center = Offset(w * 0.46f, h * 0.5f))
                drawCircle(color = Color(0xFFE30A17), radius = w * 0.17f, center = Offset(w * 0.52f, h * 0.5f))
            }
            "UA" -> {
                drawRect(color = Color(0xFF0057B7), topLeft = Offset(0f, 0f), size = Size(w, h / 2f))
                drawRect(color = Color(0xFFFFD700), topLeft = Offset(0f, h / 2f), size = Size(w, h / 2f))
            }
            "KR" -> {
                drawRect(color = Color.White, size = Size(w, h))
                drawCircle(color = Color(0xFFCD2E3A), radius = w * 0.26f, center = Offset(w / 2f, h / 2f))
                drawArc(
                    color = Color(0xFF0047A0),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.24f, h * 0.24f),
                    size = Size(w * 0.52f, h * 0.52f)
                )
            }
            else -> {
                drawRect(color = scheme.primary, topLeft = Offset(0f, 0f), size = Size(w, h / 3f))
                drawRect(color = Color.White, topLeft = Offset(0f, h / 3f), size = Size(w, h / 3f))
                drawRect(color = scheme.tertiary, topLeft = Offset(0f, 2f * h / 3f), size = Size(w, h / 3f))
            }
        }
        drawCircle(
            color = scheme.outlineVariant.copy(alpha = 0.6f),
            radius = (w / 2f) - 0.5f,
            center = Offset(w / 2f, h / 2f),
            style = Stroke(width = 1.2f)
        )
    }
}
