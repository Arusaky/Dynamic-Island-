package com.akira.island.island

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import com.akira.island.core.BatteryInfo
import com.akira.island.core.Controls
import com.akira.island.core.IslandBus
import com.akira.island.data.AppLanguage
import com.akira.island.data.SettingsRepo
import com.akira.island.ui.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StopwatchDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun pausedStopwatchCanBeResetAndClosedFromANarrowCard() {
        val saved = SettingsRepo.current
        val previous = IslandBus.stopwatch.value
        val focus = IslandBus.focus.value
        val expanded = IslandBus.expanded.value
        try {
            compose.runOnUiThread {
                SettingsRepo.update { copy(language = AppLanguage.EN, contentScale = 130, expandedMaxHeight = 180) }
                Controls.startStopwatch()
                Controls.expand("stopwatch")
                compose.activity.setContent {
                    val watch by IslandBus.stopwatch.collectAsState()
                    Box(Modifier.width(180.dp)) {
                        watch?.let { ExpandedContent(Act.StopwatchA(it), SettingsRepo.current, null, BatteryInfo(), 180.dp) }
                    }
                }
            }
            compose.onNodeWithContentDescription("Pause").assertIsDisplayed().performClick()
            compose.onNodeWithContentDescription("Reset").performScrollTo().assertIsDisplayed().performClick()
            compose.runOnIdle {
                val watch = checkNotNull(IslandBus.stopwatch.value)
                assertTrue(!watch.running)
                assertEquals(0L, watch.accumulated)
            }
            compose.onNodeWithContentDescription("Close").performScrollTo().assertIsDisplayed().performClick()
            compose.runOnIdle {
                assertNull(IslandBus.stopwatch.value)
                assertNull(IslandBus.focus.value)
                assertTrue(!IslandBus.expanded.value)
            }
        } finally {
            compose.runOnUiThread {
                SettingsRepo.update { saved }
                IslandBus.stopwatch.value = previous
                IslandBus.focus.value = focus
                IslandBus.expanded.value = expanded
            }
        }
    }
}
