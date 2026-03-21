package io.medatarun.model.ports.needs

import io.medatarun.model.domain.ModelId
import io.medatarun.security.AppTraceabilityRecord
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagKey
import io.medatarun.tags.core.domain.TagRef
import io.medatarun.tags.core.domain.TagScopeId
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.domain.TagScopeType
import io.medatarun.tags.core.domain.Tag

/**
 * Port used by model commands to resolve tags and validate attachment rules
 * without depending directly on tags-core services.
 */
interface ModelTagResolver {

    /**
     * Resolves this [tagRef] as a [TagId] whatever the tag scope is.
     *
     * Error is thrown if the tagRef doesn't match an existing tag
     */
    fun resolveTagId(tagRef: TagRef): TagId

    /**
     * Finds a [Tag] by its identifier.
     *
     * Error is thrown if the id does not match an existing tag.
     */
    fun findTagById(tagId: TagId): Tag

    /**
     * Resolves this [tagRef] as a [TagId] whatever the tag scope is.
     *
     * When the [tagRef] is already a [TagRef.ById], no lookup is done. It means the tag may not exist at all, which can be
     * helpful where performance is preferred to consistency.
     *
     * When the [tagRef] is a [TagRef.ByKey] the key must be resolved, so an error will be thrown if the key doesn't
     * exist, because we cannot guess the id otherwise.
     */
    fun resolveTagIdUnsafe(tagRef: TagRef): TagId

    /**
     * Resolves the [tagRef] as a [TagId]
     * if the ref is either global (and exists)
     * or either a local tag for specified [modelId]
     */
    fun resolveTagIdCompatible(modelId: ModelId, tagRef: TagRef): TagId

    /**
     * Create a new tag in the local scope of the model.
     *
     * The caller must forward the traceability record that triggered the model command so the tag
     * layer keeps the original call identity.
     */
    fun create(traceabilityRecord: AppTraceabilityRecord, modelId: ModelId, key: TagKey, name: String?, description: String?)

    companion object {
        val modelTagScopeType = TagScopeType("model")
        fun modelTagScopeRef(modelId: ModelId) = TagScopeRef.Local(
            type = modelTagScopeType,
            localScopeId = TagScopeId(modelId.value)
        )
    }


}
