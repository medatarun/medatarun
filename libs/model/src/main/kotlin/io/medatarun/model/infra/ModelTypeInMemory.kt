package io.medatarun.model.infra

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelType
import io.medatarun.model.domain.ModelTypeId

/**
 * Default implementation of ModelType
 */
data class ModelTypeInMemory(
    override val id: ModelTypeId,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
) : ModelType {
    companion object {
        fun of(id: String): ModelTypeInMemory {
            return ModelTypeInMemory(ModelTypeId(id), null, null)
        }
        fun of(other: ModelType): ModelTypeInMemory {
            return ModelTypeInMemory(id = other.id, name = other.name, description = other.description)
        }
    }
}