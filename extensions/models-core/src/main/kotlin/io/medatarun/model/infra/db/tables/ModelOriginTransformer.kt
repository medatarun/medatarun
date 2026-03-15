package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.ModelOrigin
import org.jetbrains.exposed.v1.core.ColumnTransformer
import java.net.URI

class ModelOriginTransformer : ColumnTransformer<String, ModelOrigin> {
    override fun unwrap(value: ModelOrigin): String {
        return when (value) {
            is ModelOrigin.Manual -> "manual"
            is ModelOrigin.Uri -> value.uri.toString()
        }
    }

    override fun wrap(value: String): ModelOrigin {
        return if (value == "manual") ModelOrigin.Manual else ModelOrigin.Uri(URI(value))
    }
}
