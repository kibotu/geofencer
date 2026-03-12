package net.kibotu.geofencer.internal

import net.kibotu.geofencer.Geofence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TransitionCounterTest {

    @Test
    fun `first sample sets initial state without transition`() {
        val counter = TransitionCounter(requiredSamples = 3)
        assertNull(counter.update(inside = true))
    }

    @Test
    fun `staying in same state produces no transition`() {
        val counter = TransitionCounter(requiredSamples = 3)
        counter.update(inside = true)
        assertNull(counter.update(inside = true))
        assertNull(counter.update(inside = true))
        assertNull(counter.update(inside = true))
    }

    @Test
    fun `requires N consecutive samples to flip`() {
        val counter = TransitionCounter(requiredSamples = 3)
        counter.update(inside = true) // initial

        assertNull(counter.update(inside = false)) // 1
        assertNull(counter.update(inside = false)) // 2
        assertEquals(Geofence.Transition.Exit, counter.update(inside = false)) // 3 -> flip
    }

    @Test
    fun `resets count on interrupted sequence`() {
        val counter = TransitionCounter(requiredSamples = 3)
        counter.update(inside = true)

        counter.update(inside = false) // 1
        counter.update(inside = false) // 2
        counter.update(inside = true) // back to same — resets

        assertNull(counter.update(inside = false)) // 1 again
        assertNull(counter.update(inside = false)) // 2
        assertEquals(Geofence.Transition.Exit, counter.update(inside = false)) // 3 -> flip
    }

    @Test
    fun `can flip back to enter after exit`() {
        val counter = TransitionCounter(requiredSamples = 2)
        counter.update(inside = true) // initial

        counter.update(inside = false)
        assertEquals(Geofence.Transition.Exit, counter.update(inside = false))

        counter.update(inside = true)
        assertEquals(Geofence.Transition.Enter, counter.update(inside = true))
    }

    @Test
    fun `single sample required flips immediately`() {
        val counter = TransitionCounter(requiredSamples = 1)
        counter.update(inside = false) // initial
        assertEquals(Geofence.Transition.Enter, counter.update(inside = true))
    }
}
