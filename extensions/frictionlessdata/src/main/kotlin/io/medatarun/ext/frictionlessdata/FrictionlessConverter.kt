package io.medatarun.ext.frictionlessdata

import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.model.model.*
import io.medatarun.model.ports.ResourceLocator

class FrictionlessConverter() {
    val ser = DataPackageSerializer()
    fun readString(path: String, resourceLocator: ResourceLocator): Model {
        val types = FrictionlessTypes().all
        val location: ResourceLocator = resourceLocator.withPath(path)
        val something = readSomething(location)
        if (something.schema != null && something.datapackage != null) {
            return readAsMixedTableSchema(types, something.datapackage, something.schema)
        } else if (something.datapackage != null) {
            return readAsDataPackage(types, something.datapackage, resourceLocator)
        }

        throw FrictionlessConverterUnsupportedFileFormatException(path)
    }

    private fun readSomething(location: ResourceLocator): ReadResult {
        val content = location.getRootContent()
        val schema = runCatching { ser.readTableSchema(content) }
            .also { result -> println(result.exceptionOrNull()) }
            .getOrNull()

        val datapackage = runCatching { ser.readDataPackage(content) }
            .also { result -> println(result.exceptionOrNull()) }
            .getOrNull()

        return ReadResult(datapackage, schema)

    }

    class ReadResult(
        val datapackage: DataPackage?,
        val schema: TableSchema?,
    )

    private fun readAsDataPackage(
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
}


