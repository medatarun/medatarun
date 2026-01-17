package io.medatarun.model.domain

import java.net.URI

sealed interface ModelOrigin {
    /**
     * Model definition had been imported and is linked to another source (JsonSchema, TableSchema, DataPackage Schema, JDBC database, etc.)
     *
     * An external source like this makes this entity externally managed (the origin is source of truth)
     */
    data class Uri(val uri: URI) : ModelOrigin

    /**
     * Model is manually created
     */
    object Manual : ModelOrigin
}
