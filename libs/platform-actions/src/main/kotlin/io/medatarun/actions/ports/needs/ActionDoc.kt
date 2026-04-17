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
    val securityRule: String,
    val semantics: ActionDocSemantics = ActionDocSemantics(
        mode = ActionDocSemanticsMode.AUTO
    )

)

