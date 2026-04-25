package io.medatarun.auth.infra.db.types

import io.medatarun.auth.domain.ActorPermission
import org.jetbrains.exposed.v1.core.ColumnTransformer

class ActorPermissionTransformer: ColumnTransformer<String, ActorPermission> {
    override fun unwrap(value: ActorPermission): String {
        return value.key
    }

    override fun wrap(value: String): ActorPermission {
        return ActorPermission(value)
    }
}