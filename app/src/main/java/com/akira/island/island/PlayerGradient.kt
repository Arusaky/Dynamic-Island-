package com.akira.island.island

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.lerp
import com.akira.island.core.MediaInfo

private val PlayerBackdropDark = Color(0xFF110E18)

internal data class PlayerGradientColors(val main: Color, val highlight: Color)

internal fun playerGradientColors(accent: Color, secondary: Color?): PlayerGradientColors {
    val base = accent.takeUnless { it == Color.Unspecified } ?: Color(0xFFC6C3F7)
    val glow = secondary?.takeUnless { it == Color.Unspecified } ?: lerp(base, Color(0xFFFF7594), 0.44f)
    return PlayerGradientColors(darkenForPlayer(base, 0.60f), darkenForPlayer(glow, 0.62f))
}

private fun darkenForPlayer(color: Color, peakLimit: Float): Color {
    val srgb = color.convert(ColorSpaces.Srgb)
    val peak = maxOf(srgb.red, srgb.green, srgb.blue)
    val scale = peakLimit / peak.coerceAtLeast(0.22f)
    return lerp(PlayerBackdropDark, Color(srgb.red * scale, srgb.green * scale, srgb.blue * scale), 0.90f)
}

/**
 * Painted inside the island, above its fill and below its content and outline.
 * The glass version stays translucent. The cached brushes change only with color or size.
 */
@Composable
internal fun playerGradient(media: MediaInfo?, glass: Boolean, animated: Boolean): Modifier {
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
        val highlightBehindControls = Brush.radialGradient(
            listOf(glow.copy(alpha = glow.alpha * 0.65f), glow.copy(alpha = 0f)),
            center = Offset(size.width * 0.76f, size.height * 0.94f),
            radius = maxOf(1f, size.width * 0.68f, size.height * 0.50f),
        )
        onDrawBehind {
            drawRect(vertical)
            drawRect(highlightBehindControls)
        }
    }
}
