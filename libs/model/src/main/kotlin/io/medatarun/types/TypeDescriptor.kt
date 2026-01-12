package io.medatarun.types

import kotlin.reflect.KClass

interface TypeDescriptor<T:Any> {
    val target: KClass<T>
    fun validate(value: T):T
    val equivMultiplatorm: String
    val equivJson: JsonTypeEquiv
    val redacted: Boolean get() = false
}