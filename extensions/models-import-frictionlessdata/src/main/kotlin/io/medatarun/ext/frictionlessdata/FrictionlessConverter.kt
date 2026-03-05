package io.medatarun.ext.frictionlessdata

import io.medatarun.lang.strings.trimToNull
import io.medatarun.model.domain.*
import io.medatarun.model.infra.AttributeInMemory
import io.medatarun.model.infra.EntityInMemory
import io.medatarun.model.infra.ModelAggregateInMemory
import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.platform.kernel.ResourceLocator
import io.medatarun.tags.core.domain.TagId
import org.slf4j.LoggerFactory
import java.net.URI

interface FrictionlessTagImporter {
    fun importModelScopeTags(modelId: ModelId, keywords: List<String>): List<TagId>
}

object NoopFrictionlessTagImporter : FrictionlessTagImporter {
    override fun importModelScopeTags(modelId: ModelId, keywords: List<String>): List<TagId> {
        return emptyList()
    }
}

class FrictionlessConverter(
    private val tagImporter: FrictionlessTagImporter
) {
    val ser = DataPackageSerializer()

    fun isCompatible(path: String, resourceLocator: ResourceLocator): Boolean {
        try {
            convert(path, resourceLocator, null, null)
            return true
        } catch (e: Exception) {
            logger.error("Could not guess the file", e)
            return false
        }
    }

    fun convert(
        path: String,
        resourceLocator: ResourceLocator,
        modelKey: ModelKey?,
        modelName: String?
    ): ModelAggregate {
        val types = FrictionlessTypes().generateAll()
        val location: ResourceLocator = resourceLocator.withPath(path)
        val something = readSomething(location)
        val uri = resourceLocator.resolveUri(path)
        val model = if (something.schema != null && something.datapackage != null) {
            readAsMixedTableSchema(uri, types, something.datapackage, something.schema)
        } else if (something.datapackage != null) {
            readAsDataPackage(uri, types, something.datapackage, resourceLocator)
        } else {
            throw FrictionlessConverterUnsupportedFileFormatException(path)
        }

        val finalKey = modelKey?.value?.trimToNull()?.let { ModelKey(it) } ?: model.key
        val finalName = modelName?.trimToNull()?.let { LocalizedTextNotLocalized(it) } ?: model.name
        return model.copy(
            model = ModelInMemory.of(model.model).copy(
                key = finalKey,
                name = finalName
            ),
        )
    }

    private fun readSomething(location: ResourceLocator): ReadResult {
        val content = location.getRootContent()
        val schema = runCatching { ser.readTableSchema(content) }
            .onFailure { result -> logger.error(result.message) }
            .getOrNull()

        val datapackage = runCatching { ser.readDataPackage(content) }
            .onFailure { result -> logger.error(result.message, result) }
            .getOrNull()

        return ReadResult(datapackage, schema)

    }


    private fun readAsDataPackage(
        uri: URI,
        types: List<ModelTypeInMemory>,
        datapackage: DataPackage,
        resourceLocator: ResourceLocator
    ): ModelAggregateInMemory {
        val modelId = ModelId.generate()
        val attributesCollector = mutableListOf<AttributeInMemory>()
        val model = ModelAggregateInMemory(
            model = ModelInMemory(
                id = modelId,
                key = ModelKey(datapackage.name ?: "unknown"),
                name = datapackage.title?.let { LocalizedTextNotLocalized(it) },
                description = datapackage.description?.let { LocalizedMarkdownNotLocalized(it) },
                version = try {
                    ModelVersion(datapackage.version ?: "0.0.0").validate()
                } catch (_: Exception) {
                    ModelVersion("0.0.0")
                },
                origin = ModelOrigin.Uri(uri),
                documentationHome = toURLSafe(datapackage.homepage)
            ),
            types = types,
            entities = datapackage.resources.mapNotNull { resource ->
                val schemaOrString = resource.schema
                val subresource = getSchemaFromResource(schemaOrString, resourceLocator)
                if (subresource.schema == null) {
                    null
                } else {
                    val entityAndAttributes = toEntity(
                        uri = if (schemaOrString is StringOrTableSchema.Str) resourceLocator.resolveUri(schemaOrString.value) else uri,
                        entityKey = subresource.datapackage?.name ?: resource.name ?: "unknown",
                        entityName = subresource.datapackage?.title ?: resource.title,
                        entityDescription = subresource.datapackage?.description ?: resource.description,
                        documentationHome = subresource.datapackage?.homepage ?: resource.homepage,
                        schema = subresource.schema,
                        tags = tagImporter.importModelScopeTags(
                            modelId,
                            subresource.datapackage?.keywords ?: datapackage.keywords
                        ),
                        types = types
                    )
                    attributesCollector.addAll(entityAndAttributes.attributes)
                    entityAndAttributes.entity
                }
            },
            relationships = emptyList(),
            tags = tagImporter.importModelScopeTags(modelId, datapackage.keywords),
            attributes = attributesCollector
        )
        return model
    }


    private fun readAsMixedTableSchema(
        uri: URI,
        types: List<ModelTypeInMemory>,
        datapackage: DataPackage,
        schema: TableSchema,

        ): ModelAggregateInMemory {
        val modelId = ModelId.generate()

        val entityAndAttributes = toEntity(
            uri = uri,
            entityKey = datapackage.name ?: "unknown",
            entityName = datapackage.title,
            entityDescription = datapackage.description,
            documentationHome = datapackage.homepage,
            schema = schema,
            tags = tagImporter.importModelScopeTags(modelId, datapackage.keywords),
            types = types
        )

        val model = ModelAggregateInMemory(
            model = ModelInMemory(
                id = modelId,
                key = ModelKey(datapackage.name ?: "unknown"),
                name = datapackage.title?.let { LocalizedTextNotLocalized(it) },
                description = datapackage.description?.let { LocalizedMarkdownNotLocalized(it) },
                version = ModelVersion(datapackage.version ?: "0.0.0"),
                origin = ModelOrigin.Uri(uri),
                documentationHome = toURLSafe(datapackage.homepage),
            ),
            types = types,
            entities = listOf(entityAndAttributes.entity),
            relationships = emptyList(),
            attributes = entityAndAttributes.attributes,
            tags = tagImporter.importModelScopeTags(modelId, datapackage.keywords),
        )
        return model
    }

    fun toURLSafe(str: String?) = runCatching { str?.let { URI(it).normalize().toURL() } }.getOrNull()

    data class EntityAndAttributes(val entity: EntityInMemory, val attributes: List<AttributeInMemory>)

    private fun toEntity(
        uri: URI,
        entityKey: String,
        entityName: String?,
        entityDescription: String?,
        documentationHome: String?,
        schema: TableSchema,
        tags: List<TagId>,
        types: List<ModelTypeInMemory>,
    ): EntityAndAttributes {

        val pk = schema.primaryKey?.values?.joinToString("__")
            ?: schema.fields.firstOrNull()?.name
            ?: "empty"

        val entityId = EntityId.generate()

        val attributes = schema.fields.map { field ->
            AttributeInMemory(
                id = AttributeId.generate(),
                key = AttributeKey(field.name),
                name = field.title?.let(::LocalizedTextNotLocalized),
                description = field.description?.let(::LocalizedMarkdownNotLocalized),
                typeId = types.firstOrNull { it.key == TypeKey(field.type) }?.id
                    ?: throw FrictionlessConverterTypeNotFound(field.name, field.type),
                optional = field.isOptional(),
                tags = emptyList(),
                ownerId = AttributeOwnerId.OwnerEntityId(entityId)
            )
        }

        val identifierAttribute = attributes.firstOrNull { it.key.value == pk }
        if (identifierAttribute == null) throw FrictionlessConverterEntityIdentifierNotFound(entityKey, pk)

        val entity = EntityInMemory(
            id = entityId,
            key = EntityKey(entityKey),
            name = entityName?.let(::LocalizedTextNotLocalized),
            description = entityDescription?.let(::LocalizedMarkdownNotLocalized),
            origin = EntityOrigin.Uri(uri),
            documentationHome = toURLSafe(documentationHome),
            tags = tags,
            identifierAttributeId = identifierAttribute.id
        )
        return EntityAndAttributes(entity, attributes)
    }

    /**
     * Follow string or schema embedded in DataResource
     */
    private fun getSchemaFromResource(
        schemaOrString: StringOrTableSchema?,
        resourceLocator: ResourceLocator
    ): ReadResult {
        return when (schemaOrString) {
            null -> ReadResult(null, null)
            is StringOrTableSchema.Str -> {
                readSomething(resourceLocator.withPath(schemaOrString.value))
            }

            is StringOrTableSchema.Schema -> {
                ReadResult(null, schemaOrString.value)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FrictionlessConverter::class.java)
    }
}

private class ReadResult(
    val datapackage: DataPackage?,
    val schema: TableSchema?,
)
