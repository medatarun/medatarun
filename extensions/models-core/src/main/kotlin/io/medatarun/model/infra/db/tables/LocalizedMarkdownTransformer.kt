package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.LocalizedMarkdown
import org.jetbrains.exposed.v1.core.ColumnTransformer

class LocalizedMarkdownTransformer : ColumnTransformer<String, LocalizedMarkdown> {
    override fun unwrap(value: LocalizedMarkdown): String {
        return value.name
    }

    override fun wrap(value: String): LocalizedMarkdown {
        return LocalizedMarkdown(value)
    }

}