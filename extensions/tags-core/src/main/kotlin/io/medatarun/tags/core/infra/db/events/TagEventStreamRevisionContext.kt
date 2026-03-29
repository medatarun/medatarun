package io.medatarun.tags.core.infra.db.events

import io.medatarun.tags.core.domain.TagScopeId

/**
 * Local state for one append attempt on the global tag event stream.
 */
class TagEventStreamRevisionContext(
    val scopeType: String,
    val scopeId: TagScopeId?,
    expectedRevision: Int
) {
    var expectedRevision: Int = expectedRevision
        private set

    fun nextRevision(): Int {
        return expectedRevision + 1
    }

    fun onAppendCommitted(streamRevision: Int) {
        expectedRevision = streamRevision
    }
}
