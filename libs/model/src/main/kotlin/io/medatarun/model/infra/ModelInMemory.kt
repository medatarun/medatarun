package io.medatarun.model.infra

import io.medatarun.model.model.LocalizedMarkdown
import io.medatarun.model.model.LocalizedText
import io.medatarun.model.model.Model
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.ModelType
import io.medatarun.model.model.ModelVersion
import javax.print.attribute.standard.MediaSize

/**
 * Default implementation of Model
 */
data class ModelInMemory(
    override val id: ModelId,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
    override val version: ModelVersion,
    override val types: List<ModelTypeInMemory>,
    override val entityDefs: List<EntityDefInMemory>
) : Model {

    companion object {
        fun of(other: Model): ModelInMemory {
            return ModelInMemory(
                id = other.id,
                name = other.name,
                description = other.description,
                version = other.version,
                types = other.types.map(ModelTypeInMemory::of),
                entityDefs = other.entityDefs.map(EntityDefInMemory::of)
            )
        }
    }
}