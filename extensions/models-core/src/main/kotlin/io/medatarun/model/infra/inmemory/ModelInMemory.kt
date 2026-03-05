package io.medatarun.model.infra.inmemory

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelAggregate
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelOrigin
import io.medatarun.model.domain.ModelVersion
import java.net.URL

data class ModelInMemory(
    override val id: ModelId,
    override val key: ModelKey,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
    override val version: ModelVersion,
    override val origin: ModelOrigin,
    override val documentationHome: URL?,
): Model {
    companion object {
        fun of(other: Model): ModelInMemory {
            return ModelInMemory(
                id = other.id,
                key = other.key,
                name = other.name,
                description = other.description,
                version = other.version,
                origin = other.origin,
                documentationHome = other.documentationHome
            )
        }
    }
}