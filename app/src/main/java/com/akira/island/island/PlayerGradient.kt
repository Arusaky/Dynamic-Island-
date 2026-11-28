package com.akira.island.island

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.IntSize
import com.akira.island.core.MediaInfo
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val PlayerBackdropDark = Color(0xFF110E18)

internal data class PlayerGradientColors(val main: Color, val highlight: Color)
internal data class PlayerLightTouch(val position: Offset = Offset(0.5f, 0.5f), val pressed: Boolean = false)

internal fun playerTouchPosition(position: Offset, bounds: IntSize): Offset = Offset(
    (position.x / bounds.width.coerceAtLeast(1)).coerceIn(0f, 1f),
    (position.y / bounds.height.coerceAtLeast(1)).coerceIn(0f, 1f),
)

internal fun playerLightPosition(timeMs: Long, touch: Offset, strength: Float): Offset {
    val phase = (timeMs % 8_000L) * (2 * PI / 8_000)
    val idle = Offset(0.60f + 0.23f * cos(phase).toFloat(), 0.78f + 0.17f * sin(phase).toFloat())
    return idle + (touch - idle) * strength
}

internal fun playerGradientColors(accent: Color, secondary: Color?): PlayerGradientColors {
    val base = accent.takeUnless { it == Color.Unspecified } ?: Color(0xFFC6C3F7)
    val glow = secondary?.takeUnless { it == Color.Unspecified } ?: lerp(base, Color(0xFFFF7594), 0.44f)
    return PlayerGradientColors(darkenForPlayer(base, 0.60f), darkenForPlayer(glow, 0.62f))
}

private fun darkenForPlayer(color: Color, peakLimit: Float): Color {
    val srgb = color.convert(ColorSpaces.Srgb)
    val peak = maxOf(srgb.red, srgb.green, srgb.blue)
    val scale = peakLimit / peak.coerceAtLeast(0.22f)
    val tint = lerp(PlayerBackdropDark, Color(srgb.red * scale, srgb.green * scale, srgb.blue * scale), 0.90f)
    val luminance = tint.luminance()
    if (luminance <= 0.27f) return tint

    // Scale in linear light so vivid colors keep their hue while white playback controls stay legible.
    val linear = tint.convert(ColorSpaces.LinearSrgb)
    val dim = 0.27f / luminance
    return Color(linear.red * dim, linear.green * dim, linear.blue * dim,
        colorSpace = ColorSpaces.LinearSrgb).convert(ColorSpaces.Srgb)
}

/**
 * Painted inside the island, above its fill and below its content and outline.
 * The glass version stays translucent. The cached brushes change only with color or size.
 */
@Composable
internal fun playerGradient(
    media: MediaInfo?,
    glass: Boolean,
    animated: Boolean,
    live: Boolean,
    touch: State<PlayerLightTouch>,
): Modifier {
    val colors = remember(media?.accent, media?.artworkSecondaryAccent) {
        media?.let { playerGradientColors(it.accent, it.artworkSecondaryAccent) }
    }
    val duration = if (colors == null) 170 else 340
    val spec = if (animated) tween<Color>(duration) else snap<Color>()
    val main = animateColorAsState(
        colors?.main?.copy(alpha = if (glass) 0.52f else 0.87f) ?: Color.Transparent,
        spec,
        label = "player gradient",
    )
    val highlight = animateColorAsState(
        colors?.highlight?.copy(alpha = if (glass) 0.40f else 0.78f) ?: Color.Transparent,
        spec,
        label = "player gradient highlight",
    )
    val visible by remember(main, highlight) {
        derivedStateOf { main.value.alpha > 0.001f || highlight.value.alpha > 0.001f }
    }
    if (!visible && colors == null) return Modifier

    val time = rememberFxClock(live)
    val pressed by remember(touch) { derivedStateOf { touch.value.pressed } }
    val following = live && pressed
    val strength = animateFloatAsState(
        if (following) 1f else 0f,
        if (live) tween(if (following) 125 else 480, easing = FastOutSlowInEasing) else snap(),
        label = "player light touch",
    )
    return Modifier.drawWithCache {
        val base = main.value
        val glow = highlight.value
        val vertical = Brush.verticalGradient(
            0f to PlayerBackdropDark.copy(alpha = base.alpha * 0.12f),
            0.24f to PlayerBackdropDark.copy(alpha = base.alpha * 0.18f),
            0.54f to base.copy(alpha = base.alpha * 0.58f),
            0.80f to base,
            1f to lerp(base, glow, 0.55f),
        )
        val radius = maxOf(1f, size.width * 0.67f, size.height * 0.65f)
        val halo = Brush.radialGradient(
            0f to glow.copy(alpha = glow.alpha),
            0.55f to glow.copy(alpha = glow.alpha * 0.42f),
            1f to glow.copy(alpha = 0f),
            center = Offset.Zero,
            radius = radius,
        )
        val focusRadius = radius * 0.62f
        val focusColor = lerp(glow, Color(0xFF9A89C0), 0.22f)
        val focus = Brush.radialGradient(
            listOf(focusColor.copy(alpha = glow.alpha * 0.64f), focusColor.copy(alpha = 0f)),
            center = Offset.Zero,
            radius = focusRadius,
        )
        onDrawBehind {
            drawRect(vertical)
            val amount = if (live) strength.value else 0f
            val center = if (live) playerLightPosition(time.value, touch.value.position, amount)
                else Offset(0.76f, 0.94f)
            // Move cached shaders in the draw pass while the card and its controls keep their layout.
            translate(size.width * center.x, size.height * center.y) {
                drawCircle(halo, center = Offset.Zero, radius = radius, alpha = 0.69f + amount * 0.31f)
                if (amount > 0f) drawCircle(focus, center = Offset.Zero, radius = focusRadius, alpha = amount)
            }
        }
    }
}
