package io.medatarun.model.infra.db.snapshots

import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelSnapshotKind
import io.medatarun.model.domain.ModelSnapshotId
import io.medatarun.model.infra.db.tables.ModelSnapshotTable
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq

/**
 * Selects the root model snapshot a reader should use to reconstruct storage state.
 */
sealed interface SnapshotSelector {
    fun criterion(): Op<Boolean>

    data class ById(
        val modelSnapshotId: ModelSnapshotId
    ) : SnapshotSelector {
        override fun criterion(): Op<Boolean> {
            return ModelSnapshotTable.id eq modelSnapshotId
        }
    }

    data class CurrentHeadByModelId(
        val modelId: ModelId
    ) : SnapshotSelector {
        override fun criterion(): Op<Boolean> {
            return (ModelSnapshotTable.modelId eq modelId) and
                (ModelSnapshotTable.snapshotKind eq ModelSnapshotKind.CURRENT_HEAD)
        }
    }

    data class CurrentHeadByKey(
        val key: ModelKey
    ) : SnapshotSelector {
        override fun criterion(): Op<Boolean> {
            return (ModelSnapshotTable.key eq key) and
                (ModelSnapshotTable.snapshotKind eq ModelSnapshotKind.CURRENT_HEAD)
        }
    }
}
