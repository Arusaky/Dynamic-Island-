package com.akira.island.island

import androidx.compose.runtime.State
import androidx.compose.runtime.staticCompositionLocalOf

import androidx.compose.ui.geometry.Rect

/** The visible window-space contour, independent of the unchanged hit-testing bounds. */
data class BackdropGeometry(
    val area: Rect,
    val bottomRadius: Float,
    val topRadius: Float,
    val usable: Boolean,
    val card: Boolean,
)

internal class NativeIslandBackdrop(val body: State<Boolean>, val bubble: State<Boolean>)

internal val LocalNativeIslandBackdrop = staticCompositionLocalOf<NativeIslandBackdrop?> { null }
