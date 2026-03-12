package net.kibotu.geofencer.internal

import net.kibotu.geofencer.Geofence
import net.kibotu.geofencer.GeofenceEvent
import net.kibotu.geofencer.LatLng
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
class DwellConfirmationTest {

    private val fence = Geofence(
        id = "test",
        latitude = 52.52,
        longitude = 13.40,
        radius = 100.0,
        enterDwellDuration = 10.seconds,
        exitDwellDuration = 10.seconds,
    )

    private fun enterEvent() = GeofenceEvent(fence, Geofence.Transition.Enter, LatLng(52.52, 13.40))
    private fun exitEvent() = GeofenceEvent(fence, Geofence.Transition.Exit, LatLng(52.54, 13.40))
    private fun dwellEvent() = GeofenceEvent(fence, Geofence.Transition.Dwell, LatLng(52.52, 13.40))

    @Test
    fun `first occurrence starts pending, returns null`() {
        val dwell = DwellConfirmation()
        assertNull(dwell.process(enterEvent(), now = 1000L))
    }

    @Test
    fun `emits after dwell duration elapsed`() {
        val dwell = DwellConfirmation()
        dwell.process(enterEvent(), now = 1000L)
        assertNull(dwell.process(enterEvent(), now = 5000L))
        assertNotNull(dwell.process(enterEvent(), now = 11_000L))
    }

    @Test
    fun `cancels pending on opposite transition`() {
        val dwell = DwellConfirmation()
        dwell.process(enterEvent(), now = 1000L)
        // Switch to exit before enter dwell completes
        dwell.process(exitEvent(), now = 5000L)
        // Enter should now be forgotten — trying enter again starts fresh
        assertNull(dwell.process(enterEvent(), now = 6000L))
    }

    @Test
    fun `dwell transitions pass through immediately`() {
        val dwell = DwellConfirmation()
        assertNotNull(dwell.process(dwellEvent(), now = 0L))
    }

    @Test
    fun `zero dwell duration passes through immediately`() {
        val zeroDwell = fence.copy(enterDwellDuration = 0.seconds)
        val event = GeofenceEvent(zeroDwell, Geofence.Transition.Enter, LatLng(52.52, 13.40))
        val dwell = DwellConfirmation()
        assertNotNull(dwell.process(event, now = 0L))
    }
}
