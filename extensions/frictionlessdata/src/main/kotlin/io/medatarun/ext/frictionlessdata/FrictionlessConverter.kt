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
        val schema = runCatching { ser.readTableSchema(location.getRootContent()) }
            .also { result -> println(result.exceptionOrNull()) }
            .getOrNull()

        val datapackage = runCatching { ser.readDataPackage(location.getRootContent()) }
            .also { result -> println(result.exceptionOrNull()) }
            .getOrNull()

        if (schema != null && datapackage != null) {
            return readAsMixedTableSchema(types, datapackage, schema)
        } else if (datapackage != null) {
            return readAsDataPackage(types, datapackage, resourceLocator)
        }

        throw FrictionlessConverterUnsupportedFileFormatException(path)
    }

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
                val schema = getSchemaFromResource(schemaOrString, resourceLocator)
                if (schema == null) null else toEntity(
                    entityId = resource.name ?: "unknown",
                    entityName = resource.title,
                    entityDescription = resource.description,
                    schema = schema
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
                schema.primaryKey?.values?.joinToString(";") ?: ""
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
    ): TableSchema? {
        val schema = when (schemaOrString) {
            null -> null
            is StringOrTableSchema.Str -> {
                val nextJsonStr = resourceLocator.getContent(schemaOrString.value)
                val nextSchema = runCatching { ser.readTableSchema(nextJsonStr) }
                    .also { result -> println(result.exceptionOrNull()) }
                    .getOrNull()
                nextSchema
            }

            is StringOrTableSchema.Schema -> {
                schemaOrString.value

            }
        }
        return schema
    }
}


