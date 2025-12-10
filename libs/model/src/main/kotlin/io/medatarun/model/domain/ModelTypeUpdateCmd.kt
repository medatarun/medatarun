package io.medatarun.model.domain

sealed interface ModelTypeUpdateCmd {
    class Name(val value: LocalizedText?): ModelTypeUpdateCmd
    class Description(val value: LocalizedText?): ModelTypeUpdateCmd
}
