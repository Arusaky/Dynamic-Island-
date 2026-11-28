package com.akira.island.island

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.akira.island.service.glassBlurRadiusDp
import kotlin.math.roundToInt

/** A locally rendered example of the optics. The overlay itself uses native cross-window blur. */
@Composable
internal fun previewGlassBackdrop(strength: Int): Modifier {
    val layer = rememberGraphicsLayer()
    return Modifier.drawWithCache {
        val width = size.width.roundToInt()
        val height = size.height.roundToInt()
        if (width <= 0 || height <= 0) return@drawWithCache onDrawBehind { }
        val blur = glassBlurRadiusDp(strength).dp.toPx()
        layer.renderEffect = if (Build.VERSION.SDK_INT >= 31 && blur > 0f) BlurEffect(blur, blur, TileMode.Clamp) else null
        layer.record(this, layoutDirection, IntSize(width, height)) {
            drawRect(Brush.linearGradient(listOf(Color(0xFF7165A0), Color(0xFF56738A), Color(0xFF31695F))))
            for (index in 1..3) {
                drawCircle(
                    Color.White.copy(alpha = 0.12f), radius = size.minDimension * 0.75f,
                    center = androidx.compose.ui.geometry.Offset(size.width * (index / 4f), size.height * 1.2f),
                )
            }
        }
        onDrawBehind { drawLayer(layer) }
    }
}
