package io.medatarun.model.infra

import io.medatarun.model.domain.*

/**
 * Default implementation of ModelType
 */
data class ModelTypeInMemory(
    override val id: TypeId,
    override val key: TypeKey,
    override val name: TextSingleLine?,
    override val description: TextMarkdown?,
) : ModelType {
    companion object {
        fun of(key: String): ModelTypeInMemory {
            return ModelTypeInMemory(TypeId.generate(), TypeKey(key), null, null)
        }

        fun of(other: ModelType): ModelTypeInMemory {
            return ModelTypeInMemory(id = other.id, key = other.key, name = other.name, description = other.description)
        }
    }
}