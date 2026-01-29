package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.TypeKey

sealed interface ModelTypeUpdateCmd {
    class Name(val value: LocalizedText?): ModelTypeUpdateCmd
    class Key(val value: TypeKey): ModelTypeUpdateCmd
    class Description(val value: LocalizedMarkdown?): ModelTypeUpdateCmd
}