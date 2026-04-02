package io.medatarun.auth.infra.db

import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.ports.needs.ActorStorage
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.exposed.jsonb
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.ColumnTransformer
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import java.util.UUID

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
            ActorsTable.insert { row ->
                row[idColumn] = id
                row[issuerColumn] = issuer
                row[subjectColumn] = subject
                row[fullNameColumn] = fullname
                row[emailColumn] = email
                row[rolesJsonColumn] = encodeRoles(roles)
                row[disabledDateColumn] = disabled
                row[createdAtColumn] = createdAt
                row[lastSeenAtColumn] = lastSeenAt
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
            ActorsTable.update(where = { ActorsTable.idColumn eq id }) { row ->
                row[fullNameColumn] = fullname
                row[emailColumn] = email
                row[lastSeenAtColumn] = lastSeenAt
            }
        }
    }

    override fun updateRoles(id: ActorId, roles: List<ActorRole>) {
        dbConnectionFactory.withExposed {
            ActorsTable.update(where = { ActorsTable.idColumn eq id }) { row ->
                row[rolesJsonColumn] = encodeRoles(roles)
            }
        }
    }

    override fun disable(id: ActorId, at: Instant) {
        dbConnectionFactory.withExposed {
            ActorsTable.update(where = { ActorsTable.idColumn eq id }) { row ->
                row[disabledDateColumn] = at
            }
        }
    }

    override fun enable(id: ActorId,) {
        dbConnectionFactory.withExposed {
            ActorsTable.update(where = { ActorsTable.idColumn eq id }) { row ->
                row[disabledDateColumn] = null
            }
        }
    }

    override fun findByIssuerAndSubjectOptional(issuer: String, subject: String): Actor? {
        return dbConnectionFactory.withExposed {
            ActorsTable.selectAll()
                .where {
                    (ActorsTable.issuerColumn eq issuer) and
                        (ActorsTable.subjectColumn eq subject)
                }
                .singleOrNull()
                ?.let { readActor(it) }
        }
    }

    override fun findByIdOptional(id: ActorId): Actor? {
        return dbConnectionFactory.withExposed {
            ActorsTable.selectAll()
                .where { ActorsTable.idColumn eq id }
                .singleOrNull()
                ?.let { readActor(it) }
        }
    }

    override fun listAll(): List<Actor> {
        return dbConnectionFactory.withExposed {
            ActorsTable.selectAll()
                .orderBy(ActorsTable.createdAtColumn to SortOrder.DESC)
                .map { readActor(it) }
        }
    }

    private fun readActor(row: ResultRow): Actor {
        val rolesJson = row[ActorsTable.rolesJsonColumn]
        return Actor(
            id = row[ActorsTable.idColumn],
            issuer = row[ActorsTable.issuerColumn],
            subject = row[ActorsTable.subjectColumn],
            fullname = row[ActorsTable.fullNameColumn],
            email = row[ActorsTable.emailColumn],
            roles = decodeRoles(rolesJson),
            disabledDate = row[ActorsTable.disabledDateColumn],
            createdAt = row[ActorsTable.createdAtColumn],
            lastSeenAt = row[ActorsTable.lastSeenAtColumn]
        )
    }

    private fun encodeRoles(roles: List<ActorRole>): String {
        return json.encodeToString(listStringSerializer, roles.map { it.key })
    }

    private fun decodeRoles(rolesJson: String): List<ActorRole> {
        return json.decodeFromString(listStringSerializer, rolesJson)
            .map { ActorRole(it) }
    }

    fun renameRoles(oldToNewRoles: Map<String, String>) {
        if (oldToNewRoles.isEmpty()) {
            return
        }
        dbConnectionFactory.withExposed {
            ActorsTable.selectAll().forEach { row ->
                val actorId = row[ActorsTable.idColumn]
                val currentRoles = decodeRoles(row[ActorsTable.rolesJsonColumn])
                var hasChanges = false
                val renamedRoles = mutableListOf<ActorRole>()
                currentRoles.forEach { role ->
                    val newRoleName = oldToNewRoles[role.key]
                    if (newRoleName != null) {
                        hasChanges = true
                        renamedRoles.add(ActorRole(newRoleName))
                    } else {
                        renamedRoles.add(role)
                    }
                }
                if (hasChanges) {
                    ActorsTable.update(where = { ActorsTable.idColumn eq actorId }) { updateRow ->
                        updateRow[rolesJsonColumn] = encodeRoles(renamedRoles)
                    }
                }
            }
        }
    }

    companion object {
        private object ActorsTable : Table("actors") {
            val idColumn = javaUUID("id").transform(ActorIdColumnTransformer())
            val issuerColumn = text("issuer")
            val subjectColumn = text("subject")
            val fullNameColumn = text("full_name")
            val emailColumn = text("email").nullable()
            val rolesJsonColumn = jsonb("roles_json")
            val disabledDateColumn = timestamp("disabled_date").nullable()
            val createdAtColumn = timestamp("created_at")
            val lastSeenAtColumn = timestamp("last_seen_at")
        }

        private class ActorIdColumnTransformer : ColumnTransformer<UUID, ActorId> {
            override fun unwrap(value: ActorId): UUID {
                return value.value
            }

            override fun wrap(value: UUID): ActorId {
                return ActorId(value)
            }
        }

        private val listStringSerializer = ListSerializer(String.serializer())
    }
}
