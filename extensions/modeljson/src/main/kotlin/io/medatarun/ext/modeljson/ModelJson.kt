package io.medatarun.ext.modeljson

import io.medatarun.model.domain.*
import io.medatarun.model.infra.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import org.intellij.lang.annotations.Language
import java.net.URI


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
            contextual(ModelKey::class, valueClassSerializer(::ModelKey) { it.value })
            contextual(EntityKey::class, valueClassSerializer(::EntityKey) { it.value })
            contextual(AttributeKey::class, valueClassSerializer(::AttributeKey) { it.value })
            contextual(LocalizedText::class, LocalizedTextSerializer())
        }
    }

    fun toJson(model: Model): String {
        val modelJson = ModelJson(
            id = model.id.value,
            schema = ModelJsonSchemas.current(),
            version = model.version.value,
            name = model.name,
            description = model.description,
            origin = toModelOriginStr(model.origin),
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
                    attributes = toAttributeJsonList(rel.attributes),
                    hashtags = rel.hashtags.map { it.value }

                )
            },
            entities = model.entityDefs.map { entity ->
                ModelEntityJson(
                    id = entity.id.value,
                    name = entity.name,
                    description = entity.description,
                    identifierAttribute = entity.identifierAttributeKey.value,
                    origin = toEntityOriginStr(entity.origin),
                    attributes = toAttributeJsonList(entity.attributes),
                    documentationHome = entity.documentationHome?.toExternalForm(),
                    hashtags = entity.hashtags.map { it.value }
                )
            },
            documentationHome = model.documentationHome?.toExternalForm(),
            hashtags = model.hashtags.map { it.value }
        )
        return this.json.encodeToString(ModelJson.serializer(), modelJson)
    }

    fun toEntityOriginStr(origin: EntityOrigin): String? {
        val originStr = when (origin) {
            null -> null
            is EntityOrigin.Manual -> null
            is EntityOrigin.Uri -> origin.uri.toString()
        }
        return originStr
    }
    fun toModelOriginStr(origin: ModelOrigin): String? {
        val originStr = when (origin) {
            null -> null
            is ModelOrigin.Manual -> null
            is ModelOrigin.Uri -> origin.uri.toString()
        }
        return originStr
    }


    fun fromJson(@Language("json") jsonString: String): ModelInMemory {
        val modelJson = this.json.decodeFromString(ModelJson.serializer(), jsonString)
        val model = ModelInMemory(
            id = ModelKey(modelJson.id),
            version = ModelVersion(modelJson.version),
            origin = when(modelJson.origin) {
                null -> ModelOrigin.Manual
                    else -> ModelOrigin.Uri(URI(modelJson.origin))
            },
            name = modelJson.name,
            description = modelJson.description,
            types = modelJson.types.map { typeJson ->
                ModelTypeInMemory(
                    id = TypeKey(typeJson.id),
                    name = typeJson.name,
                    description = typeJson.description
                )
            },
            entityDefs = modelJson.entities.map { entityJson ->
                EntityDefInMemory(
                    id = EntityKey(entityJson.id),
                    name = entityJson.name,
                    description = entityJson.description,
                    identifierAttributeKey = AttributeKey(entityJson.identifierAttribute),
                    origin = when (entityJson.origin) {
                        null -> EntityOrigin.Manual
                        else -> EntityOrigin.Uri(URI(entityJson.origin))
                    },
                    attributes = toAttributeList(entityJson.attributes),
                    documentationHome = entityJson.documentationHome?.let { URI(it).toURL() },
                    hashtags = entityJson.hashtags?.map { Hashtag(it) } ?: emptyList()
                )
            },
            relationshipDefs = modelJson.relationships.map { relationJson ->
                return@map RelationshipDefInMemory(
                    id = RelationshipKey(relationJson.id),
                    name = relationJson.name,
                    description = relationJson.description,
                    roles = relationJson.roles.map { roleJson ->
                        RelationshipRoleInMemory(
                            id = RelationshipRoleId(roleJson.id),
                            name = roleJson.name,
                            entityId = EntityKey(roleJson.entityId),
                            cardinality = RelationshipCardinality.valueOfCode(roleJson.cardinality),
                        )
                    },
                    attributes = toAttributeList(relationJson.attributes),
                    hashtags = relationJson.hashtags?.map { Hashtag(it) } ?: emptyList()
                )
            },
            documentationHome = modelJson.documentationHome?.let { URI(it).toURL() },
            hashtags = modelJson.hashtags?.map { Hashtag(it) } ?: emptyList()
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
                    hashtags = it.hashtags.map { it.value }
                )
            }
        }

        private fun toAttributeList(attrs: Collection<ModelAttributeJson>): List<AttributeDefInMemory> {
            return attrs.map { attributeJson ->
                AttributeDefInMemory(
                    id = AttributeKey(attributeJson.id),
                    name = attributeJson.name,
                    description = attributeJson.description,
                    optional = attributeJson.optional,
                    type = TypeKey(attributeJson.type),
                    hashtags = attributeJson.hashtags?.map { Hashtag(it) } ?: emptyList()
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
    val origin: String? = null,
    val hashtags: List<String>? = emptyList(),
    val attributes: List<ModelAttributeJson>,
    val documentationHome: String? = null,

)

@Serializable
class ModelAttributeJson(
    val id: String,
    val name: @Contextual LocalizedText? = null,
    val description: @Contextual LocalizedMarkdown? = null,
    val type: String,
    val optional: Boolean = false,
    val hashtags: List<String>? = emptyList()
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
    val attributes: List<ModelAttributeJson>,
    val hashtags: List<String>? = emptyList()
)

@Serializable
class ModelJson(
    val id: String,
    @SerialName($$"$schema")
    val schema: String,
    val version: String,
    val name: @Contextual LocalizedText? = null,
    val description: @Contextual LocalizedMarkdown? = null,
    val origin: String? = null,
    val hashtags: List<String>? = emptyList(),
    val types: List<ModelTypeJson>,
    val entities: List<ModelEntityJson>,
    val relationships: List<RelationshipJson> = emptyList(),
    val documentationHome: String? = null,

)

