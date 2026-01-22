package io.medatarun.ext.frictionlessdata

import io.medatarun.lang.strings.trimToNull
import io.medatarun.model.domain.*
import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.platform.kernel.ResourceLocator
import org.slf4j.LoggerFactory
import java.net.URI

class FrictionlessConverter {
    val ser = DataPackageSerializer()

    fun isCompatible(path: String, resourceLocator: ResourceLocator): Boolean {
        try {
            readString(path, resourceLocator, null, null)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun readString(path: String, resourceLocator: ResourceLocator, modelKey: ModelKey?, modelName: String?): Model {
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

        val finalKey = modelKey?.value?.trimToNull()?.let{ ModelKey(it) } ?: model.key
        val finalName = modelName?.trimToNull()?.let { LocalizedTextNotLocalized(it) } ?: model.name
        return model.copy(
            key = finalKey,
            name = finalName
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
    ): ModelInMemory {
        val model = ModelInMemory(
            id = ModelId.generate(),
            key = ModelKey(datapackage.name ?: "unknown"),
            name = datapackage.title?.let { LocalizedTextNotLocalized(it) },
            description = datapackage.description?.let { LocalizedMarkdownNotLocalized(it) },
            version = try {
                ModelVersion(datapackage.version ?: "0.0.0").validate()
            } catch (_:Exception) {
                ModelVersion("0.0.0")
            },
            origin = ModelOrigin.Uri(uri),
            types = types,
            entityDefs = datapackage.resources.mapNotNull { resource ->
                val schemaOrString = resource.schema
                val subresource = getSchemaFromResource(schemaOrString, resourceLocator)
                if (subresource.schema == null) null else toEntity(
                    uri = if (schemaOrString is StringOrTableSchema.Str) resourceLocator.resolveUri(schemaOrString.value) else uri,
                    entityId = subresource.datapackage?.name ?: resource.name ?: "unknown",
                    entityName = subresource.datapackage?.title ?: resource.title,
                    entityDescription = subresource.datapackage?.description ?: resource.description,
                    documentationHome = subresource.datapackage?.homepage ?: resource.homepage,
                    schema = subresource.schema,
                    hashtags = (subresource.datapackage?.keywords ?: datapackage.keywords).map { Hashtag(it) },
                )
            },
            relationshipDefs = emptyList(),
            hashtags = datapackage.keywords.map { Hashtag(it) },
            documentationHome = toURLSafe(datapackage.homepage)
        )
        return model
    }


    private fun readAsMixedTableSchema(
        uri: URI,
        types: List<ModelTypeInMemory>,
        datapackage: DataPackage,
        schema: TableSchema,

        ): ModelInMemory {


        val model = ModelInMemory(
            id = ModelId.generate(),
            key = ModelKey(datapackage.name ?: "unknown"),
            name = datapackage.title?.let { LocalizedTextNotLocalized(it) },
            description = datapackage.description?.let { LocalizedMarkdownNotLocalized(it) },
            version = ModelVersion(datapackage.version ?: "0.0.0"),
            types = types,
            origin = ModelOrigin.Uri(uri),
            entityDefs = listOf(
                toEntity(
                    uri = uri,
                    entityId = datapackage.name ?: "unknown",
                    entityName = datapackage.title,
                    entityDescription = datapackage.description,
                    documentationHome = datapackage.homepage,
                    schema = schema,
                    hashtags = datapackage.keywords.map { Hashtag(it) }
                )
            ),
            relationshipDefs = emptyList(),
            documentationHome = toURLSafe(datapackage.homepage),
            hashtags = datapackage.keywords.map { Hashtag(it) },
        )
        return model
    }

    fun toURLSafe(str: String?) = runCatching { str?.let { URI(it).normalize().toURL() } }.getOrNull()

    private fun toEntity(
        uri: URI,
        entityId: String,
        entityName: String?,
        entityDescription: String?,
        documentationHome: String?,
        schema: TableSchema,
        hashtags: List<Hashtag>
    ): EntityDefInMemory {
        val entity = EntityDefInMemory(
            id = EntityId.generate(),
            key = EntityKey(entityId),
            name = entityName?.let(::LocalizedTextNotLocalized),
            description = entityDescription?.let(::LocalizedMarkdownNotLocalized),
            attributes = schema.fields.map { field ->
                AttributeDefInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey(field.name),
                    name = field.title?.let(::LocalizedTextNotLocalized),
                    description = field.description?.let(::LocalizedMarkdownNotLocalized),
                    type = TypeKey(field.type),
                    optional = field.isOptional(),
                    hashtags = emptyList()
                )
            },
            origin = EntityOrigin.Uri(uri),
            documentationHome = toURLSafe(documentationHome),
            hashtags = hashtags,
            identifierAttributeKey = AttributeKey(
                schema.primaryKey?.values?.joinToString(";")
                    ?: schema.fields.firstOrNull()?.name
                    ?: "unknown"
            ), // TODO
        )
        return entity
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
