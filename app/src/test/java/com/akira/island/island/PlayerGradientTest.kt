package com.akira.island.island

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerGradientTest {
    @Test fun vividAndWhiteArtworkProduceDarkEnoughPlayerColors() {
        for (color in listOf(Color.White, Color.Yellow, Color.Cyan, Color.Red, Color.Black)) {
            val gradient = playerGradientColors(color, Color.White)
            for (stop in listOf(gradient.main, gradient.highlight)) {
                val contrast = 1.05f / (stop.luminance() + 0.05f)
                assertTrue("White controls over ${stop} need at least 3:1 contrast, got $contrast", contrast >= 3f)
            }
        }
    }

    @Test fun coverColorsDriveBothTonesAndMissingArtworkHasAStableFallback() {
        val cool = playerGradientColors(Color(0xFF5544E1), Color(0xFFFF915E))
        val warm = playerGradientColors(Color(0xFFE14A63), Color(0xFF8560DA))
        assertNotEquals(cool.main, warm.main)
        assertNotEquals(cool.highlight, warm.highlight)
        assertTrue(cool.main.blue > cool.main.red)
        assertTrue(cool.highlight.red > cool.highlight.blue)
        assertEquals(playerGradientColors(Color.Unspecified, null), playerGradientColors(Color(0xFFC6C3F7), null))
    }

    @Test fun touchTracksTheCardAndStaysAtItsEdgeWhenDraggedOutside() {
        val bounds = IntSize(400, 200)
        assertEquals(Offset(0.75f, 0.25f), playerTouchPosition(Offset(300f, 50f), bounds))
        assertEquals(Offset(0f, 1f), playerTouchPosition(Offset(-25f, 235f), bounds))
    }

    @Test fun idleLightMovesAndReturnsToTheSamePointEachCycle() {
        val start = playerAmbientPosition(0L)
        assertNotEquals(start, playerAmbientPosition(2_000L))
        assertEquals(start, playerAmbientPosition(8_000L))
    }

    @Test fun aQuickTapLightsUpEvenWhenDownAndUpHappenBeforeDrawing() {
        val point = Offset(0.8f, 0.25f)
        val pressed = PlayerLightTouch(point, pressed = true)
        val released = pressed.release(1_020L)
        assertEquals(point, released.position)
        assertEquals(1f, playerTouchStrength(pressed, 1_000L, 560), 0f)
        assertTrue(playerTouchStrength(released, 1_036L, 560) > 0.99f)
        assertEquals(0.5f, playerTouchStrength(released, 1_300L, 560), 0.001f)
        assertEquals(0f, playerTouchStrength(released, 1_580L, 560), 0f)
        assertEquals(released, released.release(1_400L))
    }
}
