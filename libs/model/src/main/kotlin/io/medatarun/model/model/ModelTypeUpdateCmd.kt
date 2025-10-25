package io.medatarun.model.model

sealed interface ModelTypeUpdateCmd {
    class Name(val value: LocalizedText?): ModelTypeUpdateCmd
    class Description(val value: LocalizedText?): ModelTypeUpdateCmd
}
