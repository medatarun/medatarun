package io.medatarun.platform.db.jdbc

import io.medatarun.lang.uuid.UuidUtils
import java.nio.ByteBuffer
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.util.UUID

fun PreparedStatement.setUUID(parameterIndex: Int, uuid: UUID?) {
    if (uuid == null) {
        this.setNull(parameterIndex, Types.BLOB)
        return
    }
    this.setBytes(parameterIndex, toBytes(uuid))
}

fun ResultSet.getUuidFromString(columnName: String): UUID? {
    val value = this.getString(columnName) ?: return null
    return UuidUtils.fromString(value)
}

private fun toBytes(value: UUID): ByteArray {
    val buffer = ByteBuffer.allocate(UUID_BINARY_SIZE)
    buffer.putLong(value.mostSignificantBits)
    buffer.putLong(value.leastSignificantBits)
    return buffer.array()
}
private const val UUID_BINARY_SIZE = 16
