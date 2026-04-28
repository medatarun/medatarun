package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.TextSingleLine
import org.jetbrains.exposed.v1.core.ColumnTransformer

class TextSingleLineTransformer: ColumnTransformer<String, TextSingleLine> {
    override fun unwrap(value: TextSingleLine): String {
        return value.name
    }

    override fun wrap(value: String): TextSingleLine {
        return TextSingleLine(value)
    }

}