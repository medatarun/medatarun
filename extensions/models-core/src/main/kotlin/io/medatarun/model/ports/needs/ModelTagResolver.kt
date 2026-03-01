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
     * Resolves this [tagRef] as a [TagId] whatever the tag scope is
     */
    fun resolveTagId(tagRef: TagRef): TagId

    /**
     * Resolves the [tagRef] as a [TagId]
     * if the ref is either global (and exists)
     * or either a local tag for specified [modelId]
     */
    fun resolveTagIdCompatible(modelId: ModelId, tagRef: TagRef): TagId
}
