package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.ModelAuthority
import org.jetbrains.exposed.v1.core.ColumnTransformer

class ModelAuthorityTransformer : ColumnTransformer<String, ModelAuthority> {
    override fun unwrap(value: ModelAuthority): String {
        return value.code
    }

    override fun wrap(value: String): ModelAuthority {
        return ModelAuthority.valueOfCode(value)
    }
}
