package io.medatarun.lang.uuid

import com.github.f4b6a3.uuid.UuidCreator
import com.github.f4b6a3.uuid.util.UuidUtil
import com.github.f4b6a3.uuid.enums.UuidVersion
import com.github.f4b6a3.uuid.exception.InvalidUuidException
import com.github.f4b6a3.uuid.util.UuidValidator
import io.medatarun.lang.exceptions.MedatarunUserException
import java.time.Instant
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

    fun getInstant(uuid: UUID): Instant {
        return UuidUtil.getInstant(uuid)
    }

    fun fromStringSafe(value: String): UUID {
        val uuid = UuidCreator.fromString(value)
        if (!UuidValidator.isValid(
                uuid,
                UuidVersion.VERSION_TIME_ORDERED_EPOCH.value
            )
        ) throw InvalidUUIDv7Exception(value)
        return uuid
    }

    class InvalidUUIDv7Exception(value: String): MedatarunUserException("Invalid uuid v7: [$value]")
}
