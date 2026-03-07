package io.medatarun.actions.ports.needs

@Target()
annotation class ActionDocSemantics(
    val mode: ActionDocSemanticsMode = ActionDocSemanticsMode.AUTO,
    val intent: ActionDocSemanticsIntent = ActionDocSemanticsIntent.OTHER,
    val subjects: Array<String> = []
)