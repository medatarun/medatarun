package io.medatarun.model.ports.needs

import io.medatarun.model.domain.ModelId
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagRef

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
}
