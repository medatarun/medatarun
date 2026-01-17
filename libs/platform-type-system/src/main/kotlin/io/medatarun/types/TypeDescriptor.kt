package io.medatarun.types

import kotlin.reflect.KClass

interface TypeDescriptor<T:Any> {
    val target: KClass<T>
    fun validate(value: T):T
    val equivMultiplatorm: String
    val equivJson: TypeJsonEquiv
    val redacted: Boolean get() = false
    val description: String get() = ""
    val jsonConverter: TypeJsonConverter<T>? get() = null
}