package net.kibotu.geofencer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal object DurationMillisSerializer : KSerializer<Duration> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DurationMillis", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeLong(if (value.isInfinite()) -1L else value.inWholeMilliseconds)
    }

    override fun deserialize(decoder: Decoder): Duration {
        val millis = decoder.decodeLong()
        return if (millis < 0) Duration.INFINITE else millis.milliseconds
    }
}
