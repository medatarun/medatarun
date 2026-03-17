package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.ModelVersion
import org.jetbrains.exposed.v1.core.ColumnTransformer

object ModelVersionTransformer : ColumnTransformer<String, ModelVersion> {
    override fun unwrap(value: ModelVersion): String {
        return value.asString()
    }

    override fun wrap(value: String): ModelVersion {
        return ModelVersion(value)
    }

}