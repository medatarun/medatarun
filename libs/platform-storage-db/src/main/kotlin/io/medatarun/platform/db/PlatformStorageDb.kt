package io.medatarun.platform.db

interface PlatformStorageDb {
    fun connectionFactory(): DbConnectionFactory
}