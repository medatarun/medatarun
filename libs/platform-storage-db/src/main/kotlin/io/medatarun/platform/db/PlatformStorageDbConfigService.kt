package io.medatarun.platform.db

import java.util.Properties

interface PlatformStorageDbConfigService {
    /**
     * Returns the raw JDBC URL from configuration.
     */
    fun getJdbcUrl(): String?

    /**
     * Returns JDBC properties configured under the JDBC properties prefix.
     * Entries with a blank key suffix or blank value are discarded.
     */
    fun getJdbcProperties(): Properties

    /**
     * Returns the configured database engine identifier.
     */
    fun getDbEngine(): String?
}
