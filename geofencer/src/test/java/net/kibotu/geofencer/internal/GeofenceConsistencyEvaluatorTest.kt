package net.kibotu.geofencer.internal

import net.kibotu.geofencer.Geofence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeofenceConsistencyEvaluatorTest {

    private val fence = Geofence(
        id = "test",
        latitude = 52.520000,
        longitude = 13.404000,
        radius = 100.0,
        transitions = setOf(Geofence.Transition.Enter, Geofence.Transition.Exit),
        consistentSamples = 3,
    )

    @Test
    fun `no events on first location`() {
        val evaluator = GeofenceConsistencyEvaluator()
        val loc = location(52.520000, 13.404000)
        val events = evaluator.evaluate(loc, listOf(fence))
        assertTrue(events.isEmpty())
    }

    @Test
    fun `fires exit after N consistent samples outside`() {
        val evaluator = GeofenceConsistencyEvaluator()
        // First: inside (sets initial state)
        evaluator.evaluate(location(52.520000, 13.404000), listOf(fence))

        // ~2km away — clearly outside
        val outside = location(52.540000, 13.404000)
        assertTrue(evaluator.evaluate(outside, listOf(fence)).isEmpty()) // 1
        assertTrue(evaluator.evaluate(outside, listOf(fence)).isEmpty()) // 2
        val events = evaluator.evaluate(outside, listOf(fence)) // 3
        assertEquals(1, events.size)
        assertEquals(Geofence.Transition.Exit, events[0].transition)
    }

    @Test
    fun `does not fire for transition not in set`() {
        val enterOnly = fence.copy(transitions = setOf(Geofence.Transition.Enter))
        val evaluator = GeofenceConsistencyEvaluator()
        evaluator.evaluate(location(52.520000, 13.404000), listOf(enterOnly))

        val outside = location(52.540000, 13.404000)
        repeat(3) { evaluator.evaluate(outside, listOf(enterOnly)) }
        // Exit happened but not in transitions — should be empty after 3rd too
        // Actually: after 3 it flips. Let's verify by checking the 3rd call result
        val evaluator2 = GeofenceConsistencyEvaluator()
        evaluator2.evaluate(location(52.520000, 13.404000), listOf(enterOnly))
        evaluator2.evaluate(outside, listOf(enterOnly))
        evaluator2.evaluate(outside, listOf(enterOnly))
        val events = evaluator2.evaluate(outside, listOf(enterOnly))
        assertTrue(events.isEmpty())
    }

    @Test
    fun `clear resets all state`() {
        val evaluator = GeofenceConsistencyEvaluator()
        evaluator.evaluate(location(52.520000, 13.404000), listOf(fence))
        evaluator.clear()
        // After clear, next location is treated as first again
        val events = evaluator.evaluate(location(52.540000, 13.404000), listOf(fence))
        assertTrue(events.isEmpty())
    }
}
