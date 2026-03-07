package io.medatarun.actions.ports.needs

enum class ActionDocSemanticsIntent(
    val code: String
) {
    CREATE("create"),
    READ("read"),
    UPDATE("update"),
    DELETE("delete"),
    OTHER("other")
}