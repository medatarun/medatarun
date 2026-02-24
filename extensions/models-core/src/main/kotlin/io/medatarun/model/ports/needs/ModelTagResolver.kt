package io.medatarun.model.ports.needs

import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagRef

/**
 * Minimal port used by model commands to resolve a tag reference to its stable identifier.
 *
 * The implementation may later choose to query existing tags only or create missing tags,
 * depending on the policy needed by the caller.
 */
interface ModelTagResolver {
    fun resolveTagId(tagRef: TagRef): TagId
}
