package io.medatarun.platform.db.jdbc

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.DbDialect
import io.medatarun.type.commons.instant.InstantAdapters
import java.nio.ByteBuffer
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.time.Instant
import java.util.UUID

fun PreparedStatement.setUUID(
    parameterIndex: Int,
    uuid: UUID?,
    dialect: DbDialect = DbDialect.SQLITE
) {
    if (uuid == null) {
        when (dialect) {
            DbDialect.POSTGRESQL -> this.setNull(parameterIndex, Types.OTHER)
            DbDialect.SQLITE -> this.setNull(parameterIndex, Types.BLOB)
        }
        return
    }
    when (dialect) {
        DbDialect.POSTGRESQL -> this.setObject(parameterIndex, uuid)
        DbDialect.SQLITE -> this.setBytes(parameterIndex, toBytes(uuid))
    }
}

fun PreparedStatement.setInstantSQLite(parameterIndex: Int, value: Instant?) {
    if (value == null) {
        this.setNull(parameterIndex, Types.INTEGER)
        return
    }
    this.setLong(parameterIndex, value.toEpochMilli())
}

fun ResultSet.getUuidFromString(columnName: String): UUID? {
    val value = this.getString(columnName) ?: return null
    return UuidUtils.fromString(value)
}

fun ResultSet.getUuid(columnName: String, dialect: DbDialect = DbDialect.SQLITE): UUID? {
    return when (dialect) {
        DbDialect.POSTGRESQL -> {
            val value = this.getObject(columnName) ?: return null
            if (value is UUID) {
                return value
            }
            if (value is String) {
                return UuidUtils.fromString(value)
            }
            UuidUtils.fromString(value.toString())
        }
        DbDialect.SQLITE -> {
            val bytes = this.getBytes(columnName) ?: return null
            fromBytes(bytes)
        }
    }
}

private fun toBytes(value: UUID): ByteArray {
    val buffer = ByteBuffer.allocate(UUID_BINARY_SIZE)
    buffer.putLong(value.mostSignificantBits)
    buffer.putLong(value.leastSignificantBits)
    return buffer.array()
}

private fun fromBytes(value: ByteArray): UUID {
    val buffer = ByteBuffer.wrap(value)
    val mostSignificantBits = buffer.getLong()
    val leastSignificantBits = buffer.getLong()
    return UUID(mostSignificantBits, leastSignificantBits)
}

private const val UUID_BINARY_SIZE = 16
