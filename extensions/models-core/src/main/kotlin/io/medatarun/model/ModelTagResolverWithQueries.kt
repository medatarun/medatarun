package io.medatarun.model

import io.medatarun.model.domain.ModelId
import io.medatarun.model.ports.needs.ModelTagResolver
import io.medatarun.model.ports.needs.ModelTagResolver.Companion.modelTagScopeRef
import io.medatarun.model.ports.needs.ModelTagResolver.Companion.modelTagScopeType
import io.medatarun.tags.core.domain.*

internal class ModelTagResolverWithQueries(
    private val tagQueries: TagQueries,
    private val tagCmds: TagCmds
) : ModelTagResolver {
    override fun findTagById(tagId: TagId): Tag {
        val tag = tagQueries.findTagByIdOptional(tagId)
        return tag ?: throw TagNotFoundException(tagId.asString())
    }

    override fun resolveTagId(tagRef: TagRef): TagId {
        return tagQueries.findTagByRef(tagRef).id
    }

    override fun resolveTagIdUnsafe(tagRef: TagRef): TagId {
        return when (tagRef) {
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
            type = modelTagScopeType,
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

    override fun create(modelId: ModelId, key: TagKey, name: String?, description: String?) {
        tagCmds.dispatch(
            TagCmd.TagFreeCreate(
                scopeRef = modelTagScopeRef(modelId),
                key = key, name = name, description = description
            )
        )
    }
}
