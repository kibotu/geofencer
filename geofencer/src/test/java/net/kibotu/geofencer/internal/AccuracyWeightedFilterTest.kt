package net.kibotu.geofencer.internal

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AccuracyWeightedFilterTest {

    private val filter = AccuracyWeightedFilter()

    @Test
    fun `accepts first location with empty history`() {
        val history = LocationHistory()
        assertTrue(filter.accept(location(52.0, 13.0, accuracy = 50f), history))
    }

    @Test
    fun `rejects movement within combined accuracy radius`() {
        val history = LocationHistory()
        val prev = location(52.520000, 13.404000, accuracy = 100f)
        history.add(prev)
        // ~11m north — well within 0.5 * (100 + 100) = 100m
        val next = location(52.520100, 13.404000, accuracy = 100f)
        assertFalse(filter.accept(next, history))
    }

    @Test
    fun `accepts movement beyond combined accuracy radius`() {
        val history = LocationHistory()
        val prev = location(52.520000, 13.404000, accuracy = 10f)
        history.add(prev)
        // ~1.1km north — way beyond 0.5 * (10 + 10) = 10m
        val next = location(52.530000, 13.404000, accuracy = 10f)
        assertTrue(filter.accept(next, history))
    }

    @Test
    fun `passes through when accuracy missing on previous`() {
        val history = LocationHistory()
        val prev = location(52.520000, 13.404000)
        prev.removeAccuracy()
        history.add(prev)
        assertTrue(filter.accept(location(52.520001, 13.404000, accuracy = 50f), history))
    }

    @Test
    fun `passes through when accuracy missing on candidate`() {
        val history = LocationHistory()
        history.add(location(52.520000, 13.404000, accuracy = 50f))
        val next = location(52.520001, 13.404000)
        next.removeAccuracy()
        assertTrue(filter.accept(next, history))
    }
}
