package io.medatarun.lang.idconv

import io.medatarun.lang.exceptions.MedatarunTechnicalException


class IdConv<T>(val name: String, val factory: () -> T) {
    val map = mutableMapOf<T, T>()
    fun generate(old: T): T {
        val newId = factory()
        map[old] = newId
        return newId
    }

    fun register(old: T, new: T) {
        map[old] = new
    }

    fun convert(old: T): T {
        return map[old] ?: throw IdConversionFailedException(name, old.toString())
    }
}

class IdConversionFailedException(name:String, old:String): MedatarunTechnicalException("Identifier conversion failed. Can not find matching identifier for $old in $name")