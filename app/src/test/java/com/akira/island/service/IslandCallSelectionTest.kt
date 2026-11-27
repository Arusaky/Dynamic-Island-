package com.akira.island.service

import com.akira.island.core.CallInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IslandCallSelectionTest {
    private fun call(key: String, pkg: String, incoming: Boolean, posted: Long) =
        CallInfo(key, pkg, key, null, incoming, posted, posted, null, null, null, null)

    @Test fun ringingCarrierCallDoesNotReuseTheActiveMessengerIdentity() {
        val carrier = call(CallMonitor.KEY, "dialer", true, 1)
        val activeMessenger = call("active", "messenger", false, 2)
        assertEquals(carrier, selectIslandCall(listOf(carrier, activeMessenger)))
    }

    @Test fun dialerNotificationEnrichesCarrierWithoutLosingItsIdentity() {
        val carrier = call(CallMonitor.KEY, "dialer", true, 1)
        val notification = call("incoming", "dialer", true, 2)
        val selected = checkNotNull(selectIslandCall(listOf(carrier, notification)))
        assertEquals(CallMonitor.KEY, selected.key)
        assertEquals(notification.name, selected.name)
        assertTrue(selected.incoming)
    }

    @Test fun activeDialerNotificationDoesNotReplaceRingingCallerIdentity() {
        val carrier = call(CallMonitor.KEY, "dialer", true, 1)
        val activeDialer = call("ongoing", "dialer", false, 2)
        assertEquals(carrier, selectIslandCall(listOf(carrier, activeDialer)))
    }

    @Test fun staleDialerBannerDoesNotReplaceTheAnsweredCarrierState() {
        val answered = call(CallMonitor.KEY, "dialer", false, 2)
        assertEquals(answered, selectIslandCall(listOf(answered, call("old", "dialer", true, 1))))
        val later = call("call-waiting", "dialer", true, 3)
        assertEquals(later, selectIslandCall(listOf(answered, later)))
    }

    @Test fun ringingMessengerCallTakesPrecedenceOverAnActivePhoneCall() {
        val carrier = call(CallMonitor.KEY, "dialer", false, 2)
        val messenger = call("incoming", "messenger", true, 1)
        assertEquals(messenger, selectIslandCall(listOf(carrier, messenger)))
    }
}
