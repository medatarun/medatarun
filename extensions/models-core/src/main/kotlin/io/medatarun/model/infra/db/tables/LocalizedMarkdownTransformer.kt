package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.TextMarkdown
import org.jetbrains.exposed.v1.core.ColumnTransformer

class LocalizedMarkdownTransformer : ColumnTransformer<String, TextMarkdown> {
    override fun unwrap(value: TextMarkdown): String {
        return value.name
    }

    override fun wrap(value: String): TextMarkdown {
        return TextMarkdown(value)
    }

}