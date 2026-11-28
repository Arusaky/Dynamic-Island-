package com.akira.island.island

import com.akira.island.core.BatteryInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityContinuationTest {
    @Test fun replacementKeepsAnOpenCardWithAnotherRealActivity() {
        val charging = Act.ChargingA(BatteryInfo(60, charging = true))
        assertEquals(charging.id, replacementForExpandedFocus(listOf(Act.Idle, charging)))
        assertNull(replacementForExpandedFocus(listOf(Act.Idle)))
    }

    @Test fun taxiRemainsCompactOnNarrowScreensWithCustomDimensions() {
        val status = taxiCapsuleWidth(320, 400, 200, 37f, 34, showEta = true)
        assertTrue(status <= 214)
        val normal = taxiCapsuleWidth(390, 104, 40, 37f, 34, showEta = true)
        val icon = taxiCapsuleWidth(390, 104, 40, 37f, 34, showEta = false)
        assertEquals(144, normal)
        assertEquals(104, icon)
    }
}
