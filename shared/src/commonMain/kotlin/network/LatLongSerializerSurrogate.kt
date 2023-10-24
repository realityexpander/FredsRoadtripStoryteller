package network

import maps.LatLong
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// Define maps.LatLong for kotlinx serialization
@Serializable
@SerialName("maps.LatLong")
private class LatLongSurrogate(val lat: Double, val long: Double) {
    init {
        require(
            lat in -90.0..90.0
            && long in -180.0..180.0
        )
    }
}
object LatLongSerializer: KSerializer<LatLong> {
    override val descriptor: SerialDescriptor
        get() = LatLongSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: LatLong) {
        val surrogate = LatLongSurrogate(value.latitude, value.longitude)
        encoder.encodeSerializableValue(LatLongSurrogate.serializer(), surrogate)
    }

    override fun deserialize(decoder: Decoder): LatLong {
        val surrogate = decoder.decodeSerializableValue(LatLongSurrogate.serializer())
        return LatLong(surrogate.lat, surrogate.long)
    }
}
