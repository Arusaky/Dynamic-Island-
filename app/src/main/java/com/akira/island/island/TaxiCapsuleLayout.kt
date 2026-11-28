package com.akira.island.island

/** Keep room for the status bar on both sides, even with a very wide custom idle capsule. */
internal fun taxiCapsuleWidth(screenDp: Int, baseDp: Int, extraDp: Int, heightDp: Float, gapDp: Int, showEta: Boolean): Int {
    val screen = screenDp.coerceAtLeast(1)
    val limit = (screen - 2 * (screen / 6).coerceIn(40, 56)).coerceAtLeast(1)
    val minimum = gapDp + if (showEta) maxOf(108, (heightDp * 2.5f).toInt()) else (heightDp * 1.65f).toInt()
    val preferred = baseDp + if (showEta) extraDp.coerceIn(0, 48) else 0
    return maxOf(minimum, preferred).coerceIn(1, limit)
}
