package com.akira.island.island

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class CameraContentInsetTest {
    @Test fun protectsExpandedContentFromACutoutDeeperThanItsDefaultPadding() {
        assertEquals(52.dp, cameraContentInset(height = 36.dp, top = 6.dp, depth = 54.dp))
    }

    @Test fun keepsExistingPaddingWhenTheCameraIsCoveredByTheExistingGap() {
        assertEquals(29.5.dp, cameraContentInset(height = 36.dp, top = 12.dp, depth = 30.dp))
        assertEquals(29.5.dp, cameraContentInset(height = 36.dp, top = -40.dp, depth = 0.dp))
    }
}
