package net.kibotu.geofencer.internal

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocationFilterPipelineTest {

    @Test
    fun `empty pipeline accepts everything`() {
        val pipeline = LocationFilterPipeline(emptyList())
        assertTrue(pipeline.accept(location(0.0, 0.0), LocationHistory()))
    }

    @Test
    fun `pipeline short-circuits on first rejection`() {
        var secondCalled = false
        val rejectAll = LocationFilter { _, _ -> false }
        val tracker = LocationFilter { _, _ -> secondCalled = true; true }
        val pipeline = LocationFilterPipeline(listOf(rejectAll, tracker))

        assertFalse(pipeline.accept(location(0.0, 0.0), LocationHistory()))
        assertFalse(secondCalled)
    }

    @Test
    fun `all filters must pass`() {
        val pass = LocationFilter { _, _ -> true }
        val pipeline = LocationFilterPipeline(listOf(pass, pass, pass))
        assertTrue(pipeline.accept(location(0.0, 0.0), LocationHistory()))
    }

    @Test
    fun `realistic indoor drift scenario`() {
        val history = LocationHistory()
        val pipeline = LocationFilterPipeline(
            listOf(
                AccuracyFilter(50f),
                SpeedFilter(55f),
                AccuracyWeightedFilter(),
            )
        )

        val desk = location(52.520000, 13.404000, accuracy = 15f, elapsedNanos = 0L)
        assertTrue(pipeline.accept(desk, history))
        history.add(desk)

        // noisy jump with bad accuracy — rejected by AccuracyFilter
        val noisyFar = location(52.522000, 13.404000, accuracy = 120f, elapsedNanos = 5_000_000_000L)
        assertFalse(pipeline.accept(noisyFar, history))

        // tiny jitter within error radius — rejected by AccuracyWeightedFilter
        val jitter = location(52.520001, 13.404001, accuracy = 15f, elapsedNanos = 10_000_000_000L)
        assertFalse(pipeline.accept(jitter, history))

        // genuine movement ~500m — accepted
        val realMove = location(52.524500, 13.404000, accuracy = 12f, elapsedNanos = 60_000_000_000L)
        assertTrue(pipeline.accept(realMove, history))
    }
}
