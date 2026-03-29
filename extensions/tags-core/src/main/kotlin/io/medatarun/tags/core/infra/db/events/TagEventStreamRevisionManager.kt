package io.medatarun.tags.core.infra.db.events

import io.medatarun.tags.core.domain.TagScopeId
import io.medatarun.tags.core.infra.db.TagEventConcurrentWriteException
import io.medatarun.tags.core.infra.db.tables.TagEventTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.jdbc.select
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException

/**
 * Computes revisions per tag event scope stream (`scope_type`, `scope_id`).
 */
class TagEventStreamRevisionManager {

    fun createRevisionContext(scopeType: String, scopeId: TagScopeId?): TagEventStreamRevisionContext {
        return TagEventStreamRevisionContext(
            scopeType = scopeType,
            scopeId = scopeId,
            expectedRevision = currentStreamRevision(scopeType, scopeId)
        )
    }

    private fun currentStreamRevision(scopeType: String, scopeId: TagScopeId?): Int {
        val currentMax = TagEventTable.streamRevision.max()
        val query = TagEventTable.select(currentMax)
        val row = if (scopeId == null) {
            query.where {
                (TagEventTable.scopeType eq scopeType) and (TagEventTable.scopeId eq null)
            }.single()
        } else {
            query.where {
                (TagEventTable.scopeType eq scopeType) and (TagEventTable.scopeId eq scopeId)
            }.single()
        }
        return row[currentMax] ?: 0
    }

    fun rethrowIfStreamRevisionConflict(
        exception: Throwable,
        revisionContext: TagEventStreamRevisionContext,
        conflictingRevision: Int
    ) {
        if (isStreamRevisionConflict(exception)) {
            throw TagEventConcurrentWriteException(
                expectedRevision = revisionContext.expectedRevision,
                conflictingRevision = conflictingRevision
            )
        }
    }

    fun onAppendCommitted(
        revisionContext: TagEventStreamRevisionContext,
        streamRevision: Int
    ) {
        revisionContext.onAppendCommitted(streamRevision)
    }

    private fun isStreamRevisionConflict(exception: Throwable): Boolean {
        var current: Throwable? = exception
        while (current != null) {
            if (current is SQLIntegrityConstraintViolationException) {
                return true
            }
            if (current is SQLException && isIntegrityConstraintViolationSqlState(current.sqlState)) {
                return true
            }
            current = current.cause
        }
        return false
    }

    private fun isIntegrityConstraintViolationSqlState(sqlState: String?): Boolean {
        return sqlState != null && sqlState.startsWith("23")
    }
}
