package io.medatarun.ext.db.internal.modelimport

import io.medatarun.ext.db.domain.*
import io.medatarun.lang.strings.trimToNull
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.model.domain.*
import io.medatarun.model.infra.*
import io.medatarun.model.infra.inmemory.EntityPrimaryKeyInMemory
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.infra.inmemory.PBKeyParticipantInMemory
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.model.ports.needs.ModelImporterData
import io.medatarun.platform.kernel.ResourceLocator
import io.medatarun.type.commons.id.Id
import java.net.URI
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

internal class DbModelImporter(dbDriverManager: DbDriverManager, val dbConnectionRegistry: DbConnectionRegistry) :
    ModelImporter {

    private val introspect = DbIntrospect(dbDriverManager)

    override fun accept(
        path: String,
        resourceLocator: ResourceLocator
    ): Boolean {
        return path.startsWith("datasource:")
    }

    data class Collector(
        private val types: MutableList<ModelTypeInMemory> = mutableListOf(),
        private val entities: MutableList<EntityInMemory> = mutableListOf(),
        private val attributes: MutableList<AttributeInMemory> = mutableListOf(),
        private val relationships: MutableList<RelationshipInMemory> = mutableListOf()
    ) {
        private var duplicateCount = 0
        fun addType(item: ModelTypeInMemory) = types.add(item)
        fun addAttribute(item: AttributeInMemory) = attributes.add(item)
        fun findTypeByName(tableName: String, typeName: String): ModelType {
            return types.firstOrNull { type -> type.key == TypeKey(typeName) }
                ?: throw DbImportTypeNotFoundException(tableName, typeName)
        }

        fun findAllTypes(): List<ModelTypeInMemory> {
            return types
        }

        fun findAttributeOptional(entityId: EntityId, columnName: String): AttributeInMemory? {
            return attributes.firstOrNull { attr ->
                val ownerId = attr.ownerId
                ownerId is AttributeOwnerId.OwnerEntityId && ownerId.id == entityId && attr.key.value == columnName
            }
        }

        fun findAttributeOptional(entityId: EntityId, attributeKey: AttributeKey): AttributeInMemory? {
            return attributes.firstOrNull { attr ->
                val ownerId = attr.ownerId
                ownerId is AttributeOwnerId.OwnerEntityId && ownerId.id == entityId && attr.key == attributeKey
            }
        }

        fun addEntity(e: EntityInMemory) {
            this.entities.add(e)
        }

        fun findEntityByTableName(tableName: String) = entities
            .firstOrNull { it.key == EntityKey(tableName) }
            ?: throw DbImportCouldNotFindEntityForRelationship(tableName)

        fun findAllEntities(): List<EntityInMemory> {
            return entities
        }

        fun findAllAttributes(): List<AttributeInMemory> {
            return attributes
        }

        fun addRelationship(rel: RelationshipInMemory) {
            this.relationships.add(rel)
        }

        fun findAllRelationships(): List<RelationshipInMemory> {
            return relationships
        }

        fun createFKNameSafe(key: String): String {
            return if (relationships.any { it.key.value == key }) {
                key + "_" + (duplicateCount++)
            } else key
        }

    }

    override fun toModel(
        path: String,
        resourceLocator: ResourceLocator,
        modelKeyChoosen: ModelKey?,
        modelNameChoosen: String?
    ): ModelImporterData {
        val collector = Collector()
        val connectionName = path.split(":").last()
        val connection = dbConnectionRegistry.findByNameOptional(connectionName)
            ?: throw DbConnectionNotFoundException(connectionName)
        val result: IntrospectResult = introspect.introspect(connection)
        val date = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime()

        // Add types to collector
        result.types().forEach {
            val type = ModelTypeInMemory(TypeId.generate(), TypeKey(it), null, null)
            collector.addType(type)
        }


        val pkCollector = mutableListOf<EntityPrimaryKeyInMemory>()
        result.tables.forEach { table ->
            val entityId = EntityId.generate()

            table.columns.forEach { column ->
                val attr = toAttribute(collector, table, column, entityId)
                collector.addAttribute(attr)
            }

            val pk = toEntityPrimaryKey(table, collector, entityId)
            if (pk != null) pkCollector.add(pk)

            val pkAttributeId = toOldDeprecatedEntityIdentifierAttribute(table, collector, entityId)

            val e = toEntity(entityId, table, pkAttributeId, path)
            collector.addEntity(e)
        }





        for (table in result.tables) {
            for (fk in table.foreignKeys) {
                val rel = toRelationship(fk, collector, result)
                collector.addRelationship(rel)
            }
        }
        val m = toModel(modelKeyChoosen, modelNameChoosen, date, path, connection.name)
        val model = ModelAggregateInMemory(
            model = m,
            types = collector.findAllTypes(),
            entities = collector.findAllEntities(),
            relationships = collector.findAllRelationships(),
            tags = emptyList(),
            attributes = collector.findAllAttributes(),
            entityPrimaryKeys = pkCollector,
            businessKeys = emptyList()
        )
        return ModelImporterData(
            model,
            emptyList() // No tags from databases anyway
        )
    }

    private fun toRelationship(
        fk: IntrospectImportedKey,
        collector: Collector,
        result: IntrospectResult,
    ): RelationshipInMemory {
        val key = fk.fkName ?: "${fk.pkTableName}.${fk.pkColumnName}__${fk.fkTableName}.${fk.fkColumnName}"
        val roles = listOf(
            RelationshipRoleInMemory(
                id = RelationshipRoleId.generate(),
                key = RelationshipRoleKey("${fk.fkTableName}.${fk.fkColumnName}"),
                entityId = collector.findEntityByTableName(fk.fkTableName).id,
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
                entityId = collector.findEntityByTableName(fk.pkTableName).id,
                name = null
            ),
        )
        val fkKeySafe = collector.createFKNameSafe(key)
        val rel = RelationshipInMemory(
            id = RelationshipId.generate(),
            key = RelationshipKey(fkKeySafe),
            name = null,
            description = null,
            roles = roles,
            tags = emptyList(),
        )
        return rel
    }

    private fun toEntity(
        entityId: EntityId,
        table: IntrospectTable,
        pkAttributeId: AttributeId,
        path: String
    ): EntityInMemory {
        val e = EntityInMemory(
            id = entityId,
            key = EntityKey(table.tableName),
            name = null,
            description = table.remarks?.let(::LocalizedMarkdownNotLocalized),
            // TODO a supprimer plus tard
            identifierAttributeId = pkAttributeId,
            origin = EntityOrigin.Uri(URI(path)),
            documentationHome = null,
            tags = emptyList()
        )
        return e
    }

    @Deprecated("to be removed")
    private fun toOldDeprecatedEntityIdentifierAttribute(
        table: IntrospectTable,
        collector: Collector,
        entityId: EntityId
    ): AttributeId {
        // TODO a supprimer quand on aura fini
        val pkAttributeKey = table.pkNameOrFirstColumn()
        val pkAttribute = collector.findAttributeOptional(entityId, pkAttributeKey)
            ?: throw DbImportCouldNotFindAttributeFromPrimaryKeyException(table.tableName, pkAttributeKey.value)
        val pkAttributeId = pkAttribute.id
        // -- fin du TODO a supprimer quand on aura fini
        return pkAttributeId
    }

    private fun toAttribute(
        collector: Collector,
        table: IntrospectTable,
        column: IntrospectTableColumn,
        entityId: EntityId
    ): AttributeInMemory {
        val type = collector.findTypeByName(table.tableName, column.typeName)
        val attr = AttributeInMemory(
            id = AttributeId.generate(),
            key = AttributeKey(column.columnName),
            name = null,
            description = column.remarks?.let(::LocalizedMarkdownNotLocalized),
            typeId = type.id,
            optional = column.isNullable != false,
            tags = emptyList(),
            ownerId = AttributeOwnerId.OwnerEntityId(entityId)
        )
        return attr
    }

    private fun toModel(
        modelKeyChoosen: ModelKey?,
        modelNameChoosen: String?,
        date: LocalDateTime?,
        path: String,
        datasourceName: String
    ): ModelInMemory {
        val modelKeyOrGenerated =
            modelKeyChoosen?.value?.trimToNull() ?: (datasourceName + "-" + UuidUtils.generateV4String())
        val modelNameOrGenerated = modelNameChoosen?.trimToNull() ?: "$datasourceName (import $date)"
        val m = ModelInMemory(
            id = ModelId.generate(),
            key = ModelKey(modelKeyOrGenerated),
            name = LocalizedTextNotLocalized(modelNameOrGenerated),
            version = ModelVersion("0.0.1"),
            description = null,
            origin = ModelOrigin.Uri(URI(path)),
            // Whatever we put here will be overridden anyway to SYSTEM by business rules of model copy
            authority = ModelAuthority.SYSTEM,
            documentationHome = null,
        )
        return m
    }

    private fun toEntityPrimaryKey(
        table: IntrospectTable,
        collector: Collector,
        entityId: EntityId
    ): EntityPrimaryKeyInMemory? {
        val pkAttrs = table.primaryKey
            .mapNotNull { pkcol -> collector.findAttributeOptional(entityId, pkcol.columnName) }
            .map { it.id }
        val pk = if (pkAttrs.isNotEmpty()) {
            EntityPrimaryKeyInMemory(
                Id.generate(::EntityPrimaryKeyId),
                entityId,
                pkAttrs.mapIndexed { index, id -> PBKeyParticipantInMemory(id, index) })
        } else null
        return pk
    }

}
