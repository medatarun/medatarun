package io.medatarun.ext.modeljson.internal.v3

import kotlinx.serialization.Serializable

@Serializable
internal class BusinessKeyJsonV3(
    /**
     * On imports, the key can be null, on exports always present
     */
    val id: String? = null,
    /**
     * Every business key must have a key
     */
    val key: String,
    /**
     * Entity id must be specified
     */
    val entityId: String,
    /**
     * Participants is an ordered list of attribute ids and should contain one or more elements
     */
    val participants: List<String>,
    /**
     * Optional name of the business key
     */
    val name: String? = null,
    /**
     * Optional description of the business key
     */
    val description: String? = null

) {
}