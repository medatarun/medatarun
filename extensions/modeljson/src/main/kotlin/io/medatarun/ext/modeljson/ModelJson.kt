package io.medatarun.ext.modeljson

import io.medatarun.model.infra.*
import io.medatarun.model.model.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule


inline fun <reified T> valueClassSerializer(
    crossinline wrap: (String) -> T,
    crossinline unwrap: (T) -> String
): KSerializer<T> =
    object : KSerializer<T> {
        override val descriptor =
            PrimitiveSerialDescriptor(T::class.simpleName ?: "ValueClass", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: T) =
            encoder.encodeString(unwrap(value))

        override fun deserialize(decoder: Decoder): T =
            wrap(decoder.decodeString())
    }

class LocalizedTextSerializer : KSerializer<LocalizedText> {
    override val descriptor = JsonElement.serializer().descriptor

    override fun serialize(encoder: Encoder, value: LocalizedText) {
        val json = if (!value.isLocalized)
            JsonPrimitive(value.name)
        else
            JsonObject(value.all().mapValues { JsonPrimitive(it.value) })
        encoder.encodeSerializableValue(JsonElement.serializer(), json)
    }

    override fun deserialize(decoder: Decoder): LocalizedText {
        val element = decoder.decodeSerializableValue(JsonElement.serializer())
        return when (element) {
            is JsonPrimitive -> LocalizedTextNotLocalized(element.content)
            is JsonObject -> LocalizedTextMap(element.mapValues { it.value.jsonPrimitive.content })
            else -> throw SerializationException("Invalid format for LocalizedText")
        }
    }
}

class ModelJsonConverter(private val prettyPrint: Boolean) {
    val json = Json {
        prettyPrint = this@ModelJsonConverter.prettyPrint
        serializersModule = SerializersModule {
            contextual(ModelId::class, valueClassSerializer(::ModelId) { it.value })
            contextual(EntityDefId::class, valueClassSerializer(::EntityDefId) { it.value })
            contextual(AttributeDefId::class, valueClassSerializer(::AttributeDefId) { it.value })
            contextual(LocalizedText::class, LocalizedTextSerializer())
        }
    }

    fun toJson(model: Model): String {
        val modelJson = ModelJson(
            id = model.id.value,
            version = model.version.value,
            name = model.name,
            description = model.description,
            types = model.types.map { type ->
                ModelTypeJson(
                    id = type.id.value,
                    name = type.name,
                    description = type.description,
                )
            },
            relationships = model.relationshipDefs.map { rel ->
                RelationshipJson(
                    id = rel.id.value,
                    name = rel.name,
                    description = rel.description,
                    roles = rel.roles.map { role ->
                        RelationshipRoleJson(
                            id = role.id.value,
                            entityId = role.entityId.value,
                            name = role.name,
                            cardinality = role.cardinality.code
                        )
                    },
                    attributes = toAttributeJsonList(rel.attributes)

                )
            },
            entities = model.entityDefs.map { entity ->
                ModelEntityJson(
                    id = entity.id.value,
                    name = entity.name,
                    description = entity.description,
                    identifierAttribute = entity.identifierAttributeDefId.value,
                    attributes = toAttributeJsonList(entity.attributes)
                )
            }
        )
        return this.json.encodeToString(ModelJson.serializer(), modelJson)
    }



    fun fromJson(jsonString: String): ModelInMemory {
        val modelJson = this.json.decodeFromString(ModelJson.serializer(), jsonString)
        val model = ModelInMemory(
            id = ModelId(modelJson.id),
            version = ModelVersion(modelJson.version),
            name = modelJson.name,
            description = modelJson.description,
            types = modelJson.types.map { typeJson ->
                ModelTypeInMemory(
                    id = ModelTypeId(typeJson.id),
                    name = typeJson.name,
                    description = typeJson.description
                )
            },
            entityDefs = modelJson.entities.map { entityJson ->
                EntityDefInMemory(
                    id = EntityDefId(entityJson.id),
                    name = entityJson.name,
                    description = entityJson.description,
                    identifierAttributeDefId = AttributeDefId(entityJson.identifierAttribute),
                    attributes = toAttributeList(entityJson.attributes)
                )
            },
            relationshipDefs = modelJson.relationships.map { relationJson ->
                RelationshipDefInMemory(
                    id = RelationshipDefId(relationJson.id),
                    name = relationJson.name,
                    description = relationJson.description,
                    roles = relationJson.roles.map { roleJson ->
                        RelationshipRoleInMemory(
                            id = RelationshipRoleId(roleJson.id),
                            name = roleJson.name,
                            entityId = EntityDefId(roleJson.entityId),
                            cardinality = RelationshipCardinality.valueOfCode(roleJson.cardinality),
                        )
                    },
                    attributes = toAttributeList(relationJson.attributes)
                )
            }
        )
        return model
    }

    companion object {
        private fun toAttributeJsonList(attrs: Collection<AttributeDef>): List<ModelAttributeJson> {
            return attrs.map { it ->
                ModelAttributeJson(
                    id = it.id.value,
                    name = it.name,
                    description = it.description,
                    type = it.type.value,
                    optional = it.optional,
                )
            }
        }

        private fun toAttributeList(attrs: Collection<ModelAttributeJson>): List<AttributeDefInMemory> {
            return attrs.map { attributeJson ->
                AttributeDefInMemory(
                    id = AttributeDefId(attributeJson.id),
                    name = attributeJson.name,
                    description = attributeJson.description,
                    optional = attributeJson.optional,
                    type = ModelTypeId(attributeJson.type)
                )
            }
        }
    }
}

@Serializable
class ModelTypeJson(
    val id: String,
    val name: @Contextual LocalizedText? = null,
    val description: @Contextual LocalizedMarkdown? = null,
)

@Serializable
class ModelEntityJson(
    val id: String,
    val name: @Contextual LocalizedText? = null,
    val description: @Contextual LocalizedMarkdown? = null,
    val identifierAttribute: @Contextual String,
    val attributes: List<ModelAttributeJson>
)

@Serializable
class ModelAttributeJson(
    val id: String,
    val name: @Contextual LocalizedText? = null,
    val description: @Contextual LocalizedMarkdown? = null,
    val type: String,
    val optional: Boolean = false,
)

@Serializable
class RelationshipRoleJson(
    val id: String,
    val entityId: String,
    val name: @Contextual LocalizedText? = null,
    val cardinality: String
)

@Serializable
class RelationshipJson(
    val id: String,
    val name: @Contextual LocalizedText? = null,
    val description: @Contextual LocalizedMarkdown? = null,
    val roles: List<RelationshipRoleJson>,
    val attributes: List<ModelAttributeJson>
)

@Serializable
class ModelJson(
    val id: String,
    val version: String,
    val name: @Contextual LocalizedText? = null,
    val description: @Contextual LocalizedMarkdown? = null,
    val types: List<ModelTypeJson>,
    val entities: List<ModelEntityJson>,
    val relationships: List<RelationshipJson> = emptyList()
)

