package com.akira.island.island

import com.akira.island.core.BatteryInfo
import com.akira.island.core.CHARGING_WAVE_MS
import com.akira.island.core.ChargingPulse
import org.junit.Assert.*
import org.junit.Test

class ChargingPresentationTest {
    private val battery = BatteryInfo(72, charging = true)
    private val pulse = ChargingPulse(battery, startedAt = 10_000L, durationMs = 2_500)

    @Test fun connectionOnlyChargeIsCompactAndCannotBeExpanded() {
        val compact = chargingActivity(battery, sticky = false, expanded = false, pulse = pulse)!!
        assertTrue(compact.transient)
        assertEquals(72, compact.b.level)
        assertEquals(pulse.startedAt, compact.waveStartedAt)
        assertNull(chargingActivity(battery, sticky = false, expanded = true, pulse = pulse))
    }

    @Test fun expandingThePersistentIndicatorKeepsChargeAndDropsTheWave() {
        val card = chargingActivity(battery, sticky = true, expanded = true, pulse = pulse)!!
        assertFalse(card.transient)
        assertEquals(battery, card.b)
        assertNull(card.waveStartedAt)
    }

    @Test fun wrappingTheSameEventInANewSurfaceDoesNotRestartTheWave() {
        assertEquals(0f, chargingWaveProgress(pulse.startedAt, pulse.startedAt), 0f)
        assertEquals(0.5f, chargingWaveProgress(pulse.startedAt, pulse.startedAt + CHARGING_WAVE_MS / 2), 0f)
        assertEquals(1f, chargingWaveProgress(pulse.startedAt, pulse.startedAt + 1_500), 0f)
        assertTrue(pulse.isCurrent(pulse.startedAt + 1_500))
        assertFalse(pulse.isCurrent(pulse.startedAt + pulse.durationMs))
    }
}
