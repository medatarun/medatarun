package io.medatarun.kernel

import kotlin.reflect.KClass

data class MedatarunService<T: Any>(val serviceClass: KClass<T>, val implementation:T)
