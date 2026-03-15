package io.medatarun.model.infra.db.events

import io.medatarun.model.domain.ModelId

/**
 * Local state used to know which event number comes next for one model.
 *
 * It avoids re-reading the database between two event inserts done in the same
 * local append sequence.
 *
 * Example:
 * - current last event number in database: `7`
 * - one call to `ModelStorageDb.dispatch(...)` creates a context with `expectedRevision = 7`
 * - `nextRevision()` returns `8`
 * - after insert `8` succeeds, `onAppendCommitted(8)` is called
 * - the next insert in the same append sequence gets `9`
 */
class ModelEventStreamNumberContext(
    val modelId: ModelId,
    expectedRevision: Int
) {
    var expectedRevision: Int = expectedRevision
        private set

    /** Returns the number to use for the next event insert attempt. */
    fun nextRevision(): Int {
        return expectedRevision + 1
    }

    /** Records that one event insert succeeded with this number. */
    fun onAppendCommitted(streamRevision: Int) {
        expectedRevision = streamRevision
    }
}