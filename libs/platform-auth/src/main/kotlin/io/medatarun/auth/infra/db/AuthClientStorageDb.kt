package io.medatarun.auth.infra.db

import io.medatarun.auth.internal.oidc.AuthClient
import io.medatarun.auth.internal.oidc.AuthClientStorage
import io.medatarun.platform.db.DbConnectionFactory

class AuthClientStorageDb(private val dbConnectionFactory: DbConnectionFactory): AuthClientStorage {
    private val clients = LinkedHashMap<String, AuthClient>()
    override fun canRegister(): Boolean = true
    override fun register(client: AuthClient) {
        clients[client.clientId] = client
    }

    override fun findById(clientId: String): AuthClient? {
        return clients[clientId]
    }

    override fun exists(clientId: String): Boolean {
        return clients.contains(clientId)
    }

}