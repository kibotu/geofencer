package net.kibotu.geofencer.internal

import android.location.Location

internal class LocationHistory(private val maxSize: Int = 10) {

    private val buffer = ArrayDeque<Location>(maxSize)

    @Synchronized
    fun add(location: Location) {
        if (buffer.size >= maxSize) buffer.removeFirst()
        buffer.addLast(location)
    }

    @Synchronized
    fun last(): Location? = buffer.lastOrNull()

    @Synchronized
    fun toList(): List<Location> = buffer.toList()

    @Synchronized
    fun clear() {
        buffer.clear()
    }

    val size: Int @Synchronized get() = buffer.size
}
