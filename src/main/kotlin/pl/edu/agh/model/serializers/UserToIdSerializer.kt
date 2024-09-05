package pl.edu.agh.model.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pl.edu.agh.model.User

@Serializer(forClass = User::class)
object UserToIdSerializer : KSerializer<User> {
    override fun serialize(encoder: Encoder, value: User) {
        encoder.encodeInt(value.id)
    }

    override fun deserialize(decoder: Decoder): User {
        throw UnsupportedOperationException("Deserialization of User from id is not supported")
    }

    override val descriptor: SerialDescriptor
        get() = Int.serializer().descriptor
}


