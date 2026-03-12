package net.kibotu.geofencer.internal

import net.kibotu.geofencer.Geofence
import net.kibotu.geofencer.GeofenceEvent
import net.kibotu.geofencer.LatLng
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
class EventDeduplicatorTest {

    private val fence = Geofence(
        id = "test",
        latitude = 52.52,
        longitude = 13.40,
        radius = 100.0,
    )

    private fun enterEvent() = GeofenceEvent(fence, Geofence.Transition.Enter, LatLng(52.52, 13.40))
    private fun exitEvent() = GeofenceEvent(fence, Geofence.Transition.Exit, LatLng(52.54, 13.40))

    @Test
    fun `first event always emits`() {
        val dedup = EventDeduplicator(cooldown = 60.seconds)
        assertTrue(dedup.shouldEmit(enterEvent(), now = 1000L))
    }

    @Test
    fun `duplicate within cooldown is suppressed`() {
        val dedup = EventDeduplicator(cooldown = 60.seconds)
        dedup.shouldEmit(enterEvent(), now = 1000L)
        assertFalse(dedup.shouldEmit(enterEvent(), now = 30_000L))
    }

    @Test
    fun `same transition after cooldown emits`() {
        val dedup = EventDeduplicator(cooldown = 60.seconds)
        dedup.shouldEmit(enterEvent(), now = 1000L)
        assertTrue(dedup.shouldEmit(enterEvent(), now = 62_000L))
    }

    @Test
    fun `different transition emits immediately`() {
        val dedup = EventDeduplicator(cooldown = 60.seconds)
        dedup.shouldEmit(enterEvent(), now = 1000L)
        assertTrue(dedup.shouldEmit(exitEvent(), now = 2000L))
    }

    @Test
    fun `different geofences are independent`() {
        val dedup = EventDeduplicator(cooldown = 60.seconds)
        dedup.shouldEmit(enterEvent(), now = 1000L)

        val otherFence = fence.copy(id = "other")
        val otherEvent = GeofenceEvent(otherFence, Geofence.Transition.Enter, LatLng(52.52, 13.40))
        assertTrue(dedup.shouldEmit(otherEvent, now = 2000L))
    }

    @Test
    fun `clear resets state`() {
        val dedup = EventDeduplicator(cooldown = 60.seconds)
        dedup.shouldEmit(enterEvent(), now = 1000L)
        dedup.clear()
        assertTrue(dedup.shouldEmit(enterEvent(), now = 2000L))
    }
}
