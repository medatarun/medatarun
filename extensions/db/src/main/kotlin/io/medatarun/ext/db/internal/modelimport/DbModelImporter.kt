package io.medatarun.ext.db.internal.modelimport

import io.medatarun.ext.db.internal.connection.DbConnectionRegistry
import io.medatarun.ext.db.internal.drivers.DbDriverManager
import io.medatarun.ext.db.model.DbConnectionNotFoundException
import io.medatarun.kernel.ResourceLocator
import io.medatarun.model.domain.*
import io.medatarun.model.infra.*
import io.medatarun.model.ports.needs.ModelImporter
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Instant
import java.time.ZoneId
import java.util.*

class DbModelImporter(val dbDriverManager: DbDriverManager, val dbConnectionRegistry: DbConnectionRegistry) :
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
        resourceLocator: ResourceLocator
    ): Model {
        val connectionName = path.split(":").last()
        val connection = dbConnectionRegistry.findByNameOptional(connectionName)
            ?: throw DbConnectionNotFoundException(connectionName)
        val result: IntrospectResult = introspect.introspect(connection)
        val date = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime()
        val modelId = connection.name + "-" + UUID.randomUUID().toString()
        val modelName = "${connection.name} (import $date)"
        val model = ModelInMemory(
            id = ModelKey(modelId),
            name = LocalizedTextNotLocalized(modelName),
            version = ModelVersion("0.0.1"),
            description = null,
            origin = ModelOrigin.Uri(URI(path)),
            types = result.types().map { ModelTypeInMemory(TypeKey(it), null, null) },
            entityDefs = result.tables.map { table ->
                EntityDefInMemory(
                    id = EntityKey(table.tableName),
                    name = null,
                    attributes = table.columns.map {
                        AttributeDefInMemory(
                            id = AttributeKey(it.columnName),
                            name = null,
                            description = it.remarks?.let(::LocalizedTextNotLocalized),
                            type = TypeKey(it.typeName),
                            optional = it.isNullable != false,
                            hashtags = emptyList(),
                        )
                    },
                    description = table.remarks?.let(::LocalizedTextNotLocalized),
                    identifierAttributeKey = table.pkNameOrFirstColumn(),
                    origin = EntityOrigin.Uri(URI(path)),
                    documentationHome = null,
                    hashtags = emptyList()

                )
            },
            relationshipDefs = result.tables.map { table ->
                table.foreignKeys.map { fk ->
                    val idStr =
                        fk.fkName ?: "${fk.pkTableName}.${fk.pkColumnName}__${fk.fkTableName}.${fk.fkColumnName}"
                    val roles = listOf(
                        RelationshipRoleInMemory(
                            id = RelationshipRoleId("${fk.fkTableName}.${fk.fkColumnName}"),
                            entityId = EntityKey(fk.fkTableName),
                            name = null,
                            cardinality = if (result.isNullableOrUndefined(
                                    fk.fkTableName,
                                    fk.fkColumnName
                                )
                            ) RelationshipCardinality.ZeroOrOne else RelationshipCardinality.One,
                        ),
                        RelationshipRoleInMemory(
                            id = RelationshipRoleId("${fk.pkTableName}.${fk.pkColumnName}"),
                            cardinality = RelationshipCardinality.Unknown,
                            entityId = EntityKey(fk.pkTableName),
                            name = null
                        ),
                    )
                    RelationshipDefInMemory(
                        id = RelationshipKey(idStr),
                        name = null,
                        description = null,
                        attributes = emptyList(),
                        roles = roles,
                        hashtags = emptyList(),
                    )
                }
            }.flatten(),
            documentationHome = null,
            hashtags = emptyList(),
        )
        return model
    }


    companion object {
        private val logger = LoggerFactory.getLogger(DbModelImporter::class.java)
    }
}