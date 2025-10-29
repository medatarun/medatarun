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
            return readAsDataPackage(types, datapackage)
        }

        throw FrictionlessConverterUnsupportedFileFormatException(path)
    }

    private fun readAsDataPackage(
        types: List<ModelTypeInMemory>,
        datapackage: DataPackage
    ): Model {
        val model = ModelInMemory(
            id = ModelId(datapackage.name ?: "unknown"),
            name = datapackage.title?.let { LocalizedTextNotLocalized(it) },
            description = datapackage.description?.let { LocalizedTextNotLocalized(it) },
            version = ModelVersion(datapackage.version ?: "0.0.0"),
            types = types,
            entityDefs = datapackage.resources.mapNotNull { resource ->
                val schemaOrString = resource.schema
                when (schemaOrString) {
                    null -> null
                    is StringOrTableSchema.Str -> {
                        throw DatapackageResourceSchemaAsPathNotSupported()
                    }
                    is StringOrTableSchema.Schema -> {
                        val schema = schemaOrString.value
                        EntityDefInMemory(
                            id =  EntityDefId("unknown"),  // TODO
                            name = null,  // TODO
                            attributes = schema.fields.map { field ->
                                AttributeDefInMemory(
                                    id = AttributeDefId(field.name),
                                    name = field.title?.let(::LocalizedTextNotLocalized),
                                    description = field.description?.let(::LocalizedTextNotLocalized),
                                    type = ModelTypeId(field.type),
                                    optional = field.isOptional(), // TODO
                                )
                            },
                            description = null, // TODO
                            identifierAttributeDefId = AttributeDefId(
                                schema.primaryKey?.values?.joinToString(";") ?: ""
                            ), // TODO
                        )
                    }
                }

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
                EntityDefInMemory(
                    id = EntityDefId("unknown"),  // TODO
                    name = null,  // TODO
                    attributes = schema.fields.map { field ->
                        AttributeDefInMemory(
                            id = AttributeDefId(field.name),
                            name = field.title?.let(::LocalizedTextNotLocalized),
                            description = field.description?.let(::LocalizedTextNotLocalized),
                            type = ModelTypeId(field.type),
                            optional = field.isOptional(), // TODO
                        )
                    },
                    description = null, // TODO
                    identifierAttributeDefId = AttributeDefId(
                        schema.primaryKey?.values?.joinToString(";") ?: ""
                    ), // TODO
                )
            ),
            relationshipDefs = emptyList(),
        )
        return model
    }
}