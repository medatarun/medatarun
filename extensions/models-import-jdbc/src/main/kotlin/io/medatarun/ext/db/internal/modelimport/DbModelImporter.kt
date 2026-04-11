package io.medatarun.ext.db.internal.modelimport

import io.medatarun.ext.db.internal.connection.DbConnectionRegistry
import io.medatarun.ext.db.domain.DbDriverManager
import io.medatarun.ext.db.domain.DbConnectionNotFoundException
import io.medatarun.ext.db.domain.DbImportCouldNotFindAttributeFromPrimaryKeyException
import io.medatarun.ext.db.domain.DbImportCouldNotFindEntityForRelationship
import io.medatarun.ext.db.domain.DbImportTypeNotFoundException
import io.medatarun.lang.strings.trimToNull
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.model.domain.*
import io.medatarun.model.infra.*
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.model.ports.needs.ModelImporterData
import io.medatarun.platform.kernel.ResourceLocator
import java.net.URI
import java.time.Instant
import java.time.ZoneId

class DbModelImporter(dbDriverManager: DbDriverManager, val dbConnectionRegistry: DbConnectionRegistry) :
    ModelImporter {

    private val introspect = DbIntrospect(dbDriverManager)

    override fun accept(
        path: String,
        resourceLocator: ResourceLocator
    ): Boolean {
        return path.startsWith("datasource:")
    }


    override fun toModel(
        path: String,
        resourceLocator: ResourceLocator,
        modelKey: ModelKey?,
        modelName: String?
    ): ModelImporterData {
        val connectionName = path.split(":").last()
        val connection = dbConnectionRegistry.findByNameOptional(connectionName)
            ?: throw DbConnectionNotFoundException(connectionName)
        val result: IntrospectResult = introspect.introspect(connection)
        val date = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime()
        val modelKeyOrGenerated = modelKey?.value?.trimToNull() ?: (connection.name + "-" + UuidUtils.generateV4String())
        val modelNameOrGenerated = modelName?.trimToNull() ?: "${connection.name} (import $date)"
        val types = result.types().map { ModelTypeInMemory(TypeId.generate(), TypeKey(it), null, null) }
        val attributesCollector = mutableListOf<AttributeInMemory>()
        val entities = result.tables.map { table ->
            val entityId = EntityId.generate()
            val attributeFromColumns = table.columns.map {
                val type = types.firstOrNull { type -> type.key == TypeKey(it.typeName) }
                    ?: throw DbImportTypeNotFoundException(table.tableName, it.typeName)
                AttributeInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey(it.columnName),
                    name = null,
                    description = it.remarks?.let(::LocalizedMarkdownNotLocalized),
                    typeId = type.id,
                    optional = it.isNullable != false,
                    tags = emptyList(),
                    ownerId = AttributeOwnerId.OwnerEntityId(entityId)
                )
            }
            val pkAttributeKey = table.pkNameOrFirstColumn()
            val pkAttribute = attributeFromColumns.firstOrNull { it.key == pkAttributeKey }
                ?: throw DbImportCouldNotFindAttributeFromPrimaryKeyException(table.tableName, pkAttributeKey.value)

            attributesCollector.addAll(attributeFromColumns)

            EntityInMemory(
                id = entityId,
                key = EntityKey(table.tableName),
                name = null,
                description = table.remarks?.let(::LocalizedMarkdownNotLocalized),
                identifierAttributeId = pkAttribute.id,
                origin = EntityOrigin.Uri(URI(path)),
                documentationHome = null,
                tags = emptyList()

            )
        }

        fun findEntityByTableName(tableName: String) = entities
            .firstOrNull { it.key == EntityKey(tableName) }
            ?: throw DbImportCouldNotFindEntityForRelationship(tableName)

        val relationships = mutableListOf<RelationshipInMemory>()

        var duplicateCount = 0
        for (table in result.tables) {
            for (fk in table.foreignKeys) {
                val key = fk.fkName ?: "${fk.pkTableName}.${fk.pkColumnName}__${fk.fkTableName}.${fk.fkColumnName}"
                val roles = listOf(
                    RelationshipRoleInMemory(
                        id = RelationshipRoleId.generate(),
                        key = RelationshipRoleKey("${fk.fkTableName}.${fk.fkColumnName}"),
                        entityId = findEntityByTableName(fk.fkTableName).id,
                        name = null,
                        cardinality = if (result.isNullableOrUndefined(
                                fk.fkTableName,
                                fk.fkColumnName
                            )
                        ) RelationshipCardinality.ZeroOrOne else RelationshipCardinality.One,
                    ),
                    RelationshipRoleInMemory(
                        id = RelationshipRoleId.generate(),
                        key = RelationshipRoleKey("${fk.pkTableName}.${fk.pkColumnName}"),
                        cardinality = RelationshipCardinality.Unknown,
                        entityId = findEntityByTableName(fk.pkTableName).id,
                        name = null
                    ),
                )
                val fkKeySafe = if (relationships.any { it.key.value == key }) {
                        key + "_" + (duplicateCount++)
                } else key
                relationships.add(RelationshipInMemory(
                    id = RelationshipId.generate(),
                    key = RelationshipKey(fkKeySafe),
                    name = null,
                    description = null,
                    roles = roles,
                    tags = emptyList(),
                ))
            }
        }

        val model = ModelAggregateInMemory(
            model = ModelInMemory(
                id = ModelId.generate(),
                key = ModelKey(modelKeyOrGenerated),
                name = LocalizedTextNotLocalized(modelNameOrGenerated),
                version = ModelVersion("0.0.1"),
                description = null,
                origin = ModelOrigin.Uri(URI(path)),
                // Whatever we put here will be overridden anyway to SYSTEM by business rules of model copy
                authority = ModelAuthority.SYSTEM,
                documentationHome = null,
            ),
            types = types,
            entities = entities,
            relationships = relationships,
            tags = emptyList(),
            attributes = attributesCollector,
            entityPrimaryKeys = emptyList(),
            businessKeys = emptyList()
        )
        return ModelImporterData(
            model,
            emptyList() // No tags from databases anyway
        )
    }

}
