package io.medatarun.actions.runtime

@Target(AnnotationTarget.PROPERTY)
annotation class ActionParamDoc(
    val name: String,
    val description: String = "",
    val order: Int = 0
)
