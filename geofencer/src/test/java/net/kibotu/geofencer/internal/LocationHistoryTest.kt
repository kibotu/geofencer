package net.kibotu.geofencer.internal

import android.location.Location
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocationHistoryTest {

    @Test
    fun `empty history returns null for last`() {
        val history = LocationHistory()
        assertNull(history.last())
        assertEquals(0, history.size)
    }

    @Test
    fun `add and retrieve single location`() {
        val history = LocationHistory()
        val loc = location(52.0, 13.0)
        history.add(loc)
        assertEquals(loc, history.last())
        assertEquals(1, history.size)
    }

    @Test
    fun `respects max capacity`() {
        val history = LocationHistory(maxSize = 3)
        repeat(5) { i -> history.add(location(i.toDouble(), 0.0)) }
        assertEquals(3, history.size)
        assertEquals(4.0, history.last()!!.latitude, 0.0)
        assertEquals(2.0, history.toList().first().latitude, 0.0)
    }

    @Test
    fun `clear empties the buffer`() {
        val history = LocationHistory()
        history.add(location(1.0, 2.0))
        history.clear()
        assertEquals(0, history.size)
        assertNull(history.last())
    }

    @Test
    fun `toList returns snapshot in insertion order`() {
        val history = LocationHistory()
        history.add(location(1.0, 0.0))
        history.add(location(2.0, 0.0))
        history.add(location(3.0, 0.0))
        val list = history.toList()
        assertEquals(listOf(1.0, 2.0, 3.0), list.map { it.latitude })
    }
}
