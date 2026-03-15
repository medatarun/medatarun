package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.EntityOrigin
import org.jetbrains.exposed.v1.core.ColumnTransformer
import java.net.URI

class EntityOriginTransformer : ColumnTransformer<String, EntityOrigin> {
    override fun unwrap(value: EntityOrigin): String {
        return when (value) {
            is EntityOrigin.Manual -> "manual"
            is EntityOrigin.Uri -> value.uri.toString()
        }
    }

    override fun wrap(value: String): EntityOrigin {
        return if (value == "manual") EntityOrigin.Manual else EntityOrigin.Uri(URI(value))
    }
}
