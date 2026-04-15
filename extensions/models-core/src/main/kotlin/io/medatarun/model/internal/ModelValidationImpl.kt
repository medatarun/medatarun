package io.medatarun.model.internal

import io.medatarun.model.domain.*

class ModelValidationImpl : ModelValidation {

    override fun validate(model: ModelAggregate): ModelValidationState {

        val errors = ensureEachAttributeHasKnownType(model) +
                ensureTypeKeyUniqueInModel(model) +
                ensureEntityKeyUniqueInModel(model) +
                ensureEntityAttributeKeyUniqueInEntity(model) +
                ensureRelationshipKeyUniqueInModel(model) +
                ensureRelationshipAttributeKeyUniqueInRelationship(model)

        return if (errors.isEmpty()) ModelValidationState.Ok() else ModelValidationState.Error(errors)
    }

    /**
     * each attribute must have a known type
     */
    private fun ensureEachAttributeHasKnownType(model: ModelAggregate): List<ModelValidationError> {
        val errors = mutableListOf<ModelValidationError>()
        // each attribute must have a known type
        model.attributes.forEach { attr ->
            if (model.findTypeOptional(attr.typeId) == null) {
                val key = when(val ownerId = attr.ownerId) {
                    is AttributeOwnerId.OwnerEntityId -> model.findEntity(ownerId.id).key
                    is AttributeOwnerId.OwnerRelationshipId -> model.findRelationship(ownerId.id).key
                }
                errors.add(ModelValidationErrorTypeNotFound(model.key, key, attr.key, attr.typeId))
            }
        }
        return errors
    }

    private fun ensureEntityKeyUniqueInModel(model: ModelAggregate): List<ModelValidationError> {
        val duplicates = findDuplicateKeys(model.entities) { entity -> entity.key }
        return duplicates.map { key -> ModelValidationErrorDuplicateEntityKey(model.key, key) }
    }

    private fun ensureEntityAttributeKeyUniqueInEntity(model: ModelAggregate): List<ModelValidationError> {
        val errors = mutableListOf<ModelValidationError>()
        model.entities.forEach { entity ->
            val duplicateKeys = findDuplicateKeys(model.findEntityAttributes(entity.ref)) { attribute -> attribute.key }
            duplicateKeys.forEach { attributeKey ->
                errors.add(ModelValidationErrorDuplicateEntityAttributeKey(model.key, entity.key, attributeKey))
            }
        }
        return errors
    }

    private fun ensureRelationshipKeyUniqueInModel(model: ModelAggregate): List<ModelValidationError> {
        val duplicates = findDuplicateKeys(model.relationships) { relationship -> relationship.key }
        return duplicates.map { key -> ModelValidationErrorDuplicateRelationshipKey(model.key, key) }
    }

    private fun ensureRelationshipAttributeKeyUniqueInRelationship(model: ModelAggregate): List<ModelValidationError> {
        val errors = mutableListOf<ModelValidationError>()
        model.relationships.forEach { relationship ->
            val duplicateKeys = findDuplicateKeys(model.findRelationshipAttributes(relationship.ref)) { attribute -> attribute.key }
            duplicateKeys.forEach { attributeKey ->
                errors.add(
                    ModelValidationErrorDuplicateRelationshipAttributeKey(
                        model.key,
                        relationship.key,
                        attributeKey
                    )
                )
            }
        }
        return errors
    }

    private fun ensureTypeKeyUniqueInModel(model: ModelAggregate): List<ModelValidationError> {
        val duplicates = findDuplicateKeys(model.types) { type -> type.key }
        return duplicates.map { key -> ModelValidationErrorDuplicateTypeKey(model.key, key) }
    }

    private fun <T, K> findDuplicateKeys(items: List<T>, keySelector: (T) -> K): Set<K> {
        val byKey = mutableMapOf<K, Int>()
        items.forEach { item ->
            val key = keySelector(item)
            val count = byKey[key] ?: 0
            byKey[key] = count + 1
        }
        val duplicates = mutableSetOf<K>()
        byKey.entries.forEach { entry ->
            if (entry.value > 1) {
                duplicates.add(entry.key)
            }
        }
        return duplicates
    }
}
