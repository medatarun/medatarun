package io.medatarun.model.infra

import io.medatarun.model.model.LocalizedMarkdown
import io.medatarun.model.model.LocalizedText
import io.medatarun.model.model.Model
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.ModelType
import io.medatarun.model.model.ModelVersion

/**
 * Default implementation of Model
 */
data class ModelInMemory(
    override val id: ModelId,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
    override val version: ModelVersion,
    override val types: List<ModelType>,
    override val entityDefs: List<EntityDefInMemory>
) : Model