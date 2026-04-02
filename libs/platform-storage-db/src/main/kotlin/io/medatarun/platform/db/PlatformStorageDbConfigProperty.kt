package io.medatarun.platform.db

import io.medatarun.lang.config.ConfigPropertyDescription

enum class PlatformStorageDbConfigProperty(
    override val key: String,
    override val type: String,
    override val defaultValue: String,
    override val description: String
) : ConfigPropertyDescription {
    DbEngine(
        key = "medatarun.storage.datasource.jdbc.dbengine",
        type = "String",
        defaultValue = "sqlite",
        description = "Database engine used by the storage layer. Supported values are `sqlite` and `postgresql`."
    ),
    JdbcUrl(
        key = "medatarun.storage.datasource.jdbc.url",
        type = "String",
        defaultValue = "<generated for sqlite>",
        description = "JDBC URL used to connect to the storage database. If omitted with sqlite, Medatarun uses a database file under `MEDATARUN_HOME/data/database.db`."
    ),
    JdbcPropertiesEntry(
        key = "medatarun.storage.datasource.jdbc.properties.*",
        type = "String",
        defaultValue = "",
        description = "Additional JDBC property. Replace `*` with the JDBC property key to pass through to the JDBC driver (for example `user` or `password`)."
    );

    /**
     * Returns the key prefix used to scan all JDBC property entries.
     * For example: medatarun.storage.datasource.jdbc.properties.
     */
    fun prefixKey(): String {
        return key.removeSuffix("*")
    }
}
