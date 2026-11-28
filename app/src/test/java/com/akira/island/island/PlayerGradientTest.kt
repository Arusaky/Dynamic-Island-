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
        val touch = Offset.Zero
        val start = playerLightPosition(0L, touch, 0f)
        assertNotEquals(start, playerLightPosition(2_000L, touch, 0f))
        assertEquals(start, playerLightPosition(8_000L, touch, 0f))
    }

    @Test fun lightFollowsTheLatestTouchAndBlendsBackToIdleOnRelease() {
        val frame = 2_000L
        val first = Offset(0.25f, 0.5f)
        val last = Offset(0.8f, 0.25f)
        val idle = playerLightPosition(frame, first, 0f)
        assertEquals(first, playerLightPosition(frame, first, 1f))
        val dragged = playerLightPosition(frame, last, 1f)
        assertEquals(last.x, dragged.x, 0.0001f)
        assertEquals(last.y, dragged.y, 0.0001f)
        val returning = playerLightPosition(frame, last, 0.5f)
        assertEquals((idle.x + last.x) / 2f, returning.x, 0.0001f)
        assertEquals((idle.y + last.y) / 2f, returning.y, 0.0001f)
        assertEquals(idle, playerLightPosition(frame, last, 0f))
    }
}
