package io.medatarun.auth.infra.db

import io.medatarun.auth.internal.oidc.AuthClient
import io.medatarun.auth.internal.oidc.AuthClientStorage
import io.medatarun.auth.internal.oidc.OidcClientOrigin
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.exposed.jsonb
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.net.URI
import java.time.Instant

class AuthClientStorageDb(
    private val dbConnectionFactory: DbConnectionFactory
): AuthClientStorage {
    private val json = Json { encodeDefaults = true }

    override fun canRegister(): Boolean = true

    override fun register(client: AuthClient) {
        dbConnectionFactory.withExposed {
            AuthClientTable.deleteWhere { AuthClientTable.clientIdColumn eq client.clientId }
            AuthClientTable.insert { row ->
                row[clientIdColumn] = client.clientId
                row[originColumn] = client.origin.name
                row[originalRegistrationPayloadColumn] = encodePayload(client.originalRegistrationPayload)
                row[redirectUrisJsonColumn] = encodeStringList(client.redirectUris.map { it.toString() })
                row[grantTypesJsonColumn] = encodeStringList(client.grantTypes)
                row[responseTypesJsonColumn] = encodeStringList(client.responseTypes)
                row[tokenEndpointAuthMethodColumn] = client.tokenEndpointAuthMethod
                row[clientNameColumn] = client.clientName
                row[clientUriColumn] = client.clientUri?.toString()
                row[logoUriColumn] = client.logoUri?.toString()
                row[contactsJsonColumn] = encodeStringList(client.contacts)
                row[softwareIdColumn] = client.softwareId
                row[softwareVersionColumn] = client.softwareVersion
                row[tosUriColumn] = client.tosURI?.toString()
                row[policyUriColumn] = client.policyURI?.toString()
                row[createdAtColumn] = client.createdAt
                row[lastUsedAtColumn] = client.lastUsedAt
            }
        }
    }

    override fun findById(clientId: String): AuthClient? {
        return dbConnectionFactory.withExposed {
            AuthClientTable.selectAll()
                .where { AuthClientTable.clientIdColumn eq clientId }
                .singleOrNull()
                ?.let { readAuthClient(it) }
        }
    }

    override fun exists(clientId: String): Boolean {
        return dbConnectionFactory.withExposed {
            AuthClientTable.selectAll()
                .where { AuthClientTable.clientIdColumn eq clientId }
                .singleOrNull() != null
        }
    }

    private fun readAuthClient(row: ResultRow): AuthClient {
        return AuthClient(
            clientId = row[AuthClientTable.clientIdColumn],
            origin = OidcClientOrigin.valueOf(row[AuthClientTable.originColumn]),
            originalRegistrationPayload = decodePayload(row[AuthClientTable.originalRegistrationPayloadColumn]),
            createdAt = row[AuthClientTable.createdAtColumn],
            lastUsedAt = row[AuthClientTable.lastUsedAtColumn],
            redirectUris = decodeStringList(row[AuthClientTable.redirectUrisJsonColumn]).map { URI(it) },
            grantTypes = decodeStringList(row[AuthClientTable.grantTypesJsonColumn]),
            responseTypes = decodeStringList(row[AuthClientTable.responseTypesJsonColumn]),
            tokenEndpointAuthMethod = row[AuthClientTable.tokenEndpointAuthMethodColumn],
            clientName = row[AuthClientTable.clientNameColumn],
            clientUri = row[AuthClientTable.clientUriColumn]?.let { URI(it) },
            logoUri = row[AuthClientTable.logoUriColumn]?.let { URI(it) },
            contacts = decodeStringList(row[AuthClientTable.contactsJsonColumn]),
            softwareId = row[AuthClientTable.softwareIdColumn],
            softwareVersion = row[AuthClientTable.softwareVersionColumn],
            tosURI = row[AuthClientTable.tosUriColumn]?.let { URI(it) },
            policyURI = row[AuthClientTable.policyUriColumn]?.let { URI(it) }
        )
    }

    private fun encodeStringList(values: List<String>): String {
        return json.encodeToString(listStringSerializer, values)
    }

    private fun decodeStringList(value: String): List<String> {
        return json.decodeFromString(listStringSerializer, value)
    }

    private fun encodePayload(value: JsonObject?): String? {
        return value?.let { json.encodeToString(JsonObject.serializer(), it) }
    }

    private fun decodePayload(value: String?): JsonObject? {
        return value?.let { json.decodeFromString(JsonObject.serializer(), it) }
    }

    override fun purgeInactiveDynamicClients(expiresBefore: Instant) {
        dbConnectionFactory.withExposed {
            AuthClientTable.deleteWhere {
                (AuthClientTable.originColumn eq OidcClientOrigin.DCRP.name) and
                    (AuthClientTable.lastUsedAtColumn less expiresBefore)
            }
        }
    }

    companion object {
        private object AuthClientTable : Table("auth_client") {
            val clientIdColumn = text("client_id")
            val originColumn = text("origin")
            val originalRegistrationPayloadColumn = jsonb("original_registration_payload").nullable()
            val redirectUrisJsonColumn = jsonb("redirect_uris_json")
            val grantTypesJsonColumn = jsonb("grant_types_json")
            val responseTypesJsonColumn = jsonb("response_types_json")
            val tokenEndpointAuthMethodColumn = text("token_endpoint_auth_method")
            val clientNameColumn = text("client_name").nullable()
            val clientUriColumn = text("client_uri").nullable()
            val logoUriColumn = text("logo_uri").nullable()
            val contactsJsonColumn = jsonb("contacts_json")
            val softwareIdColumn = text("software_id").nullable()
            val softwareVersionColumn = text("software_version").nullable()
            val tosUriColumn = text("tos_uri").nullable()
            val policyUriColumn = text("policy_uri").nullable()
            val createdAtColumn = timestamp("created_at")
            val lastUsedAtColumn = timestamp("last_used_at")
        }

        private val listStringSerializer = ListSerializer(String.serializer())
    }
}
