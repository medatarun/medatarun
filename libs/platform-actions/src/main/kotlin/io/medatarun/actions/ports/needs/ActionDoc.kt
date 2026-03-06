package io.medatarun.actions.ports.needs

/**
 * Documentation of a command
 */
@Target(AnnotationTarget.CLASS)
annotation class ActionDoc(
    val key: String,
    val title: String,
    /**
     * Description intended for end-users (API developers, cli users). Can contain Markdown.
     */
    val description: String = "",
    val uiLocations: Array<String>,
    val securityRule: String,
    val semantics: ActionSemantics = ActionSemantics(
        mode = ActionSemanticsMode.AUTO
    )

)

@Target()
annotation class ActionSemantics(
    val mode: ActionSemanticsMode = ActionSemanticsMode.AUTO
)

enum class ActionSemanticsMode {
    AUTO, NONE, UNKNOWN
}