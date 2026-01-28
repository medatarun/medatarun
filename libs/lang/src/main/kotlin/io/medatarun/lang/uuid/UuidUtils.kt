package io.medatarun.lang.uuid

import com.github.f4b6a3.uuid.UuidCreator
import java.util.*

object UuidUtils {

    fun generateV7(): UUID {
        return UuidCreator.getTimeOrderedEpoch()
    }

    fun generateV4String(): String {
        return UUID.randomUUID().toString()
    }

    fun fromString(value: String): UUID {
        return UUID.fromString(value)
    }
}