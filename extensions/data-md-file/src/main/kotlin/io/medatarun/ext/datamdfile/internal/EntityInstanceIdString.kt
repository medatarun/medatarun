package io.medatarun.ext.datamdfile.internal

import io.medatarun.data.EntityInstanceId

data class EntityInstanceIdString(
    private val value: String
) : EntityInstanceId {
    override fun asString(): String = value
}