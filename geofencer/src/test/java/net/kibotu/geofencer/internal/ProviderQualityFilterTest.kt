package net.kibotu.geofencer.internal

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProviderQualityFilterTest {

    private val history = LocationHistory()

    @Test
    fun `accepts gps location regardless of accuracy`() {
        val filter = ProviderQualityFilter(50f)
        assertTrue(filter.accept(location(0.0, 0.0, accuracy = 200f, provider = "gps"), history))
    }

    @Test
    fun `accepts good network location`() {
        val filter = ProviderQualityFilter(50f)
        assertTrue(filter.accept(location(0.0, 0.0, accuracy = 30f, provider = "network"), history))
    }

    @Test
    fun `rejects bad network location`() {
        val filter = ProviderQualityFilter(50f)
        assertFalse(filter.accept(location(0.0, 0.0, accuracy = 80f, provider = "network"), history))
    }

    @Test
    fun `accepts network location at exact threshold`() {
        val filter = ProviderQualityFilter(50f)
        assertTrue(filter.accept(location(0.0, 0.0, accuracy = 50f, provider = "network"), history))
    }

    @Test
    fun `disabled when threshold is zero`() {
        val filter = ProviderQualityFilter(0f)
        assertTrue(filter.accept(location(0.0, 0.0, accuracy = 999f, provider = "network"), history))
    }
}
