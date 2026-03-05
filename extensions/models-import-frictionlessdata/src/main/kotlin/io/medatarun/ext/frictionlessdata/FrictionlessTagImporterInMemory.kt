package io.medatarun.ext.frictionlessdata

import io.medatarun.model.domain.ModelId
import io.medatarun.model.ports.needs.ModelTagResolver
import io.medatarun.tags.core.domain.Tag
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagKey
import io.medatarun.tags.core.internal.TagInMemory
import io.medatarun.type.commons.id.Id

class FrictionlessTagImporterInMemory(
) : FrictionlessTagImporter {

    val tags = mutableMapOf<String, TagInMemory>()

    override fun importModelScopeTags(modelId: ModelId, keywords: List<String>): List<TagId> {
        val scopeRef = ModelTagResolver.modelTagScopeRef(modelId)
        return keywords
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .map { keyword ->
                tags.getOrPut(keyword) {
                    TagInMemory(
                        id = Id.generate(::TagId),
                        key = TagKey(keyword),
                        scope = scopeRef,
                        groupId = null,
                        name = null,
                        description = null
                    )
                }.id
            }
    }

    override fun getAllCollectedTags(): List<Tag> {
        return tags.values.toList()
    }
}