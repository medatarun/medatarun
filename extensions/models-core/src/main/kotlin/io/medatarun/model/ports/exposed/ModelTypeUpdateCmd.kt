package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText

sealed interface ModelTypeUpdateCmd {
    class Name(val value: LocalizedText?): ModelTypeUpdateCmd
    class Description(val value: LocalizedMarkdown?): ModelTypeUpdateCmd
}