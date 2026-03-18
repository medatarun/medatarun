package io.medatarun.model.adapters.jsonserializers

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.ModelRepoCmdEventInvalidOriginJsonException
import io.medatarun.model.infra.db.ModelRepoCmdEventUnknownOriginTypeException
import io.medatarun.tags.core.adapters.jsonserializers.TagsJsonSerializers
import io.medatarun.tags.core.domain.TagId
import io.medatarun.type.commons.serialization.SerializationUtils.enumWithCodeSerializer
import io.medatarun.type.commons.serialization.SerializationUtils.stringSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import java.net.URI
import java.net.URL

/**
 * Set of Kotlin serializers and deserializers for types in this module
 */
object ModelJsonSerializers {

    val modelId = stringSerializer("ModelId", ModelId.Companion::fromString) { it.value.toString() }
    val entityId = stringSerializer("EntityId", EntityId.Companion::fromString) { it.value.toString() }
    val attributeId = stringSerializer("AttributeId", AttributeId.Companion::fromString) { it.value.toString() }
    val relationshipId = stringSerializer("RelationshipId", RelationshipId.Companion::fromString) { it.value.toString() }
    val relationshipRoleId = stringSerializer("RelationshipRoleId", RelationshipRoleId.Companion::fromString) { it.value.toString() }
    val typeId = stringSerializer("TypeId", TypeId.Companion::fromString) { it.value.toString() }


    val modelKey = stringSerializer("ModelKey", { ModelKey(it) }) { it.value }
    val entityKey = stringSerializer("EntityKey", { EntityKey(it) }) { it.value }
    val attributeKey = stringSerializer("AttributeKey", { AttributeKey(it) }) { it.value }
    val relationshipKey = stringSerializer("RelationshipKey", { RelationshipKey(it) }) { it.value }
    val relationshipRoleKey = stringSerializer("RelationshipRoleKey", { RelationshipRoleKey(it) }) { it.value }
    val typeKey = stringSerializer("TypeKey", { TypeKey(it) }) { it.value }
    val modelVersion = object : KSerializer<ModelVersion> {
        override val descriptor = PrimitiveSerialDescriptor("ModelVersion", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: ModelVersion) {
            encoder.encodeString(value.value)
        }

        override fun deserialize(decoder: Decoder): ModelVersion {
            val rawValue = decoder.decodeString()
            return try {
                ModelVersion(rawValue)
            } catch (e: MedatarunException) {
                throw SerializationException(e.message ?: "Invalid model version", e)
            }
        }
    }
    val url = stringSerializer("URL", { URL(it) }) { it.toExternalForm() }

    val localizedTextAsString = object : KSerializer<LocalizedText> {
        override val descriptor = PrimitiveSerialDescriptor("LocalizedText", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: LocalizedText) {
            encoder.encodeString(value.name)
        }

        override fun deserialize(decoder: Decoder): LocalizedText {
            return LocalizedTextNotLocalized(decoder.decodeString())
        }
    }

    val localizedMarkdownAsString = object : KSerializer<LocalizedMarkdown> {
        override val descriptor = PrimitiveSerialDescriptor("LocalizedMarkdown", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: LocalizedMarkdown) {
            encoder.encodeString(value.name)
        }

        override fun deserialize(decoder: Decoder): LocalizedMarkdown {
            return LocalizedMarkdownNotLocalized(decoder.decodeString())
        }
    }


    val modelOrigin = object : KSerializer<ModelOrigin> {
        override val descriptor = JsonElement.serializer().descriptor

        override fun serialize(encoder: Encoder, value: ModelOrigin) {
            val jsonElement = when (value) {
                is ModelOrigin.Manual -> JsonObject(mapOf("origin_type" to JsonPrimitive("manual")))
                is ModelOrigin.Uri -> JsonObject(
                    mapOf(
                        "origin_type" to JsonPrimitive("uri"),
                        "uri" to JsonPrimitive(value.uri.toString())
                    )
                )
            }
            encoder.encodeSerializableValue(JsonElement.serializer(), jsonElement)
        }

        override fun deserialize(decoder: Decoder): ModelOrigin {
            val element = decoder.decodeSerializableValue(JsonElement.serializer()).jsonObject
            val originType = element["origin_type"]?.jsonPrimitive?.content
                ?: throw ModelRepoCmdEventInvalidOriginJsonException("model", "origin_type")
            return when (originType) {
                "manual" -> ModelOrigin.Manual
                "uri" -> {
                    val uri = element["uri"]?.jsonPrimitive?.content
                        ?: throw ModelRepoCmdEventInvalidOriginJsonException("model", "uri")
                    ModelOrigin.Uri(URI(uri))
                }
                else -> throw ModelRepoCmdEventUnknownOriginTypeException("model", originType)
            }
        }
    }

    val entityOrigin = object : KSerializer<EntityOrigin> {
        override val descriptor = JsonElement.serializer().descriptor

        override fun serialize(encoder: Encoder, value: EntityOrigin) {
            val jsonElement = when (value) {
                is EntityOrigin.Manual -> JsonObject(mapOf("origin_type" to JsonPrimitive("manual")))
                is EntityOrigin.Uri -> JsonObject(
                    mapOf(
                        "origin_type" to JsonPrimitive("uri"),
                        "uri" to JsonPrimitive(value.uri.toString())
                    )
                )
            }
            encoder.encodeSerializableValue(JsonElement.serializer(), jsonElement)
        }

        override fun deserialize(decoder: Decoder): EntityOrigin {
            val element = decoder.decodeSerializableValue(JsonElement.serializer()).jsonObject
            val originType = element["origin_type"]?.jsonPrimitive?.content
                ?: throw ModelRepoCmdEventInvalidOriginJsonException("entity", "origin_type")
            return when (originType) {
                "manual" -> EntityOrigin.Manual
                "uri" -> {
                    val uri = element["uri"]?.jsonPrimitive?.content
                        ?: throw ModelRepoCmdEventInvalidOriginJsonException("entity", "uri")
                    EntityOrigin.Uri(URI(uri))
                }
                else -> throw ModelRepoCmdEventUnknownOriginTypeException("entity", originType)
            }
        }
    }

    val modelAuthority = enumWithCodeSerializer("ModelAuthority", ModelAuthority.Companion::valueOfCode)
    val relationshipCardinality =
        enumWithCodeSerializer("RelationshipCardinality", RelationshipCardinality.Companion::valueOfCode)

    fun module(): SerializersModule {
        return SerializersModule {
            contextual(ModelId::class, modelId)
            contextual(EntityId::class, entityId)
            contextual(AttributeId::class, attributeId)
            contextual(RelationshipId::class, relationshipId)
            contextual(RelationshipRoleId::class, relationshipRoleId)
            contextual(TypeId::class, typeId)
            contextual(TagId::class, TagsJsonSerializers.tagId)
            contextual(ModelKey::class, modelKey)
            contextual(EntityKey::class, entityKey)
            contextual(AttributeKey::class, attributeKey)
            contextual(RelationshipKey::class, relationshipKey)
            contextual(RelationshipRoleKey::class, relationshipRoleKey)
            contextual(TypeKey::class, typeKey)
            contextual(ModelVersion::class, modelVersion)
            contextual(URL::class, url)
            contextual(LocalizedText::class, localizedTextAsString)
            contextual(LocalizedMarkdown::class, localizedMarkdownAsString)
            contextual(ModelOrigin::class, modelOrigin)
            contextual(EntityOrigin::class, entityOrigin)
            contextual(ModelAuthority::class, modelAuthority)
            contextual(RelationshipCardinality::class, relationshipCardinality)
        }
    }

}
