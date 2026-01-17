package io.medatarun.ext.db.model

/**
 * A connection to a database, identified by an abstract name amongst all available datasources
 */
data class DbDatasource(
    /**
     * is a logical name for your datasource. This is the name you will use to do imports and will be shared in your
     *   project. This way, all your team members can rely on the same names.
     */
    val name: String,
    /**
     * is the `id` of the driver
     */
    val driver: String,
    /**
     * is the JDBC URL used to connect to the database from your environment. For example, with PostgreSQL, a URL often
     * looks like: `jdbc:postgresql://localhost:5432/myschema`. Other databases use similar URLs, mostly differing by the
     * prefix. Check your database documentation for the exact format.
     */
    val url: String,
    /**
     * connexion username
     */
    val username: String,
    /**
     * defines how the password is stored
     */
    val secret: DbConnectionSecret,
    /**
     * is a set of values you can pass to the driver on each connection. You must refer to your database vendor
     *   documentation to know the list of possible values.
     */
    val properties: Map<String, String>
)