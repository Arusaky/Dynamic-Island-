package com.akira.island.island

import android.os.SystemClock
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.akira.island.core.BatteryInfo
import com.akira.island.core.Controls
import com.akira.island.core.IslandBus
import com.akira.island.core.MediaInfo
import com.akira.island.core.Tints
import com.akira.island.data.AppLanguage
import com.akira.island.data.IslandSettings
import com.akira.island.data.SettingsRepo
import com.akira.island.ui.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChargingMotionDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun chargingWaveFadesToTheOriginalSurface() {
        compose.mainClock.autoAdvance = false
        try {
            compose.runOnUiThread { compose.activity.setContent {
                Box(Modifier.size(180.dp, 44.dp).testTag("wave").background(Color.Black)
                    .then(chargingWave(IslandSettings(), remember { SystemClock.elapsedRealtime() })))
            } }
            fun greenPixels(): Int {
                val image = compose.onNodeWithTag("wave").captureToImage().toPixelMap()
                return (0 until image.height).sumOf { y ->
                    (0 until image.width).count { x -> image[x, y].green > 0.09f }
                }
            }
            compose.mainClock.advanceTimeBy(500)
            assertTrue(greenPixels() > 0)
            compose.mainClock.advanceTimeBy(850)
            assertEquals(0, greenPixels())
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    @Test fun chargingBubbleCanBeReopenedAfterInterruptedMorph() {
        val saved = SettingsRepo.current
        val oldBattery = IslandBus.battery.value
        val oldMedia = IslandBus.media.value
        val oldNotice = IslandBus.notice.value
        val oldFocus = IslandBus.focus.value
        val oldExpanded = IslandBus.expanded.value
        var bubble = Rect.Zero
        try {
            compose.runOnUiThread {
                SettingsRepo.update { IslandSettings(language = AppLanguage.EN, cardCharging = true, chargingBrief = false,
                    bubble = true, preferMusic = true, autoUpdateCheck = false, autoCollapseSec = 0, fxGlow = false) }
                IslandBus.battery.value = BatteryInfo(72, charging = true)
                IslandBus.media.value = MediaInfo("test", "", "Track", "Artist", null, null, Tints.Green,
                    100_000, 2_000, SystemClock.elapsedRealtime(), 1f, true, true)
                IslandBus.notice.value = null
                Controls.collapse()
                compose.activity.setContent { Box(Modifier.fillMaxSize()) { IslandOverlay(onBubbleBounds = { bubble = it }) } }
            }
            compose.mainClock.autoAdvance = false
            compose.mainClock.advanceTimeBy(1000)
            compose.onNodeWithContentDescription("Charging").performClick()
            compose.mainClock.advanceTimeBy(80)
            compose.runOnUiThread {
                assertEquals("charging", IslandBus.focus.value)
                IslandBus.battery.value = BatteryInfo(73, charging = true)
                Controls.collapse()
            }
            compose.mainClock.advanceTimeBy(1100)
            val charging = compose.onNodeWithContentDescription("Charging")
            charging.assertIsDisplayed()
            compose.runOnIdle { assertTrue(bubble.width > 0) }
            charging.performClick()
            compose.mainClock.advanceTimeBy(1100)
            compose.onNodeWithTag("expanded-idle").assertIsDisplayed()
            compose.runOnIdle {
                assertTrue(IslandBus.expanded.value)
                assertEquals("charging", IslandBus.focus.value)
                assertEquals(Rect.Zero, bubble)
            }
        } finally {
            compose.mainClock.autoAdvance = true
            compose.runOnUiThread {
                SettingsRepo.update { saved }
                IslandBus.battery.value = oldBattery
                IslandBus.media.value = oldMedia
                IslandBus.notice.value = oldNotice
                IslandBus.focus.value = oldFocus
                IslandBus.expanded.value = oldExpanded
            }
        }
    }
}
