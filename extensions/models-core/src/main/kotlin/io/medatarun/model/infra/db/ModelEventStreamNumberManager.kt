package io.medatarun.model.infra.db

import io.medatarun.model.domain.ModelEventConcurrentWriteException
import io.medatarun.model.domain.ModelId
import io.medatarun.model.infra.db.events.ModelEventStreamNumberContext
import io.medatarun.model.infra.db.tables.ModelEventTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException

/**
 * Helps [ModelStorageDb] choose the right event number for one model.
 *
 * This class only does three things:
 * - read the last event number already stored for a model,
 * - remember which number should be used next during one local append sequence,
 * - recognize a JDBC uniqueness error and turn it into the domain exception.
 *
 * It does not insert rows itself. The SQL insert stays in [ModelStorageDb].
 */
class ModelEventStreamNumberManager {

    /**
     * Creates a local context for writing events of one model.
     *
     * The context remembers the last event number seen in the database when
     * [ModelStorageDb] starts one append sequence.
     *
     * Example:
     * - the database already contains events `1`, `2`, `3` for model `crm`
     * - `createNumberContext(crm)` starts with `expectedRevision = 3`
     * - the next event written in this transaction must use number `4`
     *
     * This context lives only for the current append sequence. In practice
     * today, that means one call to `ModelStorageDb.dispatch(...)`.
     *
     * Another call to `ModelStorageDb.dispatch(...)` in the same outer SQL
     * transaction will create its own context again from the database state
     * visible at that moment.
     */
    fun createNumberContext(modelId: ModelId): ModelEventStreamNumberContext {
        return ModelEventStreamNumberContext(
            modelId = modelId,
            expectedRevision = currentStreamRevision(modelId)
        )
    }

    private fun currentStreamRevision(modelId: ModelId): Int {
        val currentMax = ModelEventTable.selectAll()
            .where { ModelEventTable.modelId eq modelId }
            .maxOfOrNull { it[ModelEventTable.streamRevision] }
        return currentMax ?: 0
    }

    /**
     * Checks whether a failed insert was caused by another writer taking the
     * same event number first.
     *
     * Example:
     * - this transaction tries to insert event number `4`
     * - another transaction already inserted event number `4`
     * - the SQL uniqueness constraint fails
     * - this method throws `ModelEventConcurrentWriteException`
     *
     * If the error is unrelated, this method does nothing and the caller keeps
     * the original exception.
     */
    fun rethrowIfStreamRevisionConflict(
        exception: Throwable,
        numberContext: ModelEventStreamNumberContext,
        conflictingRevision: Int
    ) {
        if (isStreamRevisionConflict(exception)) {
            throw ModelEventConcurrentWriteException(
                modelId = numberContext.modelId,
                expectedRevision = numberContext.expectedRevision,
                conflictingRevision = conflictingRevision
            )
        }
    }

    /**
     * Updates the local context after one event insert succeeded.
     *
     * This matters when the same append sequence writes several events in a row.
     * That is the target behavior for `create` and `import` like sequences where one dispatch creates many events.
     *
     * Example:
     * - the context starts at `3`
     * - the first inserted event uses number `4`
     * - `onAppendCommitted(..., 4)` records that success
     * - the next event in the same append sequence will use number `5`
     */
    fun onAppendCommitted(
        numberContext: ModelEventStreamNumberContext,
        streamRevision: Int
    ) {
        numberContext.onAppendCommitted(streamRevision)
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

    /**
     * In JDBC, SQLState values starting with `23` mean "integrity constraint violation".
     */
    private fun isIntegrityConstraintViolationSqlState(sqlState: String?): Boolean {
        return sqlState != null && sqlState.startsWith("23")
    }
}

