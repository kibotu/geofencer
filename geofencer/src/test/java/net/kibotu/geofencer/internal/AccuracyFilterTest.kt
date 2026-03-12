package net.kibotu.geofencer.internal

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AccuracyFilterTest {

    private val history = LocationHistory()

    @Test
    fun `accepts location within threshold`() {
        val filter = AccuracyFilter(50f)
        assertTrue(filter.accept(location(0.0, 0.0, accuracy = 30f), history))
    }

    @Test
    fun `rejects location exceeding threshold`() {
        val filter = AccuracyFilter(50f)
        assertFalse(filter.accept(location(0.0, 0.0, accuracy = 80f), history))
    }

    @Test
    fun `accepts location at exact threshold`() {
        val filter = AccuracyFilter(50f)
        assertTrue(filter.accept(location(0.0, 0.0, accuracy = 50f), history))
    }

    @Test
    fun `disabled when threshold is zero`() {
        val filter = AccuracyFilter(0f)
        assertTrue(filter.accept(location(0.0, 0.0, accuracy = 999f), history))
    }

    @Test
    fun `accepts when accuracy not available`() {
        val filter = AccuracyFilter(50f)
        val loc = location(0.0, 0.0, accuracy = 0f)
        loc.removeAccuracy()
        assertTrue(filter.accept(loc, history))
    }
}
