package io.medatarun.model.infra.db.snapshots

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.ModelStorageDbSearchWrite
import io.medatarun.model.infra.db.ModelStorageDbUnsupportedProjectedDeleteException
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.ports.needs.ModelClock
import io.medatarun.model.ports.needs.ModelStorageCmd

internal class ModelStorageDbProjection(
    private val searchWrite: ModelStorageDbSearchWrite,
    private val snapshots: ModelStorageDbSnapshots,
    private val clock: ModelClock,
    private val snapWrite: ModelStorageDbSnapshotWriter,
    private val snapshotCreate: ModelStorageDbSnapshotCreate
) {

    data class ProjectionEventCtx(
        val cmd: ModelStorageCmd,
        val modelId: ModelId,
        val modelSnapshotId: ModelSnapshotId,
        val modelEventId: ModelEventId,
        val streamRevision: Int
    )

    fun projectCommand(ctx: ProjectionEventCtx) {
        when (val cmd = ctx.cmd) {
            //@formatter:off
            is ModelStorageCmd.StoreModelAggregate -> storeModelAggregate(ctx, cmd)
            is ModelStorageCmd.CreateModel -> createModel(ctx, cmd)
            is ModelStorageCmd.UpdateModelName -> updateModelName(ctx, cmd)
            is ModelStorageCmd.UpdateModelKey -> updateModelKey(ctx, cmd)
            is ModelStorageCmd.UpdateModelDescription -> updateModelDescription(ctx, cmd)
            is ModelStorageCmd.UpdateModelAuthority -> updateModelAuthority(ctx, cmd)
            is ModelStorageCmd.ModelRelease -> releaseModel(ctx, cmd)
            is ModelStorageCmd.UpdateModelDocumentationHome -> updateModelDocumentationHome(ctx, cmd)
            is ModelStorageCmd.UpdateModelTagAdd -> addModelTag(ctx, cmd)
            is ModelStorageCmd.UpdateModelTagDelete -> deleteModelTag(ctx, cmd)
            is ModelStorageCmd.CreateType -> createType(ctx, cmd)
            is ModelStorageCmd.UpdateTypeKey -> updateTypeKey(ctx, cmd)
            is ModelStorageCmd.UpdateTypeName -> updateTypeName(ctx, cmd)
            is ModelStorageCmd.UpdateTypeDescription -> updateTypeDescription(ctx, cmd)
            is ModelStorageCmd.DeleteType -> deleteType(ctx, cmd)
            is ModelStorageCmd.CreateEntity -> createEntity(ctx, cmd)
            is ModelStorageCmd.UpdateEntityKey -> updateEntityKey(ctx, cmd)
            is ModelStorageCmd.UpdateEntityName -> updateEntityName(ctx, cmd)
            is ModelStorageCmd.UpdateEntityDescription -> updateEntityDescription(ctx, cmd)
            is ModelStorageCmd.Entity_PrimaryKey_Set -> entityPrimaryKeySet(ctx, cmd)
            is ModelStorageCmd.UpdateEntityDocumentationHome -> updateEntityDocumentationHome(ctx, cmd)
            is ModelStorageCmd.UpdateEntityTagAdd -> addEntityTag(ctx, cmd)
            is ModelStorageCmd.UpdateEntityTagDelete -> deleteEntityTag(ctx, cmd)
            is ModelStorageCmd.DeleteEntity -> deleteEntity(ctx, cmd)
            is ModelStorageCmd.CreateEntityAttribute -> createEntityAttribute(ctx, cmd)
            is ModelStorageCmd.UpdateEntityAttributeKey -> updateEntityAttributeKey(ctx, cmd)
            is ModelStorageCmd.UpdateEntityAttributeName -> updateEntityAttributeName(ctx, cmd)
            is ModelStorageCmd.UpdateEntityAttributeDescription -> updateEntityAttributeDescription(ctx, cmd)
            is ModelStorageCmd.UpdateEntityAttributeType -> updateEntityAttributeType(ctx, cmd)
            is ModelStorageCmd.UpdateEntityAttributeOptional -> updateEntityAttributeOptional(ctx, cmd)
            is ModelStorageCmd.UpdateEntityAttributeTagAdd -> addEntityAttributeTag(ctx, cmd)
            is ModelStorageCmd.UpdateEntityAttributeTagDelete -> deleteEntityAttributeTag(ctx, cmd)
            is ModelStorageCmd.DeleteEntityAttribute -> deleteEntityAttribute(ctx, cmd)
            is ModelStorageCmd.CreateRelationship -> createRelationship(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipKey -> updateRelationshipKey(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipName -> updateRelationshipName(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipDescription -> updateRelationshipDescription(ctx, cmd)
            is ModelStorageCmd.CreateRelationshipRole -> createRelationshipRole(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipRoleKey -> updateRelationshipRoleKey(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipRoleName -> updateRelationshipRoleName(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipRoleEntity -> updateRelationshipRoleEntity(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipRoleCardinality -> updateRelationshipRoleCardinality(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipTagAdd -> addRelationshipTag(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipTagDelete -> deleteRelationshipTag(ctx, cmd)
            is ModelStorageCmd.DeleteRelationship -> deleteRelationship(ctx, cmd)
            is ModelStorageCmd.DeleteRelationshipRole -> deleteRelationshipRole(ctx, cmd)
            is ModelStorageCmd.CreateRelationshipAttribute -> createRelationshipAttribute(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeKey -> updateRelationshipAttributeKey(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeName -> updateRelationshipAttributeName(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeDescription -> updateRelationshipAttributeDescription(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeType -> updateRelationshipAttributeType(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeOptional -> updateRelationshipAttributeOptional(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeTagAdd -> addRelationshipAttributeTag(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeTagDelete -> deleteRelationshipAttributeTag(ctx, cmd)
            is ModelStorageCmd.DeleteRelationshipAttribute -> deleteRelationshipAttribute(ctx, cmd)
            is ModelStorageCmd.DeleteModel -> throw ModelStorageDbUnsupportedProjectedDeleteException("model_deleted")
            //@formatter:on
        }
    }

    // Model
    // ------------------------------------------------------------------------


    private fun createModel(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.CreateModel) {
        val inMemoryModel = ModelInMemory(
            id = cmd.id,
            key = cmd.key,
            name = cmd.name,
            description = cmd.description,
            version = cmd.version,
            origin = cmd.origin,
            authority = cmd.authority,
            documentationHome = cmd.documentationHome,
        )
        val now = clock.now()
        snapWrite.modelInsert(
            ModelRecord(
                snapshotId = ctx.modelSnapshotId,
                modelId = inMemoryModel.id,
                key = inMemoryModel.key,
                name = inMemoryModel.name,
                description = inMemoryModel.description,
                version = inMemoryModel.version,
                origin = inMemoryModel.origin,
                authority = inMemoryModel.authority,
                documentationHome = inMemoryModel.documentationHome?.toExternalForm(),
                snapshotKind = ModelSnapshotKind.CURRENT_HEAD,
                upToRevision = 0,
                modelEventReleaseId = null,
                createdAt = now,
                updatedAt = now
            )
        )
        searchWrite.refreshModelBranch(ctx.modelSnapshotId)
    }

    private fun storeModelAggregate(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.StoreModelAggregate) {

        val now = clock.now()
        val modelSnapshotId = ctx.modelSnapshotId
        snapWrite.modelInsert(
            ModelRecord(
                snapshotId = modelSnapshotId,
                modelId = cmd.model.id,
                key = cmd.model.key,
                name = cmd.model.name,
                description = cmd.model.description,
                version = cmd.model.version,
                origin = cmd.model.origin,
                authority = cmd.model.authority,
                documentationHome = cmd.model.documentationHome?.toExternalForm(),
                snapshotKind = ModelSnapshotKind.CURRENT_HEAD,
                upToRevision = 0,
                modelEventReleaseId = null,
                createdAt = now,
                updatedAt = now
            )
        )

        val typeSnapshotIds = mutableMapOf<TypeId, TypeSnapshotId>()
        val entitySnapshotIds = mutableMapOf<EntityId, EntitySnapshotId>()
        val entityAttributeSnapshotIds = mutableMapOf<AttributeId, AttributeSnapshotId>()
        val relationshipSnapshotIds = mutableMapOf<RelationshipId, RelationshipSnapshotId>()
        val relationshipAttributeSnapshotIds = mutableMapOf<AttributeId, AttributeSnapshotId>()

        for (type in cmd.types) {
            val typeSnapshotId = TypeSnapshotId.generate()
            typeSnapshotIds[type.id] = typeSnapshotId
            snapWrite.typeInsert(
                ModelTypeRecord(
                    snapshotId = typeSnapshotId,
                    lineageId = type.id,
                    modelSnapshotId = modelSnapshotId,
                    key = type.key,
                    name = type.name,
                    description = type.description
                )
            )
        }

        for (entity in cmd.entities) {
            val entitySnapshotId = EntitySnapshotId.generate()
            entitySnapshotIds[entity.id] = entitySnapshotId

            val identifierAttributeSnapshotId = entityAttributeSnapshotIds
                .getOrPut(entity.identifierAttributeId) { AttributeSnapshotId.generate() }

            val entityPKSnapshotId = EntityPKSnapshotId.generate()
            snapWrite.entityInsert(
                EntityRecord(
                    snapshotId = entitySnapshotId,
                    lineageId = entity.id,
                    modelSnapshotId = modelSnapshotId,
                    key = entity.key,
                    name = entity.name,
                    description = entity.description,
                    origin = entity.origin,
                    documentationHome = entity.documentationHome?.toExternalForm(),
                )
            )
            searchWrite.refreshEntityBranch(modelSnapshotId, entity.id)

            for (attr in cmd.entityAttributes.filter { it.entityId == entity.id }) {
                val attributeSnapshotId = entityAttributeSnapshotIds.getOrPut(attr.id) {
                    AttributeSnapshotId.generate()
                }
                snapWrite.entityAttributeInsert(
                    EntityAttributeRecord(
                        snapshotId = attributeSnapshotId,
                        lineageId = attr.id,
                        entitySnapshotId = entitySnapshotId,
                        key = attr.key,
                        name = attr.name,
                        description = attr.description,
                        typeSnapshotId = typeSnapshotIds.getValue(attr.typeId),
                        optional = attr.optional
                    )
                )
                searchWrite.refreshEntityAttributeBranch(modelSnapshotId, entity.id, attr.id)
            }
            snapWrite.entityPrimaryKeyInsert(
                entityPKSnapshotId,
                entitySnapshotId,
                listOf(identifierAttributeSnapshotId)
            )
        }

        for (relationship in cmd.relationships) {
            val relationshipSnapshotId = RelationshipSnapshotId.generate()
            relationshipSnapshotIds[relationship.id] = relationshipSnapshotId
            snapWrite.relationshipInsert(
                record = RelationshipRecord(
                    snapshotId = relationshipSnapshotId,
                    lineageId = relationship.id,
                    modelSnapshotId = modelSnapshotId,
                    key = relationship.key,
                    name = relationship.name,
                    description = relationship.description
                ),
                roles = relationship.roles.map { role ->
                    RelationshipRoleRecord(
                        snapshotId = RelationshipRoleSnapshotId.generate(),
                        lineageId = role.id,
                        relationshipSnapshotId = relationshipSnapshotId,
                        key = role.key,
                        entitySnapshotId = entitySnapshotIds.getValue(role.entityId),
                        name = role.name,
                        cardinality = role.cardinality.code
                    )
                }
            )
            searchWrite.refreshRelationshipBranch(modelSnapshotId, relationship.id)

            for (attr in cmd.relationshipAttributes.filter { it.relationshipId == relationship.id }) {
                val attributeSnapshotId = relationshipAttributeSnapshotIds.getOrPut(attr.id) {
                    AttributeSnapshotId.generate()
                }
                snapWrite.relationshipAttributeInsert(
                    RelationshipAttributeRecord(
                        snapshotId = attributeSnapshotId,
                        lineageId = attr.id,
                        relationshipSnapshotId = relationshipSnapshotId,
                        key = attr.key,
                        name = attr.name,
                        description = attr.description,
                        typeSnapshotId = typeSnapshotIds.getValue(attr.typeId),
                        optional = attr.optional
                    )
                )
                searchWrite.refreshRelationshipAttributeBranch(modelSnapshotId, relationship.id, attr.id)
            }
        }

        searchWrite.refreshModelBranch(modelSnapshotId)

    }


    private fun updateModelName(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateModelName) {
        snapWrite.modelUpdateName(ctx.modelSnapshotId, cmd.name)
        searchWrite.refreshModelBranch(ctx.modelSnapshotId)
    }

    private fun updateModelKey(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateModelKey) {
        snapWrite.modelUpdateKey(ctx.modelSnapshotId, cmd.key)
        searchWrite.refreshModelBranch(ctx.modelSnapshotId)
    }

    private fun updateModelDescription(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateModelDescription) {
        snapWrite.modelUpdateDescription(ctx.modelSnapshotId, cmd.description)
        searchWrite.refreshModelBranch(ctx.modelSnapshotId)
    }

    private fun updateModelAuthority(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateModelAuthority) {
        snapWrite.modelUpdateAuthority(ctx.modelSnapshotId, cmd.authority)
    }

    private fun releaseModel(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.ModelRelease) {
        snapWrite.modelUpdateVersion(ctx.modelSnapshotId, cmd.version)
        snapshotCreate.createVersionSnapshotFromCurrentHead(
            modelId = ctx.modelId,
            currentHeadSnapshotId = ctx.modelSnapshotId,
            streamRevision = ctx.streamRevision,
            modelEventId = ctx.modelEventId,
            version = cmd.version
        )
    }

    private fun updateModelDocumentationHome(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateModelDocumentationHome
    ) {
        snapWrite.modelUpdateDocumentationHome(ctx.modelSnapshotId, cmd.documentationHome?.toExternalForm())
    }

    private fun addModelTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateModelTagAdd) {
        snapWrite.modelAddTagIfNotExists(ctx.modelSnapshotId, cmd.tagId)
        searchWrite.refreshModelBranch(ctx.modelSnapshotId)
    }

    private fun deleteModelTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateModelTagDelete) {
        snapWrite.modelDeleteTag(ctx.modelSnapshotId, cmd.tagId)
        searchWrite.refreshModelBranch(ctx.modelSnapshotId)
    }

    // Types
    // ------------------------------------------------------------------------


    private fun createType(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.CreateType) {
        val record = ModelTypeRecord(
            snapshotId = TypeSnapshotId.generate(),
            lineageId = cmd.typeId,
            modelSnapshotId = ctx.modelSnapshotId,
            key = cmd.key,
            name = cmd.name,
            description = cmd.description
        )
        snapWrite.typeInsert(record)
    }

    private fun updateTypeKey(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateTypeKey) {
        snapWrite.typeUpdateKey(ctx.modelSnapshotId, cmd.typeId, cmd.key)
    }

    private fun updateTypeName(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateTypeName) {
        snapWrite.typeUpdateName(ctx.modelSnapshotId, cmd.typeId, cmd.name)
    }

    private fun updateTypeDescription(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateTypeDescription) {
        snapWrite.typeUpdateDescription(ctx.modelSnapshotId, cmd.typeId, cmd.description)
    }

    private fun deleteType(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.DeleteType) {
        snapWrite.typeDelete(ctx.modelSnapshotId, cmd.typeId)
    }

    // Entity
    // ------------------------------------------------------------------------


    private fun createEntity(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.CreateEntity) {
        val entitySnapshotId = EntitySnapshotId.generate()
        val record = EntityRecord(
            snapshotId = entitySnapshotId,
            lineageId = cmd.entityId,
            modelSnapshotId = ctx.modelSnapshotId,
            key = cmd.key,
            name = cmd.name,
            description = cmd.description,
            origin = cmd.origin,
            documentationHome = cmd.documentationHome?.toExternalForm()
        )
        snapWrite.entityInsert(record)
        searchWrite.refreshEntityBranch(ctx.modelSnapshotId, cmd.entityId)
    }


    private fun updateEntityKey(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityKey) {
        snapWrite.entityUpdateKey(ctx.modelSnapshotId, cmd.entityId, cmd.key)
        searchWrite.refreshEntityBranch(ctx.modelSnapshotId, cmd.entityId)
    }

    private fun updateEntityName(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityName) {
        snapWrite.entityUpdateName(ctx.modelSnapshotId, cmd.entityId, cmd.name)
        searchWrite.refreshEntityBranch(ctx.modelSnapshotId, cmd.entityId)
    }

    private fun updateEntityDescription(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityDescription) {
        snapWrite.entityUpdateDescription(ctx.modelSnapshotId, cmd.entityId, cmd.description)
        searchWrite.refreshEntityBranch(ctx.modelSnapshotId, cmd.entityId)
    }

    private fun entityPrimaryKeySet(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.Entity_PrimaryKey_Set) {
        snapWrite.entityPrimaryKeyUpdate(
            modelSnapshotId = ctx.modelSnapshotId,
            entityId = cmd.entityId,
            attributeIds = cmd.attributeIds
        )
    }

    private fun updateEntityDocumentationHome(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateEntityDocumentationHome
    ) {
        snapWrite.entityUpdateDocumentationHome(
            modelSnapshotId = ctx.modelSnapshotId,
            entityId = cmd.entityId,
            documentationHome = cmd.documentationHome?.toExternalForm()
        )
    }

    private fun addEntityTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityTagAdd) {
        snapWrite.entityAddTagIfNotExist(ctx.modelSnapshotId, cmd.entityId, cmd.tagId)
        searchWrite.refreshEntityBranch(ctx.modelSnapshotId, cmd.entityId)
    }

    private fun deleteEntityTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityTagDelete) {
        snapWrite.entityDeleteTag(ctx.modelSnapshotId, cmd.entityId, cmd.tagId)
        searchWrite.refreshEntityBranch(ctx.modelSnapshotId, cmd.entityId)
    }

    private fun deleteEntity(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.DeleteEntity) {
        searchWrite.deleteEntityBranch(cmd.entityId)
        snapWrite.entityDelete(ctx.modelSnapshotId, cmd.entityId)
    }

    // Entity attribute
    // ------------------------------------------------------------------------


    private fun createEntityAttribute(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.CreateEntityAttribute) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotIdInModelSnapshot(ctx.modelSnapshotId, cmd.entityId)
        snapWrite.entityAttributeInsert(
            EntityAttributeRecord(
                snapshotId = AttributeSnapshotId.generate(),
                lineageId = cmd.attributeId,
                entitySnapshotId = entitySnapshotId,
                key = cmd.key,
                name = cmd.name,
                description = cmd.description,
                typeSnapshotId = snapshots.currentHeadTypeSnapshotIdInModelSnapshot(ctx.modelSnapshotId, cmd.typeId),
                optional = cmd.optional
            )
        )
        searchWrite.refreshEntityAttributeBranch(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId)
    }

    private fun updateEntityAttributeKey(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityAttributeKey) {
        snapWrite.entityAttributeUpdateKey(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId, cmd.key)
        searchWrite.refreshEntityAttributeBranch(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId)
    }

    private fun updateEntityAttributeName(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityAttributeName) {
        snapWrite.entityAttributeUpdateName(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId, cmd.name)
        searchWrite.refreshEntityAttributeBranch(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId)
    }

    private fun updateEntityAttributeDescription(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateEntityAttributeDescription
    ) {
        snapWrite.entityAttributeUpdateDescription(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId, cmd.description)
        searchWrite.refreshEntityAttributeBranch(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId)
    }

    private fun updateEntityAttributeType(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityAttributeType) {
        snapWrite.entityAttributeUpdateType(
            modelSnapshotId = ctx.modelSnapshotId,
            entityId = cmd.entityId,
            attributeId = cmd.attributeId,
            typeId = cmd.typeId
        )
    }

    private fun updateEntityAttributeOptional(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateEntityAttributeOptional
    ) {
        snapWrite.entityAttributeUpdateOptional(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId, cmd.optional)
    }


    private fun addEntityAttributeTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityAttributeTagAdd) {
        snapWrite.entityAttributeAddTagIfNotExists(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId, cmd.tagId)
        searchWrite.refreshEntityAttributeBranch(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId)
    }

    private fun deleteEntityAttributeTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityAttributeTagDelete) {
        snapWrite.entityAttributeDeleteTag(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId, cmd.tagId)
        searchWrite.refreshEntityAttributeBranch(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId)
    }

    private fun deleteEntityAttribute(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.DeleteEntityAttribute) {
        searchWrite.deleteEntityAttributeBranch(cmd.attributeId)
        snapWrite.entityAttributeDelete(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId)
    }


    // Relationship
    // ------------------------------------------------------------------------


    private fun createRelationship(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.CreateRelationship) {
        val record = RelationshipRecord(
            snapshotId = RelationshipSnapshotId.generate(),
            lineageId = cmd.relationshipId,
            modelSnapshotId = ctx.modelSnapshotId,
            key = cmd.key,
            name = cmd.name,
            description = cmd.description
        )
        val roles = cmd.roles.map { role ->
            RelationshipRoleRecord(
                snapshotId = RelationshipRoleSnapshotId.generate(),
                lineageId = role.id,
                relationshipSnapshotId = record.snapshotId,
                key = role.key,
                name = role.name,
                entitySnapshotId = snapshots.currentHeadEntitySnapshotIdInModelSnapshot(
                    ctx.modelSnapshotId,
                    role.entityId
                ),
                cardinality = role.cardinality.code
            )
        }
        snapWrite.relationshipInsert(record, roles)
        searchWrite.refreshRelationshipBranch(ctx.modelSnapshotId, cmd.relationshipId)
    }

    private fun updateRelationshipKey(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateRelationshipKey) {
        snapWrite.relationshipUpdateKey(ctx.modelSnapshotId, cmd.relationshipId, cmd.key)
        searchWrite.refreshRelationshipBranch(ctx.modelSnapshotId, cmd.relationshipId)
    }

    private fun updateRelationshipName(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateRelationshipName) {
        snapWrite.relationshipUpdateName(ctx.modelSnapshotId, cmd.relationshipId, cmd.name)
        searchWrite.refreshRelationshipBranch(ctx.modelSnapshotId, cmd.relationshipId)
    }

    private fun updateRelationshipDescription(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipDescription
    ) {
        snapWrite.relationshipUpdateDescription(ctx.modelSnapshotId, cmd.relationshipId, cmd.description)
        searchWrite.refreshRelationshipBranch(ctx.modelSnapshotId, cmd.relationshipId)
    }

    private fun createRelationshipRole(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.CreateRelationshipRole) {
        snapWrite.relationshipRoleInsert(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            relationshipRoleId = cmd.relationshipRoleId,
            key = cmd.key,
            name = cmd.name,
            entityId = cmd.entityId,
            cardinality = cmd.cardinality.code
        )
    }


    private fun updateRelationshipRoleKey(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateRelationshipRoleKey) {
        snapWrite.relationshipRoleUpdateKey(ctx.modelSnapshotId, cmd.relationshipId, cmd.relationshipRoleId, cmd.key)
    }

    private fun updateRelationshipRoleName(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateRelationshipRoleName) {
        snapWrite.relationshipRoleUpdateName(ctx.modelSnapshotId, cmd.relationshipId, cmd.relationshipRoleId, cmd.name)
    }

    private fun updateRelationshipRoleEntity(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipRoleEntity
    ) {
        snapWrite.relationshipRoleUpdateEntity(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            relationshipRoleId = cmd.relationshipRoleId,
            entityId = cmd.entityId
        )
    }

    private fun updateRelationshipRoleCardinality(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipRoleCardinality
    ) {
        snapWrite.relationshipRoleUpdateCardinality(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            relationshipRoleId = cmd.relationshipRoleId,
            cardinality = cmd.cardinality.code
        )
    }

    private fun deleteRelationshipRole(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.DeleteRelationshipRole) {
        snapWrite.relationshipRoleDelete(ctx.modelSnapshotId, cmd.relationshipId, cmd.relationshipRoleId)
    }

    private fun addRelationshipTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateRelationshipTagAdd) {
        snapWrite.relationshipAddTagIfNotExists(ctx.modelSnapshotId, cmd.relationshipId, cmd.tagId)
        searchWrite.refreshRelationshipBranch(ctx.modelSnapshotId, cmd.relationshipId)
    }

    private fun deleteRelationship(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.DeleteRelationship) {
        searchWrite.deleteRelationshipBranch(cmd.relationshipId)
        snapWrite.relationshipDelete(ctx.modelSnapshotId, cmd.relationshipId)
    }

    private fun deleteRelationshipTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateRelationshipTagDelete) {
        snapWrite.relationshipDeleteTag(ctx.modelSnapshotId, cmd.relationshipId, cmd.tagId)
        searchWrite.refreshRelationshipBranch(ctx.modelSnapshotId, cmd.relationshipId)
    }
    // Relationship attribute
    // ------------------------------------------------------------------------

    private fun createRelationshipAttribute(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.CreateRelationshipAttribute) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotIdInModelSnapshot(
            ctx.modelSnapshotId,
            cmd.relationshipId
        )
        val record = RelationshipAttributeRecord(
            snapshotId = AttributeSnapshotId.generate(),
            lineageId = cmd.attributeId,
            relationshipSnapshotId = relationshipSnapshotId,
            name = cmd.name,
            key = cmd.key,
            description = cmd.description,
            typeSnapshotId = snapshots.currentHeadTypeSnapshotIdInModelSnapshot(ctx.modelSnapshotId, cmd.typeId),
            optional = cmd.optional
        )
        snapWrite.relationshipAttributeInsert(record)
        searchWrite.refreshRelationshipAttributeBranch(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            attributeId = record.lineageId
        )
    }

    private fun updateRelationshipAttributeKey(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipAttributeKey
    ) {
        snapWrite.relationshipAttributeUpdateKey(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            attributeId = cmd.attributeId,
            key = cmd.key
        )
        searchWrite.refreshRelationshipAttributeBranch(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            attributeId = cmd.attributeId
        )
    }

    private fun updateRelationshipAttributeName(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipAttributeName
    ) {
        snapWrite.relationshipAttributeUpdateName(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            attributeId = cmd.attributeId,
            name = cmd.name
        )
        searchWrite.refreshRelationshipAttributeBranch(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            attributeId = cmd.attributeId
        )
    }

    private fun updateRelationshipAttributeDescription(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipAttributeDescription
    ) {
        snapWrite.relationshipAttributeUpdateDescription(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            attributeId = cmd.attributeId,
            description = cmd.description
        )
        searchWrite.refreshRelationshipAttributeBranch(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            attributeId = cmd.attributeId
        )
    }

    private fun updateRelationshipAttributeType(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipAttributeType
    ) {
        snapWrite.relationshipAttributeUpdateType(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            attributeId = cmd.attributeId,
            typeId = cmd.typeId
        )
    }

    private fun updateRelationshipAttributeOptional(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipAttributeOptional
    ) {
        snapWrite.relationshipAttributeUpdateOptional(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            attributeId = cmd.attributeId,
            optional = cmd.optional
        )

    }

    private fun addRelationshipAttributeTag(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipAttributeTagAdd
    ) {
        snapWrite.relationshipAttributeAddTag(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            attributeId = cmd.attributeId,
            tagId = cmd.tagId
        )
        searchWrite.refreshRelationshipAttributeBranch(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            attributeId = cmd.attributeId
        )
    }

    private fun deleteRelationshipAttribute(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.DeleteRelationshipAttribute
    ) {
        searchWrite.deleteRelationshipAttributeBranch(cmd.attributeId)
        snapWrite.relationshipAttributeDelete(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            attributeId = cmd.attributeId
        )
    }

    private fun deleteRelationshipAttributeTag(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipAttributeTagDelete
    ) {
        snapWrite.relationshipAttributeDeleteTag(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            attributeId = cmd.attributeId,
            tagId = cmd.tagId
        )
        searchWrite.refreshRelationshipAttributeBranch(
            modelSnapshotId = ctx.modelSnapshotId,
            relationshipId = cmd.relationshipId,
            attributeId = cmd.attributeId
        )
    }


}
