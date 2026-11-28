package com.akira.island.ui.setup

import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.akira.island.core.tr
import com.akira.island.ui.IslandTheme
import com.akira.island.ui.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SetupFlowDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun requiredPermissionsUnlockStyleThenActivitiesThenHome() {
        val access = mutableStateOf(SetupAccess(false, false, false))
        var finished = false
        compose.runOnUiThread {
            compose.activity.setContent {
                IslandTheme { SetupFlow(access.value, {}, {}, {}, {}) { finished = true } }
            }
        }
        val next = compose.onNodeWithTag("setup-next")
        next.assertIsNotEnabled()
        compose.runOnIdle { access.value = SetupAccess(true, false, true) }
        next.assertIsNotEnabled()
        compose.runOnIdle { access.value = SetupAccess(true, true, false) }
        next.assertIsEnabled().performClick()
        compose.onNodeWithText(tr("Ваш остров", "Your island")).assertExists()
        next.performClick()
        compose.onNodeWithText(tr("Ваши активности", "Your activities")).assertExists()
        next.performClick()
        compose.onNodeWithText(tr("Остров готов!", "Your island is ready!")).assertExists()
        next.performClick()
        compose.runOnIdle { assertTrue(finished) }
    }
}
