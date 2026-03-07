package io.medatarun.actions.domain

import io.medatarun.actions.ports.needs.ActionDocSemanticsIntent

interface ActionSemantics {
    val intent: ActionDocSemanticsIntent
    val subjects: List<ActionSemanticsSubject>
}

interface ActionSemanticsSubject {
    val type: String
    val referencingParams: List<ActionSemanticsSubjectReferencingParam>
}

data class ActionSemanticsSubjectReferencingParam(
    val name: String,
    val kind: ActionSemanticsSubjectReferencingParamKind
)

enum class ActionSemanticsSubjectReferencingParamKind {
    ID,
    REF,
    KEY
}
