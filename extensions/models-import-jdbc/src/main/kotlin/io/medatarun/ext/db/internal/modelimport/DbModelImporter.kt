package io.medatarun.ext.db.internal.modelimport

import io.medatarun.ext.db.internal.connection.DbConnectionRegistry
import io.medatarun.ext.db.internal.drivers.DbDriverManager
import io.medatarun.ext.db.model.DbConnectionNotFoundException
import io.medatarun.ext.db.model.DbImportCouldNotFindAttributeFromPrimaryKeyException
import io.medatarun.ext.db.model.DbImportCouldNotFindEntityForRelationship
import io.medatarun.ext.db.model.DbImportTypeNotFoundException
import io.medatarun.lang.strings.trimToNull
import io.medatarun.model.domain.*
import io.medatarun.model.infra.*
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.platform.kernel.ResourceLocator
import java.net.URI
import java.time.Instant
import java.time.ZoneId
import java.util.*

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
    ): Model {
        val connectionName = path.split(":").last()
        val connection = dbConnectionRegistry.findByNameOptional(connectionName)
            ?: throw DbConnectionNotFoundException(connectionName)
        val result: IntrospectResult = introspect.introspect(connection)
        val date = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime()
        val modelKeyOrGenerated = modelKey?.value?.trimToNull() ?: (connection.name + "-" + UUID.randomUUID().toString())
        val modelNameOrGenerated = modelName?.trimToNull() ?: "${connection.name} (import $date)"
        val types = result.types().map { ModelTypeInMemory(TypeId.generate(), TypeKey(it), null, null) }
        val entities = result.tables.map { table ->

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
                    hashtags = emptyList(),
                )
            }
            val pkAttributeKey = table.pkNameOrFirstColumn()
            val pkAttribute = attributeFromColumns.firstOrNull { it.key == pkAttributeKey }
                ?: throw DbImportCouldNotFindAttributeFromPrimaryKeyException(table.tableName, pkAttributeKey.value)

            EntityDefInMemory(
                id = EntityId.generate(),
                key = EntityKey(table.tableName),
                name = null,
                attributes = attributeFromColumns,
                description = table.remarks?.let(::LocalizedMarkdownNotLocalized),
                identifierAttributeId = pkAttribute.id,
                origin = EntityOrigin.Uri(URI(path)),
                documentationHome = null,
                hashtags = emptyList()

            )
        }

        fun findEntityByTableName(tableName: String) = entities
            .firstOrNull { it.key == EntityKey(tableName) }
            ?: throw DbImportCouldNotFindEntityForRelationship(tableName)

        val relationships = result.tables.map { table ->
            table.foreignKeys.map { fk ->
                val idStr =
                    fk.fkName ?: "${fk.pkTableName}.${fk.pkColumnName}__${fk.fkTableName}.${fk.fkColumnName}"
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
                RelationshipDefInMemory(
                    id = RelationshipId.generate(),
                    key = RelationshipKey(idStr),
                    name = null,
                    description = null,
                    attributes = emptyList(),
                    roles = roles,
                    hashtags = emptyList(),
                )
            }
        }
        val model = ModelInMemory(
            id = ModelId.generate(),
            key = ModelKey(modelKeyOrGenerated),
            name = LocalizedTextNotLocalized(modelNameOrGenerated),
            version = ModelVersion("0.0.1"),
            description = null,
            origin = ModelOrigin.Uri(URI(path)),
            types = types,
            entityDefs = entities,
            relationshipDefs = relationships.flatten(),
            documentationHome = null,
            hashtags = emptyList(),
        )
        return model
    }

}