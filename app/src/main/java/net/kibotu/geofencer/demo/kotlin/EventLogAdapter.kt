package net.kibotu.geofencer.demo.kotlin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationResult
import net.kibotu.geofencer.demo.databinding.ItemEventLogBinding
import net.kibotu.geofencer.geofencer.GeofenceEvent
import net.kibotu.geofencer.geofencer.models.Geofence
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

sealed class LogEntry(val timestamp: Instant) {

    class Location(
        val result: LocationResult,
        ts: Instant = Instant.now(),
    ) : LogEntry(ts)

    class Fence(
        val event: GeofenceEvent,
        ts: Instant = Instant.now(),
    ) : LogEntry(ts)
}

class EventLogAdapter : RecyclerView.Adapter<EventLogAdapter.ViewHolder>() {

    private val items = mutableListOf<LogEntry>()

    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    fun add(entry: LogEntry) {
        items.add(0, entry)
        if (items.size > MAX_ENTRIES) {
            items.removeAt(items.lastIndex)
            notifyItemRemoved(items.size)
        }
        notifyItemInserted(0)
    }

    fun clear() {
        val count = items.size
        items.clear()
        notifyItemRangeRemoved(0, count)
    }

    val isEmpty: Boolean get() = items.isEmpty()

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEventLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(
        private val binding: ItemEventLogBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: LogEntry) {
            val time = timeFmt.format(entry.timestamp)
            when (entry) {
                is LogEntry.Location -> bindLocation(entry, time)
                is LogEntry.Fence -> bindFence(entry, time)
            }
        }

        private fun bindLocation(entry: LogEntry.Location, time: String) {
            val loc = entry.result.lastLocation ?: return
            binding.eventIcon.text = "\uD83D\uDCCD"
            binding.eventTitle.text = "%.6f, %.6f".format(loc.latitude, loc.longitude)
            binding.eventDetail.text = "\u00B1%.1fm \u00B7 %s".format(loc.accuracy, time)
        }

        private fun bindFence(entry: LogEntry.Fence, time: String) {
            val ev = entry.event
            val icon = when (ev.transition) {
                Geofence.Transition.Enter -> "\u2B07\uFE0F"
                Geofence.Transition.Exit -> "\u2B06\uFE0F"
                Geofence.Transition.Dwell -> "\u23F3"
            }
            val label = ev.geofence.label.ifEmpty { ev.geofence.id.take(8) }
            binding.eventIcon.text = icon
            binding.eventTitle.text = "${ev.transition.name}: $label"
            binding.eventDetail.text = "${ev.geofence.message.ifEmpty { "—" }} \u00B7 $time"
        }
    }

    companion object {
        private const val MAX_ENTRIES = 200
    }
}
