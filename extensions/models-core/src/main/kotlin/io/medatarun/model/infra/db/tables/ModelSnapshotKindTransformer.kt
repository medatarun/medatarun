package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.ModelSnapshotKind
import org.jetbrains.exposed.v1.core.ColumnTransformer

class ModelSnapshotKindTransformer : ColumnTransformer<String, ModelSnapshotKind> {
    override fun unwrap(value: ModelSnapshotKind): String {
        return value.code
    }

    override fun wrap(value: String): ModelSnapshotKind {
        return ModelSnapshotKind.valueOfCode(value)
    }
}
