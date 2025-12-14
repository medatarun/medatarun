package io.medatarun.actions.ports.needs

@Target(AnnotationTarget.PROPERTY)
annotation class ActionParamDoc(
    val name: String,
    val description: String = "",
    val order: Int = 0
)
