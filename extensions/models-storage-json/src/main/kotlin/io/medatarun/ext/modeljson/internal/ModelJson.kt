package io.medatarun.ext.modeljson.internal

import io.medatarun.ext.modeljson.ModelJsonSchemas
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


internal inline fun <reified T> valueClassSerializer(
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

internal class LocalizedTextSerializer : KSerializer<LocalizedText> {
    override val descriptor = JsonElement.serializer().descriptor

    override fun serialize(encoder: Encoder, value: LocalizedText) {
        val json = if (!value.isLocalized)
            JsonPrimitive(value.name)
        else
            JsonObject(value.all().mapValues { JsonPrimitive(it.value) })
        encoder.encodeSerializableValue(JsonElement.serializer(), json)
    }

    override fun deserialize(decoder: Decoder): LocalizedText {
        return when (val element = decoder.decodeSerializableValue(JsonElement.serializer())) {
            is JsonPrimitive -> LocalizedTextNotLocalized(element.content)
            is JsonObject -> LocalizedTextMap(element.mapValues { it.value.jsonPrimitive.content })
            else -> throw SerializationException("Invalid format for LocalizedText")
        }
    }
}

internal class LocalizedMarkdownSerializer : KSerializer<LocalizedMarkdown> {
    override val descriptor = JsonElement.serializer().descriptor

    override fun serialize(encoder: Encoder, value: LocalizedMarkdown) {
        val json = if (!value.isLocalized)
            JsonPrimitive(value.name)
        else
            JsonObject(value.all().mapValues { JsonPrimitive(it.value) })
        encoder.encodeSerializableValue(JsonElement.serializer(), json)
    }

    override fun deserialize(decoder: Decoder): LocalizedMarkdown {
        return when (val element = decoder.decodeSerializableValue(JsonElement.serializer())) {
            is JsonPrimitive -> LocalizedMarkdownNotLocalized(element.content)
            is JsonObject -> LocalizedMarkdownMap(element.mapValues { it.value.jsonPrimitive.content })
            else -> throw SerializationException("Invalid format for LocalizedText")
        }
    }
}

internal class ModelJsonConverter(private val prettyPrint: Boolean) {
    val json = Json {
        prettyPrint = this@ModelJsonConverter.prettyPrint
        serializersModule = SerializersModule {
            contextual(ModelKey::class, valueClassSerializer(::ModelKey) { it.value })
            contextual(EntityKey::class, valueClassSerializer(::EntityKey) { it.value })
            contextual(AttributeKey::class, valueClassSerializer(::AttributeKey) { it.value })
            contextual(LocalizedText::class, LocalizedTextSerializer())
            contextual(LocalizedMarkdown::class, LocalizedMarkdownSerializer())
        }
    }

    fun toModelJson(model: Model): ModelJson {
        val modelJson = ModelJson(
            id = model.id.value.toString(),
            key = model.key.value,
            schema = ModelJsonSchemas.current(),
            version = model.version.value,
            name = model.name,
            description = model.description,
            origin = toModelOriginStr(model.origin),
            types = model.types.map { type ->
                ModelTypeJson(
                    id = type.id.value.toString(),
                    key = type.key.value,
                    name = type.name,
                    description = type.description,
                )
            },
            relationships = model.relationships.map { rel ->
                RelationshipJson(
                    id = rel.id.value.toString(),
                    key = rel.key.value,
                    name = rel.name,
                    description = rel.description,
                    roles = rel.roles.map { role ->
                        RelationshipRoleJson(
                            id = role.id.value.toString(),
                            key = role.key.value,
                            entityId = model.findEntity(role.entityId).key.value,
                            name = role.name,
                            cardinality = role.cardinality.code
                        )
                    },
                    attributes = toAttributeJsonList(model, rel.attributes),
                    hashtags = rel.hashtags.map { it.value }

                )
            },
            entities = model.entities.map { entity ->
                val attributesJson = toAttributeJsonList(model, entity.attributes)
                val attributeKey = entity.attributes
                    .firstOrNull { attribute -> entity.identifierAttributeId == attribute.id }
                    ?.key ?: throw ModelJsonWriterEntityIdentifierAttributeNotFoundInAttributes(entity.id, entity.identifierAttributeId)
                ModelEntityJson(
                    id = entity.id.value.toString(),
                    key = entity.key.value,
                    name = entity.name,
                    description = entity.description,
                    identifierAttribute = attributeKey.value,
                    origin = toEntityOriginStr(entity.origin),
                    attributes = attributesJson,
                    documentationHome = entity.documentationHome?.toExternalForm(),
                    hashtags = entity.hashtags.map { it.value }
                )
            },
            documentationHome = model.documentationHome?.toExternalForm(),
            hashtags = model.hashtags.map { it.value }
        )
        return modelJson
    }

    fun toJsonString(model: Model): String {
        val modelJson = toModelJson(model)
        return this.json.encodeToString(ModelJson.serializer(), modelJson)
    }

    fun toJsonObject(model: Model): JsonObject {
        val modelJson = toModelJson(model)
        return this.json.encodeToJsonElement(ModelJson.serializer(), modelJson).jsonObject
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
        val types = modelJson.types.map { typeJson ->
            ModelTypeInMemory(
                id = typeJson.id?.let { TypeId.fromString(it) } ?: TypeId.generate(),
                key = TypeKey(typeJson.key),
                name = typeJson.name,
                description = typeJson.description
            )
        }
        val entities = modelJson.entities.map { entityJson -> toEntity(types, entityJson) }

        fun findEntity(relationJsonKey: String, roleJsonKey: String, entityKey: EntityKey) = entities
            .firstOrNull { it.key == entityKey }
            ?: throw ModelJsonReadEntityReferencedInRelationshipNotFound(relationJsonKey, roleJsonKey, entityKey.value)

        val model = ModelInMemory(
            id = modelJson.id?.let { ModelId.fromString(it) } ?: ModelId.generate(),
            key = ModelKey(modelJson.key),
            version = ModelVersion(modelJson.version),
            origin = when (modelJson.origin) {
                null -> ModelOrigin.Manual
                else -> ModelOrigin.Uri(URI(modelJson.origin))
            },
            name = modelJson.name,
            description = modelJson.description,
            types = types,
            entities = entities,
            relationships = modelJson.relationships.map { relationJson ->
                return@map RelationshipInMemory(
                    id = relationJson.id?.let { RelationshipId.fromString(it) } ?: RelationshipId.generate(),
                    key = RelationshipKey(relationJson.key),
                    name = relationJson.name,
                    description = relationJson.description,
                    roles = relationJson.roles.map { roleJson ->
                        RelationshipRoleInMemory(
                            id = roleJson.id?.let { RelationshipRoleId.fromString(it) } ?: RelationshipRoleId.generate(),
                            key = RelationshipRoleKey(roleJson.key),
                            name = roleJson.name,
                            entityId = findEntity(relationJson.key, roleJson.key, EntityKey(roleJson.entityId)).id,
                            cardinality = RelationshipCardinality.valueOfCode(roleJson.cardinality),
                        )
                    },
                    attributes = toAttributeList(types, relationJson.attributes),
                    hashtags = relationJson.hashtags?.map { Hashtag(it) } ?: emptyList()
                )
            },
            documentationHome = modelJson.documentationHome?.let { URI(it).toURL() },
            hashtags = modelJson.hashtags?.map { Hashtag(it) } ?: emptyList()
        )
        return model
    }

    private fun toEntity(types: List<ModelType>, entityJson: ModelEntityJson): EntityInMemory {
        val attributes = toAttributeList(types, entityJson.attributes)
        val identifierAttribute = attributes
            .firstOrNull { it.key == AttributeKey(entityJson.identifierAttribute)}
            ?: throw ModelJsonEntityIdentifierAttributeNotFound(entityJson.key)
        return EntityInMemory(
            id = entityJson.id?.let { EntityId.fromString(it) } ?: EntityId.generate(),
            key = EntityKey(entityJson.key),
            name = entityJson.name,
            description = entityJson.description,
            identifierAttributeId = identifierAttribute.id,
            origin = when (entityJson.origin) {
                null -> EntityOrigin.Manual
                else -> EntityOrigin.Uri(URI(entityJson.origin))
            },
            attributes = attributes,
            documentationHome = entityJson.documentationHome?.let { URI(it).toURL() },
            hashtags = entityJson.hashtags?.map { Hashtag(it) } ?: emptyList()
        )
    }

    companion object {
        private fun toAttributeJsonList(model: Model, attrs: Collection<Attribute>): List<ModelAttributeJson> {
            return attrs.map { it ->
                ModelAttributeJson(
                    id = it.id.value.toString(),
                    key = it.key.value,
                    name = it.name,
                    description = it.description,
                    type = model.findType(TypeRef.ById(it.typeId)).key.value,
                    optional = it.optional,
                    hashtags = it.hashtags.map { it.value }
                )
            }
        }

        private fun toAttributeList(types: List<ModelType>, attrs: Collection<ModelAttributeJson>): List<AttributeInMemory> {

            return attrs.map { attributeJson ->

                val type = types.firstOrNull { t -> t.key == TypeKey(attributeJson.type) }
                    ?: throw ModelJsonEntityAttributeTypeNotFoundException(attributeJson.key, attributeJson.type)

                AttributeInMemory(
                    id = attributeJson.id?.let { AttributeId.fromString(it) } ?: AttributeId.generate(),
                    key = AttributeKey(attributeJson.key),
                    name = attributeJson.name,
                    description = attributeJson.description,
                    optional = attributeJson.optional,
                    typeId = type.id,
                    hashtags = attributeJson.hashtags?.map { Hashtag(it) } ?: emptyList()
                )
            }
        }
    }
}

@Serializable
internal class ModelTypeJson(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val name: @Contextual LocalizedText? = null,
    val description: @Contextual LocalizedMarkdown? = null,
)

@Serializable
internal class ModelEntityJson(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val name: @Contextual LocalizedText? = null,
    val description: @Contextual LocalizedMarkdown? = null,
    val identifierAttribute: @Contextual String,
    val origin: String? = null,
    val hashtags: List<String>? = emptyList(),
    val attributes: List<ModelAttributeJson>,
    val documentationHome: String? = null,

    )

@Serializable
internal class ModelAttributeJson(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val name: @Contextual LocalizedText? = null,
    val description: @Contextual LocalizedMarkdown? = null,
    val type: String,
    val optional: Boolean = false,
    val hashtags: List<String>? = emptyList()
)

@Serializable
internal class RelationshipRoleJson(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val entityId: String,
    val name: @Contextual LocalizedText? = null,
    val cardinality: String
)

@Serializable
internal class RelationshipJson(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val name: @Contextual LocalizedText? = null,
    val description: @Contextual LocalizedMarkdown? = null,
    val roles: List<RelationshipRoleJson>,
    val attributes: List<ModelAttributeJson>,
    val hashtags: List<String>? = emptyList()
)

@Serializable
internal class ModelJson(
    /** Note that for imports, id may be null or missing.*/
    val id: String? = null,
    val key: String,
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

