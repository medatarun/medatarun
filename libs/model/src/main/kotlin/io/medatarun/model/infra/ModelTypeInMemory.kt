package io.medatarun.model.infra

import io.medatarun.model.model.LocalizedMarkdown
import io.medatarun.model.model.LocalizedText
import io.medatarun.model.model.ModelType
import io.medatarun.model.model.ModelTypeId

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
    }
}