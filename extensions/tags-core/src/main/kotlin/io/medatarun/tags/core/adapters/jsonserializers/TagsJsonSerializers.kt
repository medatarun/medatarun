package io.medatarun.tags.core.adapters.jsonserializers

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.infra.db.TagStorageEventInvalidScopeRefException
import io.medatarun.type.commons.id.Id
import io.medatarun.type.commons.serialization.SerializationUtils.stringSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule

object TagsJsonSerializers {
    val tagId = stringSerializer("TagId", { TagId.fromString(it) }) { it.value.toString() }
    val tagGroupId = stringSerializer("TagGroupId", { Id.fromString(it, ::TagGroupId) }) { it.value.toString() }
    val tagKey = stringSerializer("TagKey", { TagKey(it) }) { it.value }
    val tagGroupKey = stringSerializer("TagGroupKey", { TagGroupKey(it) }) { it.value }
    val tagScopeType = stringSerializer("TagScopeType", { TagScopeType(it) }) { it.value }
    val tagScopeId = stringSerializer("TagScopeId", { Id.fromString(it, ::TagScopeId) }) { it.value.toString() }

    val tagScopeRef = object : KSerializer<TagScopeRef> {
        override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

        override fun serialize(encoder: Encoder, value: TagScopeRef) {
            try {
                val jsonEncoder = encoder as? JsonEncoder
                    ?: throw TagStorageEventInvalidScopeRefException("encoder_not_json")
                val jsonObject = when (value) {
                    is TagScopeRef.Global -> JsonObject(
                        mapOf("type" to JsonPrimitive(TagScopeRef.Global.type.value))
                    )
                    is TagScopeRef.Local -> JsonObject(
                        mapOf(
                            "type" to JsonPrimitive(value.type.value),
                            "id" to JsonPrimitive(value.localScopeId.asString())
                        )
                    )
                }
                jsonEncoder.encodeJsonElement(jsonObject)
            } catch (e: TagStorageEventInvalidScopeRefException) {
                throw SerializationException(e.message ?: "Invalid tag scope ref payload.", e)
            }
        }

        override fun deserialize(decoder: Decoder): TagScopeRef {
            return try {
                val jsonDecoder = decoder as? JsonDecoder
                    ?: throw TagStorageEventInvalidScopeRefException("decoder_not_json")
                val json = jsonDecoder.decodeJsonElement()
                val jsonObject = json as? JsonObject
                    ?: throw TagStorageEventInvalidScopeRefException(json.toString())
                val typePrimitive = jsonObject["type"] as? JsonPrimitive
                    ?: throw TagStorageEventInvalidScopeRefException(json.toString())
                if (!typePrimitive.isString) {
                    throw TagStorageEventInvalidScopeRefException(json.toString())
                }
                val typeRaw = typePrimitive.content
                if (typeRaw == TagScopeRef.Global.type.value) {
                    return TagScopeRef.Global
                }
                val idPrimitive = jsonObject["id"] as? JsonPrimitive
                    ?: throw TagStorageEventInvalidScopeRefException(json.toString())
                if (!idPrimitive.isString) {
                    throw TagStorageEventInvalidScopeRefException(json.toString())
                }
                val scopeIdRaw = idPrimitive.content
                try {
                    TagScopeRef.Local(
                        type = TagScopeType(typeRaw),
                        localScopeId = Id.fromString(scopeIdRaw, ::TagScopeId)
                    )
                } catch (e: MedatarunException) {
                    throw TagStorageEventInvalidScopeRefException(json.toString())
                }
            } catch (e: TagStorageEventInvalidScopeRefException) {
                throw SerializationException(e.message ?: "Invalid tag scope ref payload.", e)
            }
        }
    }

    fun module(): SerializersModule {
        return SerializersModule {
            contextual(TagId::class, tagId)
            contextual(TagGroupId::class, tagGroupId)
            contextual(TagKey::class, tagKey)
            contextual(TagGroupKey::class, tagGroupKey)
            contextual(TagScopeType::class, tagScopeType)
            contextual(TagScopeId::class, tagScopeId)
            contextual(TagScopeRef::class, tagScopeRef)
        }
    }
}
