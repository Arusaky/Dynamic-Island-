package com.akira.island.island

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerGradientTest {
    @Test fun vividAndWhiteArtworkProduceDarkEnoughPlayerColors() {
        for (color in listOf(Color.White, Color.Yellow, Color.Cyan, Color.Red, Color.Black)) {
            val gradient = playerGradientColors(color, Color.White)
            for (stop in listOf(gradient.main, gradient.highlight)) {
                assertTrue("White controls need a dark background", 1.05f / (stop.luminance() + 0.05f) >= 3f)
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
}
