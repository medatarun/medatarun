package io.medatarun.ext.frictionlessdata

import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.model.model.*
import io.medatarun.model.ports.ResourceLocator
import org.slf4j.LoggerFactory
import java.net.URI

class FrictionlessConverter() {
    val ser = DataPackageSerializer()
    fun readString(path: String, resourceLocator: ResourceLocator): Model {
        val types = FrictionlessTypes().all
        val location: ResourceLocator = resourceLocator.withPath(path)
        val something = readSomething(location)
        val uri = resourceLocator.resolveUri(path)
        if (something.schema != null && something.datapackage != null) {
            return readAsMixedTableSchema(uri, types, something.datapackage, something.schema)
        } else if (something.datapackage != null) {
            return readAsDataPackage(uri, types, something.datapackage, resourceLocator)
        }

        throw FrictionlessConverterUnsupportedFileFormatException(path)
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
        uri : URI,
        types: List<ModelTypeInMemory>,
        datapackage: DataPackage,
        resourceLocator: ResourceLocator
    ): Model {
        val model = ModelInMemory(
            id = ModelId(datapackage.name ?: "unknown"),
            name = datapackage.title?.let { LocalizedTextNotLocalized(it) },
            description = datapackage.description?.let { LocalizedTextNotLocalized(it) },
            version = ModelVersion(datapackage.version ?: "0.0.0"),
            types = types,
            entityDefs = datapackage.resources.mapNotNull { resource ->
                val schemaOrString = resource.schema
                val subresource = getSchemaFromResource(schemaOrString, resourceLocator)
                if (subresource.schema == null) null else toEntity(
                    uri = if (schemaOrString is StringOrTableSchema.Str) resourceLocator.resolveUri(schemaOrString.value) else uri,
                    entityId = subresource.datapackage?.name ?: resource.name ?: "unknown",
                    entityName = subresource.datapackage?.title?: resource.title,
                    entityDescription = subresource.datapackage?.description ?: resource.description,
                    schema = subresource.schema
                )
            },
            relationshipDefs = emptyList()
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
            id = ModelId(datapackage.name ?: "unknown"),
            name = datapackage.title?.let { LocalizedTextNotLocalized(it) },
            description = datapackage.description?.let { LocalizedTextNotLocalized(it) },
            version = ModelVersion(datapackage.version ?: "0.0.0"),
            types = types,
            entityDefs = listOf(
                toEntity(
                    uri = uri,
                    entityId = datapackage.name ?: "unknown",
                    entityName = datapackage.title,
                    entityDescription = datapackage.description,
                    schema = schema
                )
            ),
            relationshipDefs = emptyList(),
        )
        return model
    }


    private fun toEntity(
        uri: URI,
        entityId: String,
        entityName: String?,
        entityDescription: String?,
        schema: TableSchema
    ): EntityDefInMemory {
        val entity = EntityDefInMemory(
            id = EntityDefId(entityId),
            name = entityName?.let(::LocalizedTextNotLocalized),
            description = entityDescription?.let(::LocalizedTextNotLocalized),
            attributes = schema.fields.map { field ->
                AttributeDefInMemory(
                    id = AttributeDefId(field.name),
                    name = field.title?.let(::LocalizedTextNotLocalized),
                    description = field.description?.let(::LocalizedTextNotLocalized),
                    type = ModelTypeId(field.type),
                    optional = field.isOptional(),
                )
            },
            origin = EntityOrigin.Uri(uri),
            identifierAttributeDefId = AttributeDefId(
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
