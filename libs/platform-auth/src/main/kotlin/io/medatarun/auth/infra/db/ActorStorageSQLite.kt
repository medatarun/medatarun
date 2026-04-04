package io.medatarun.auth.infra.db

import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.infra.db.tables.ActorTable
import io.medatarun.auth.ports.needs.ActorStorage
import io.medatarun.platform.db.DbConnectionFactory
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant

class ActorStorageSQLite(private val dbConnectionFactory: DbConnectionFactory) : ActorStorage {

    private val json = Json { encodeDefaults = true }

    override fun insert(
        id: ActorId,
        issuer: String,
        subject: String,
        fullname: String,
        email: String?,
        roles: List<ActorRole>,
        disabled: Instant?,
        createdAt: Instant,
        lastSeenAt: Instant
    ) {
        dbConnectionFactory.withExposed {
            ActorTable.insert { row ->
                row[this.id] = id
                row[this.issuer] = issuer
                row[this.subject] = subject
                row[fullName] = fullname
                row[this.email] = email
                row[rolesJson] = encodeRoles(roles)
                row[disabledDate] = disabled
                row[this.createdAt] = createdAt
                row[this.lastSeenAt] = lastSeenAt
            }
        }
    }

    override fun updateProfile(
        id: ActorId,
        fullname: String,
        email: String?,
        lastSeenAt: Instant
    ) {
        dbConnectionFactory.withExposed {
            ActorTable.update(where = { ActorTable.id eq id }) { row ->
                row[fullName] = fullname
                row[this.email] = email
                row[this.lastSeenAt] = lastSeenAt
            }
        }
    }

    override fun updateRoles(id: ActorId, roles: List<ActorRole>) {
        dbConnectionFactory.withExposed {
            ActorTable.update(where = { ActorTable.id eq id }) { row ->
                row[rolesJson] = encodeRoles(roles)
            }
        }
    }

    override fun disable(id: ActorId, at: Instant) {
        dbConnectionFactory.withExposed {
            ActorTable.update(where = { ActorTable.id eq id }) { row ->
                row[disabledDate] = at
            }
        }
    }

    override fun enable(id: ActorId,) {
        dbConnectionFactory.withExposed {
            ActorTable.update(where = { ActorTable.id eq id }) { row ->
                row[disabledDate] = null
            }
        }
    }

    override fun findByIssuerAndSubjectOptional(issuer: String, subject: String): Actor? {
        return dbConnectionFactory.withExposed {
            ActorTable.selectAll()
                .where {
                    (ActorTable.issuer eq issuer) and
                        (ActorTable.subject eq subject)
                }
                .singleOrNull()
                ?.let { readActor(it) }
        }
    }

    override fun findByIdOptional(id: ActorId): Actor? {
        return dbConnectionFactory.withExposed {
            ActorTable.selectAll()
                .where { ActorTable.id eq id }
                .singleOrNull()
                ?.let { readActor(it) }
        }
    }

    override fun listAll(): List<Actor> {
        return dbConnectionFactory.withExposed {
            ActorTable.selectAll()
                .orderBy(ActorTable.createdAt to SortOrder.DESC)
                .map { readActor(it) }
        }
    }

    private fun readActor(row: ResultRow): Actor {
        val rolesJson = row[ActorTable.rolesJson]
        return Actor(
            id = row[ActorTable.id],
            issuer = row[ActorTable.issuer],
            subject = row[ActorTable.subject],
            fullname = row[ActorTable.fullName],
            email = row[ActorTable.email],
            roles = decodeRoles(rolesJson),
            disabledDate = row[ActorTable.disabledDate],
            createdAt = row[ActorTable.createdAt],
            lastSeenAt = row[ActorTable.lastSeenAt]
        )
    }

    private fun encodeRoles(roles: List<ActorRole>): String {
        return json.encodeToString(listStringSerializer, roles.map { it.key })
    }

    private fun decodeRoles(rolesJson: String): List<ActorRole> {
        return json.decodeFromString(listStringSerializer, rolesJson)
            .map { ActorRole(it) }
    }

    fun renamePermissions(oldToNewPermissions: Map<String, String>) {
        if (oldToNewPermissions.isEmpty()) {
            return
        }
        dbConnectionFactory.withExposed {
            ActorTable.selectAll().forEach { row ->
                val actorId = row[ActorTable.id]
                val currentRoles = decodeRoles(row[ActorTable.rolesJson])
                var hasChanges = false
                val renamedRoles = mutableListOf<ActorRole>()
                currentRoles.forEach { role ->
                    val newRoleName = oldToNewPermissions[role.key]
                    if (newRoleName != null) {
                        hasChanges = true
                        renamedRoles.add(ActorRole(newRoleName))
                    } else {
                        renamedRoles.add(role)
                    }
                }
                if (hasChanges) {
                    ActorTable.update(where = { ActorTable.id eq actorId }) { updateRow ->
                        updateRow[rolesJson] = encodeRoles(renamedRoles)
                    }
                }
            }
        }
    }

    companion object {


        private val listStringSerializer = ListSerializer(String.serializer())
    }
}
