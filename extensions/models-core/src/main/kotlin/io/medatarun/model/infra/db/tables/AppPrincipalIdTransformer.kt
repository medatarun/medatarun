package io.medatarun.model.infra.db.tables

import io.medatarun.security.AppPrincipalId
import org.jetbrains.exposed.v1.core.ColumnTransformer

class AppPrincipalIdTransformer : ColumnTransformer<String, AppPrincipalId> {
    override fun unwrap(value: AppPrincipalId): String {
        return value.value
    }

    override fun wrap(value: String): AppPrincipalId {
        return AppPrincipalId(value)
    }
}
