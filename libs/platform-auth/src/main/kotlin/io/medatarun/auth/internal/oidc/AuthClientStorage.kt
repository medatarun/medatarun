package io.medatarun.auth.internal.oidc

import java.time.Instant

/**
 * Storage for OIDC Clients
 */
interface AuthClientStorage {
    /**
     * Find client by id
     */
    fun findById(clientId: String): AuthClient?
    /**
     * Tells if client exists
     */
    fun exists(clientId: String): Boolean

    /**
     * Tells if this storage is readonly or accepts registrations
     */
    fun canRegister(): Boolean

    /**
     * Registers a new client
     */
    fun register(client: AuthClient)

    /**
     * Purges inactive DCRP clients based on their last usage time.
     */
    fun purgeInactiveDynamicClients(expiresBefore: Instant)
}
