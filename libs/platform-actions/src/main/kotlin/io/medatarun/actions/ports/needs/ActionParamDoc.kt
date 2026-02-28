package io.medatarun.actions.ports.needs

@Target(AnnotationTarget.PROPERTY)
annotation class ActionParamDoc(
    val name: String,
    /**
     * Description for end users. Can contain Markdown format.
     */
    val description: String = "",
    val order: Int = 0
)
