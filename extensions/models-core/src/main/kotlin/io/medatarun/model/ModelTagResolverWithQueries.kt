package io.medatarun.model

import io.medatarun.model.domain.ModelId
import io.medatarun.model.ports.needs.ModelTagResolver
import io.medatarun.tags.core.domain.TagAttachScopeMismatchException
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.tags.core.domain.TagRef
import io.medatarun.tags.core.domain.TagScopeId
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.domain.TagScopeType

internal class ModelTagResolverWithQueries(
    private val tagQueries: TagQueries
) : ModelTagResolver {
    override fun resolveTagId(tagRef: TagRef): TagId {
        return tagQueries.findTagByRef(tagRef).id
    }

    override fun resolveTagIdUnsafe(tagRef: TagRef): TagId {
        return when(tagRef) {
            is TagRef.ById -> tagRef.id
            is TagRef.ByKey -> resolveTagId(tagRef)
        }
    }

    override fun resolveTagIdCompatible(
        modelId: ModelId,
        tagRef: TagRef
    ): TagId {
        val tag = tagQueries.findTagByRef(tagRef)
        if (tag.scope.isGlobal) {
            return tag.id
        }
        val targetScopeRef = TagScopeRef.Local(
            type = TagScopeType("model"),
            localScopeId = TagScopeId(modelId.value)
        )
        if (tag.scope != targetScopeRef) {
            throw TagAttachScopeMismatchException(
                targetScope = targetScopeRef.asString(),
                tagScope = tag.scope.asString(),
                tagRef = tagRef.asString()
            )
        }
        return tag.id
    }
}