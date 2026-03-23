package io.medatarun.auth.infra.db

import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.ports.needs.ActorStorage
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbSqlResources
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

    fun initSchema() {
        DbSqlResources.executeClasspathResource(dbConnectionFactory, AuthDbMigration.v001_actors)
    }

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
                row[idColumn] = toSql(id)
                row[issuerColumn] = issuer
                row[subjectColumn] = subject
                row[fullNameColumn] = fullname
                row[emailColumn] = email
                row[rolesJsonColumn] = encodeRoles(roles)
                row[disabledDateColumn] = disabled?.let { InstantSql.toSql(it) }
                row[createdAtColumn] = InstantSql.toSql(createdAt)
                row[lastSeenAtColumn] = InstantSql.toSql(lastSeenAt)
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
            ActorsTable.update(where = { ActorsTable.idColumn eq toSql(id) }) { row ->
                row[fullNameColumn] = fullname
                row[emailColumn] = email
                row[lastSeenAtColumn] = InstantSql.toSql(lastSeenAt)
            }
        }
    }

    override fun updateRoles(id: ActorId, roles: List<ActorRole>) {
        dbConnectionFactory.withExposed {
            ActorsTable.update(where = { ActorsTable.idColumn eq toSql(id) }) { row ->
                row[rolesJsonColumn] = encodeRoles(roles)
            }
        }
    }

    override fun disable(id: ActorId, at: Instant) {
        dbConnectionFactory.withExposed {
            ActorsTable.update(where = { ActorsTable.idColumn eq toSql(id) }) { row ->
                row[disabledDateColumn] = InstantSql.toSql(at)
            }
        }
    }

    override fun enable(id: ActorId,) {
        dbConnectionFactory.withExposed {
            ActorsTable.update(where = { ActorsTable.idColumn eq toSql(id) }) { row ->
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
                .where { ActorsTable.idColumn eq toSql(id) }
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

    private fun toSql(actorId: ActorId): String { return actorId.value.toString() }

    private fun readActor(row: ResultRow): Actor {
        val rolesJson = row[ActorsTable.rolesJsonColumn]
        return Actor(
            id = ActorId.fromString(row[ActorsTable.idColumn]),
            issuer = row[ActorsTable.issuerColumn],
            subject = row[ActorsTable.subjectColumn],
            fullname = row[ActorsTable.fullNameColumn],
            email = row[ActorsTable.emailColumn],
            roles = decodeRoles(rolesJson),
            disabledDate = row[ActorsTable.disabledDateColumn]?.let { Instant.parse(it) },
            createdAt = Instant.parse(row[ActorsTable.createdAtColumn]),
            lastSeenAt = Instant.parse(row[ActorsTable.lastSeenAtColumn])
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
            val idColumn = text("id")
            val issuerColumn = text("issuer")
            val subjectColumn = text("subject")
            val fullNameColumn = text("full_name")
            val emailColumn = text("email").nullable()
            val rolesJsonColumn = text("roles_json")
            val disabledDateColumn = text("disabled_date").nullable()
            val createdAtColumn = text("created_at")
            val lastSeenAtColumn = text("last_seen_at")
        }

        private val listStringSerializer = ListSerializer(String.serializer())
    }
}
