package io.medatarun.lang.uuid

import java.util.*

object UuidUtils {

    fun generateV7(): UUID {
        return UUID.randomUUID()
    }

    fun generateV4String(): String {
        return UUID.randomUUID().toString()
    }

    fun fromString(value: String): UUID {
        return UUID.fromString(value)
    }
}