package io.medatarun.model.ports.needs

import io.medatarun.model.domain.ModelAggregate
import io.medatarun.tags.core.domain.Tag

data class ModelImporterData(
    val model: ModelAggregate,
    val tags: List<Tag>
)