package io.medatarun.model.internal

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.*
import io.medatarun.model.domain.diff.*

/**
 * Computes the domain diff between two model states.
 *
 * Matching and comparison rules are implemented here so callers can rely on
 * a single place for model comparison behavior.
 */
class ModelDiffRunner {

    fun diff(left: ModelAggregate, right: ModelAggregate, scope: ModelDiffScope): ModelDiff {
        val entries = mutableListOf<ModelDiffEntry>()
        appendModelDiff(entries, left, right, scope)
        appendTypeDiff(entries, left, right, scope)
        appendEntityDiff(entries, left, right, scope)
        appendEntityAttributeDiff(entries, left, right, scope)
        appendRelationshipDiff(entries, left, right, scope)
        appendRelationshipRoleDiff(entries, left, right, scope)
        appendRelationshipAttributeDiff(entries, left, right, scope)
        return ModelDiff(
            scopeApplied = scope,
            left = ModelDiffModelSide(
                modelId = left.id,
                modelKey = left.key,
                modelVersion = left.version,
                modelAuthority = left.authority
            ),
            right = ModelDiffModelSide(
                modelId = right.id,
                modelKey = right.key,
                modelVersion = right.version,
                modelAuthority = right.authority
            ),
            entries = entries.sortedWith(compareBy<ModelDiffEntry> { it.location.objectType }.thenBy { locationSortKey(it.location) })
        )
    }

    private fun appendModelDiff(
        entries: MutableList<ModelDiffEntry>,
        left: ModelAggregate,
        right: ModelAggregate,
        scope: ModelDiffScope
    ) {
        val leftSnapshot = toModelSnapshot(left)
        val rightSnapshot = toModelSnapshot(right)
        val changed = when (scope) {
            ModelDiffScope.STRUCTURAL -> isModelStructuralChanged(leftSnapshot, rightSnapshot)
            ModelDiffScope.COMPLETE -> leftSnapshot != rightSnapshot
        }
        if (changed) {
            entries.add(
                ModelDiffEntry.Modified(
                    location = ModelDiffModelLocation(modelKey = left.key),
                    left = leftSnapshot,
                    right = rightSnapshot
                )
            )
        }
    }

    private fun appendTypeDiff(
        entries: MutableList<ModelDiffEntry>,
        left: ModelAggregate,
        right: ModelAggregate,
        scope: ModelDiffScope
    ) {
        val leftByKey = left.types.associateBy { it.key }
        val rightByKey = right.types.associateBy { it.key }
        val keys = (leftByKey.keys + rightByKey.keys).sortedBy { it.value }
        keys.forEach { typeKey ->
            val leftType = leftByKey[typeKey]
            val rightType = rightByKey[typeKey]
            val location = ModelDiffTypeLocation(modelKey = left.key, typeKey = typeKey)
            if (leftType == null && rightType != null) {
                entries.add(ModelDiffEntry.Added(location = location, right = toTypeSnapshot(rightType)))
            } else if (leftType != null && rightType == null) {
                entries.add(ModelDiffEntry.Deleted(location = location, left = toTypeSnapshot(leftType)))
            } else if (leftType != null && rightType != null) {
                val leftSnapshot = toTypeSnapshot(leftType)
                val rightSnapshot = toTypeSnapshot(rightType)
                val changed = when (scope) {
                    ModelDiffScope.STRUCTURAL -> isTypeStructuralChanged(leftSnapshot, rightSnapshot)
                    ModelDiffScope.COMPLETE -> leftSnapshot != rightSnapshot
                }
                if (changed) {
                    entries.add(ModelDiffEntry.Modified(location = location, left = leftSnapshot, right = rightSnapshot))
                }
            }
        }
    }

    private fun appendEntityDiff(
        entries: MutableList<ModelDiffEntry>,
        left: ModelAggregate,
        right: ModelAggregate,
        scope: ModelDiffScope
    ) {
        val leftByKey = left.entities.associateBy { it.key }
        val rightByKey = right.entities.associateBy { it.key }
        val keys = (leftByKey.keys + rightByKey.keys).sortedBy { it.value }
        keys.forEach { entityKey ->
            val leftEntity = leftByKey[entityKey]
            val rightEntity = rightByKey[entityKey]
            val location = ModelDiffEntityLocation(modelKey = left.key, entityKey = entityKey)
            if (leftEntity == null && rightEntity != null) {
                entries.add(ModelDiffEntry.Added(location = location, right = toEntitySnapshot(right, rightEntity)))
            } else if (leftEntity != null && rightEntity == null) {
                entries.add(ModelDiffEntry.Deleted(location = location, left = toEntitySnapshot(left, leftEntity)))
            } else if (leftEntity != null && rightEntity != null) {
                val leftSnapshot = toEntitySnapshot(left, leftEntity)
                val rightSnapshot = toEntitySnapshot(right, rightEntity)
                val changed = when (scope) {
                    ModelDiffScope.STRUCTURAL -> isEntityStructuralChanged(leftSnapshot, rightSnapshot)
                    ModelDiffScope.COMPLETE -> leftSnapshot != rightSnapshot
                }
                if (changed) {
                    entries.add(ModelDiffEntry.Modified(location = location, left = leftSnapshot, right = rightSnapshot))
                }
            }
        }
    }

    private fun appendEntityAttributeDiff(
        entries: MutableList<ModelDiffEntry>,
        left: ModelAggregate,
        right: ModelAggregate,
        scope: ModelDiffScope
    ) {
        val leftMap = collectEntityAttributeSnapshots(left)
        val rightMap = collectEntityAttributeSnapshots(right)
        val keys = (leftMap.keys + rightMap.keys).sortedWith(compareBy<EntityAttributePathKey> { it.entityKey.value }.thenBy { it.attributeKey.value })
        keys.forEach { pathKey ->
            val leftSnapshot = leftMap[pathKey]
            val rightSnapshot = rightMap[pathKey]
            val location = ModelDiffEntityAttributeLocation(
                modelKey = left.key,
                entityKey = pathKey.entityKey,
                attributeKey = pathKey.attributeKey
            )
            if (leftSnapshot == null && rightSnapshot != null) {
                entries.add(ModelDiffEntry.Added(location = location, right = rightSnapshot))
            } else if (leftSnapshot != null && rightSnapshot == null) {
                entries.add(ModelDiffEntry.Deleted(location = location, left = leftSnapshot))
            } else if (leftSnapshot != null && rightSnapshot != null) {
                val changed = when (scope) {
                    ModelDiffScope.STRUCTURAL -> isEntityAttributeStructuralChanged(leftSnapshot, rightSnapshot)
                    ModelDiffScope.COMPLETE -> leftSnapshot != rightSnapshot
                }
                if (changed) {
                    entries.add(ModelDiffEntry.Modified(location = location, left = leftSnapshot, right = rightSnapshot))
                }
            }
        }
    }

    private fun appendRelationshipDiff(
        entries: MutableList<ModelDiffEntry>,
        left: ModelAggregate,
        right: ModelAggregate,
        scope: ModelDiffScope
    ) {
        val leftByKey = left.relationships.associateBy { it.key }
        val rightByKey = right.relationships.associateBy { it.key }
        val keys = (leftByKey.keys + rightByKey.keys).sortedBy { it.value }
        keys.forEach { relationshipKey ->
            val leftRelationship = leftByKey[relationshipKey]
            val rightRelationship = rightByKey[relationshipKey]
            val location = ModelDiffRelationshipLocation(modelKey = left.key, relationshipKey = relationshipKey)
            if (leftRelationship == null && rightRelationship != null) {
                entries.add(ModelDiffEntry.Added(location = location, right = toRelationshipSnapshot(rightRelationship)))
            } else if (leftRelationship != null && rightRelationship == null) {
                entries.add(ModelDiffEntry.Deleted(location = location, left = toRelationshipSnapshot(leftRelationship)))
            } else if (leftRelationship != null && rightRelationship != null) {
                val leftSnapshot = toRelationshipSnapshot(leftRelationship)
                val rightSnapshot = toRelationshipSnapshot(rightRelationship)
                val changed = when (scope) {
                    ModelDiffScope.STRUCTURAL -> isRelationshipStructuralChanged(leftSnapshot, rightSnapshot)
                    ModelDiffScope.COMPLETE -> leftSnapshot != rightSnapshot
                }
                if (changed) {
                    entries.add(ModelDiffEntry.Modified(location = location, left = leftSnapshot, right = rightSnapshot))
                }
            }
        }
    }

    private fun appendRelationshipRoleDiff(
        entries: MutableList<ModelDiffEntry>,
        left: ModelAggregate,
        right: ModelAggregate,
        scope: ModelDiffScope
    ) {
        val leftMap = collectRelationshipRoleSnapshots(left)
        val rightMap = collectRelationshipRoleSnapshots(right)
        val keys = (leftMap.keys + rightMap.keys).sortedWith(compareBy<RelationshipRolePathKey> { it.relationshipKey.value }.thenBy { it.roleKey.value })
        keys.forEach { pathKey ->
            val leftSnapshot = leftMap[pathKey]
            val rightSnapshot = rightMap[pathKey]
            val location = ModelDiffRelationshipRoleLocation(
                modelKey = left.key,
                relationshipKey = pathKey.relationshipKey,
                roleKey = pathKey.roleKey
            )
            if (leftSnapshot == null && rightSnapshot != null) {
                entries.add(ModelDiffEntry.Added(location = location, right = rightSnapshot))
            } else if (leftSnapshot != null && rightSnapshot == null) {
                entries.add(ModelDiffEntry.Deleted(location = location, left = leftSnapshot))
            } else if (leftSnapshot != null && rightSnapshot != null) {
                val changed = when (scope) {
                    ModelDiffScope.STRUCTURAL -> isRelationshipRoleStructuralChanged(leftSnapshot, rightSnapshot)
                    ModelDiffScope.COMPLETE -> leftSnapshot != rightSnapshot
                }
                if (changed) {
                    entries.add(ModelDiffEntry.Modified(location = location, left = leftSnapshot, right = rightSnapshot))
                }
            }
        }
    }

    private fun appendRelationshipAttributeDiff(
        entries: MutableList<ModelDiffEntry>,
        left: ModelAggregate,
        right: ModelAggregate,
        scope: ModelDiffScope
    ) {
        val leftMap = collectRelationshipAttributeSnapshots(left)
        val rightMap = collectRelationshipAttributeSnapshots(right)
        val keys = (leftMap.keys + rightMap.keys).sortedWith(compareBy<RelationshipAttributePathKey> { it.relationshipKey.value }.thenBy { it.attributeKey.value })
        keys.forEach { pathKey ->
            val leftSnapshot = leftMap[pathKey]
            val rightSnapshot = rightMap[pathKey]
            val location = ModelDiffRelationshipAttributeLocation(
                modelKey = left.key,
                relationshipKey = pathKey.relationshipKey,
                attributeKey = pathKey.attributeKey
            )
            if (leftSnapshot == null && rightSnapshot != null) {
                entries.add(ModelDiffEntry.Added(location = location, right = rightSnapshot))
            } else if (leftSnapshot != null && rightSnapshot == null) {
                entries.add(ModelDiffEntry.Deleted(location = location, left = leftSnapshot))
            } else if (leftSnapshot != null && rightSnapshot != null) {
                val changed = when (scope) {
                    ModelDiffScope.STRUCTURAL -> isRelationshipAttributeStructuralChanged(leftSnapshot, rightSnapshot)
                    ModelDiffScope.COMPLETE -> leftSnapshot != rightSnapshot
                }
                if (changed) {
                    entries.add(ModelDiffEntry.Modified(location = location, left = leftSnapshot, right = rightSnapshot))
                }
            }
        }
    }

    private fun toModelSnapshot(model: ModelAggregate): ModelDiffModelSnapshot {
        return ModelDiffModelSnapshot(
            key = model.key,
            name = model.name,
            description = model.description,
            version = model.version,
            origin = model.origin,
            authority = model.authority,
            documentationHome = model.documentationHome,
            tags = model.tags.sortedBy { it.value.toString() }
        )
    }

    private fun toTypeSnapshot(type: ModelType): ModelDiffTypeSnapshot {
        return ModelDiffTypeSnapshot(
            key = type.key,
            name = type.name,
            description = type.description
        )
    }

    private fun toEntitySnapshot(model: ModelAggregate, entity: Entity): ModelDiffEntitySnapshot {
        val identifierAttribute = model.findEntityAttributeOptional(entity.ref, entity.identifierAttributeId)
            ?: throw ModelDiffEntityIdentifierAttributeNotFoundException(model.key, entity.key, entity.identifierAttributeId)
        return ModelDiffEntitySnapshot(
            key = entity.key,
            name = entity.name,
            description = entity.description,
            identifierAttributeKey = identifierAttribute.key,
            origin = entity.origin,
            documentationHome = entity.documentationHome,
            tags = entity.tags.sortedBy { it.value.toString() }
        )
    }

    private fun toEntityAttributeSnapshot(model: ModelAggregate, attribute: Attribute): ModelDiffEntityAttributeSnapshot {
        val type = model.findType(attribute.typeId)
        return ModelDiffEntityAttributeSnapshot(
            key = attribute.key,
            name = attribute.name,
            description = attribute.description,
            typeKey = type.key,
            optional = attribute.optional,
            tags = attribute.tags.sortedBy { it.value.toString() }
        )
    }

    private fun toRelationshipSnapshot(relationship: Relationship): ModelDiffRelationshipSnapshot {
        return ModelDiffRelationshipSnapshot(
            key = relationship.key,
            name = relationship.name,
            description = relationship.description,
            tags = relationship.tags.sortedBy { it.value.toString() }
        )
    }

    private fun toRelationshipRoleSnapshot(model: ModelAggregate, role: RelationshipRole): ModelDiffRelationshipRoleSnapshot {
        val entity = model.findEntity(role.entityId)
        return ModelDiffRelationshipRoleSnapshot(
            key = role.key,
            entityKey = entity.key,
            name = role.name,
            cardinality = role.cardinality
        )
    }

    private fun toRelationshipAttributeSnapshot(model: ModelAggregate, attribute: Attribute): ModelDiffRelationshipAttributeSnapshot {
        val type = model.findType(attribute.typeId)
        return ModelDiffRelationshipAttributeSnapshot(
            key = attribute.key,
            name = attribute.name,
            description = attribute.description,
            typeKey = type.key,
            optional = attribute.optional,
            tags = attribute.tags.sortedBy { it.value.toString() }
        )
    }

    private fun collectEntityAttributeSnapshots(model: ModelAggregate): Map<EntityAttributePathKey, ModelDiffEntityAttributeSnapshot> {
        val map = linkedMapOf<EntityAttributePathKey, ModelDiffEntityAttributeSnapshot>()
        model.entities.forEach { entity ->
            val attributes = model.findEntityAttributes(entity.ref)
            attributes.forEach { attribute ->
                map[EntityAttributePathKey(entity.key, attribute.key)] = toEntityAttributeSnapshot(model, attribute)
            }
        }
        return map
    }

    private fun collectRelationshipRoleSnapshots(model: ModelAggregate): Map<RelationshipRolePathKey, ModelDiffRelationshipRoleSnapshot> {
        val map = linkedMapOf<RelationshipRolePathKey, ModelDiffRelationshipRoleSnapshot>()
        model.relationships.forEach { relationship ->
            relationship.roles.forEach { role ->
                map[RelationshipRolePathKey(relationship.key, role.key)] = toRelationshipRoleSnapshot(model, role)
            }
        }
        return map
    }

    private fun collectRelationshipAttributeSnapshots(model: ModelAggregate): Map<RelationshipAttributePathKey, ModelDiffRelationshipAttributeSnapshot> {
        val map = linkedMapOf<RelationshipAttributePathKey, ModelDiffRelationshipAttributeSnapshot>()
        model.relationships.forEach { relationship ->
            val attributes = model.findRelationshipAttributes(relationship.ref)
            attributes.forEach { attribute ->
                map[RelationshipAttributePathKey(relationship.key, attribute.key)] =
                    toRelationshipAttributeSnapshot(model, attribute)
            }
        }
        return map
    }

    private fun isModelStructuralChanged(left: ModelDiffModelSnapshot, right: ModelDiffModelSnapshot): Boolean {
        return left.key != right.key ||
                left.version != right.version ||
                left.origin != right.origin ||
                left.authority != right.authority
    }

    private fun isTypeStructuralChanged(left: ModelDiffTypeSnapshot, right: ModelDiffTypeSnapshot): Boolean {
        return left.key != right.key
    }

    private fun isEntityStructuralChanged(left: ModelDiffEntitySnapshot, right: ModelDiffEntitySnapshot): Boolean {
        return left.key != right.key ||
                left.identifierAttributeKey != right.identifierAttributeKey ||
                left.origin != right.origin
    }

    private fun isEntityAttributeStructuralChanged(
        left: ModelDiffEntityAttributeSnapshot,
        right: ModelDiffEntityAttributeSnapshot
    ): Boolean {
        return left.key != right.key ||
                left.typeKey != right.typeKey ||
                left.optional != right.optional
    }

    private fun isRelationshipStructuralChanged(
        left: ModelDiffRelationshipSnapshot,
        right: ModelDiffRelationshipSnapshot
    ): Boolean {
        return left.key != right.key
    }

    private fun isRelationshipRoleStructuralChanged(
        left: ModelDiffRelationshipRoleSnapshot,
        right: ModelDiffRelationshipRoleSnapshot
    ): Boolean {
        return left.key != right.key ||
                left.entityKey != right.entityKey ||
                left.cardinality != right.cardinality
    }

    private fun isRelationshipAttributeStructuralChanged(
        left: ModelDiffRelationshipAttributeSnapshot,
        right: ModelDiffRelationshipAttributeSnapshot
    ): Boolean {
        return left.key != right.key ||
                left.typeKey != right.typeKey ||
                left.optional != right.optional
    }

    private fun locationSortKey(location: ModelDiffLocation): String {
        return when (location) {
            is ModelDiffModelLocation -> location.modelKey.value
            is ModelDiffTypeLocation -> location.modelKey.value + "|" + location.typeKey.value
            is ModelDiffEntityLocation -> location.modelKey.value + "|" + location.entityKey.value
            is ModelDiffEntityAttributeLocation -> location.modelKey.value + "|" + location.entityKey.value + "|" + location.attributeKey.value
            is ModelDiffRelationshipLocation -> location.modelKey.value + "|" + location.relationshipKey.value
            is ModelDiffRelationshipRoleLocation -> location.modelKey.value + "|" + location.relationshipKey.value + "|" + location.roleKey.value
            is ModelDiffRelationshipAttributeLocation -> location.modelKey.value + "|" + location.relationshipKey.value + "|" + location.attributeKey.value
        }
    }

    private data class EntityAttributePathKey(
        val entityKey: EntityKey,
        val attributeKey: AttributeKey
    )

    private data class RelationshipRolePathKey(
        val relationshipKey: RelationshipKey,
        val roleKey: RelationshipRoleKey
    )

    private data class RelationshipAttributePathKey(
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey
    )
}

class ModelDiffEntityIdentifierAttributeNotFoundException(
    modelKey: ModelKey,
    entityKey: EntityKey,
    attributeId: AttributeId
) : MedatarunException(
    "Cannot build entity snapshot for model [${modelKey.value}] and entity [${entityKey.value}] because identifier attribute id [${attributeId.value}] was not found"
)
