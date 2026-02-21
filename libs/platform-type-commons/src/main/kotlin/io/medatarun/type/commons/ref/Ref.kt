package io.medatarun.type.commons.ref

import io.medatarun.type.commons.id.Id
import io.medatarun.type.commons.key.Key

interface Ref<T : Ref<T>> {
    fun asString(): String
    sealed interface RefId<T: Id<T>>
    sealed interface RefKey<T: Key<T>>
}