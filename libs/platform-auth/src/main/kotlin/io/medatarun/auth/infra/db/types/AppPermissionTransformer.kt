package io.medatarun.auth.infra.db.types

import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionStringBased
import org.jetbrains.exposed.v1.core.ColumnTransformer

class AppPermissionTransformer: ColumnTransformer<String, AppPermission> {
    override fun unwrap(value: AppPermission): String {
        return value.key
    }

    override fun wrap(value: String): AppPermission {
        return AppPermissionStringBased(value)
    }
}