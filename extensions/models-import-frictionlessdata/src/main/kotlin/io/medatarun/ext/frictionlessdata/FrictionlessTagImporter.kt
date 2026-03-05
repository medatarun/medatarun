package io.medatarun.ext.frictionlessdata

import io.medatarun.model.domain.ModelId
import io.medatarun.tags.core.domain.Tag
import io.medatarun.tags.core.domain.TagId

interface FrictionlessTagImporter {
    fun importModelScopeTags(modelId: ModelId, keywords: List<String>): List<TagId>
    fun getAllCollectedTags(): List<Tag>
}