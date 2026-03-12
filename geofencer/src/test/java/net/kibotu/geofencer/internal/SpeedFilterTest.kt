package net.kibotu.geofencer.internal

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SpeedFilterTest {

    @Test
    fun `accepts first location with empty history`() {
        val filter = SpeedFilter(55f)
        val history = LocationHistory()
        assertTrue(filter.accept(location(52.0, 13.0, elapsedNanos = 1_000_000_000L), history))
    }

    @Test
    fun `accepts reasonable speed`() {
        val filter = SpeedFilter(55f)
        val history = LocationHistory()
        val prev = location(52.520000, 13.404000, elapsedNanos = 0L)
        history.add(prev)
        // ~111m north, 10 seconds later = ~11 m/s
        val next = location(52.521000, 13.404000, elapsedNanos = 10_000_000_000L)
        assertTrue(filter.accept(next, history))
    }

    @Test
    fun `rejects teleportation`() {
        val filter = SpeedFilter(55f)
        val history = LocationHistory()
        val prev = location(52.520000, 13.404000, elapsedNanos = 0L)
        history.add(prev)
        // Berlin to Paris-ish in 1 second
        val next = location(48.856, 2.352, elapsedNanos = 1_000_000_000L)
        assertFalse(filter.accept(next, history))
    }

    @Test
    fun `rejects zero time delta`() {
        val filter = SpeedFilter(55f)
        val history = LocationHistory()
        val prev = location(52.0, 13.0, elapsedNanos = 5_000_000_000L)
        history.add(prev)
        val next = location(52.001, 13.001, elapsedNanos = 5_000_000_000L)
        assertFalse(filter.accept(next, history))
    }

    @Test
    fun `disabled when max speed is zero`() {
        val filter = SpeedFilter(0f)
        val history = LocationHistory()
        history.add(location(52.0, 13.0, elapsedNanos = 0L))
        val next = location(48.0, 2.0, elapsedNanos = 1_000_000_000L)
        assertTrue(filter.accept(next, history))
    }
}
