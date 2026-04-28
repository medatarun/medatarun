package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.LocalizedText
import org.jetbrains.exposed.v1.core.ColumnTransformer

class LocalizedTextTransformer: ColumnTransformer<String, LocalizedText> {
    override fun unwrap(value: LocalizedText): String {
        return value.name
    }

    override fun wrap(value: String): LocalizedText {
        return LocalizedText(value)
    }

}