package io.medatarun.auth.infra

import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.ports.needs.ActorStorage
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language
import java.time.Instant
import java.util.*

class ActorStorageSQLite(private val dbConnectionFactory: DbConnectionFactory) : ActorStorage {

    private val json = Json { encodeDefaults = true }

    init {
        dbConnectionFactory.getConnection().use { connection ->
            SCHEMA.split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { stmt ->
                    connection.createStatement().execute(stmt)
                }
        }
    }

    override fun insert(
        id: ActorId,
        issuer: String,
        subject: String,
        fullname: String,
        email: String?,
        roles: List<ActorRole>,
        createdAt: Instant,
        lastSeenAt: Instant
    ) {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                """
                INSERT INTO actors(
                    id,
                    issuer,
                    subject,
                    full_name,
                    email,
                    roles_json,
                    disabled_date,
                    created_at,
                    last_seen_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """
            ).use { ps ->
                ps.setString(1, toSql(id))
                ps.setString(2, issuer)
                ps.setString(3, subject)
                ps.setString(4, fullname)
                ps.setString(5, email)
                ps.setString(6, encodeRoles(roles))
                ps.setString(7, null)
                ps.setString(8, createdAt.toString())
                ps.setString(9, lastSeenAt.toString())
                ps.executeUpdate()
            }
        }
    }

    override fun updateProfile(
        id: ActorId,
        fullname: String,
        email: String?,
        lastSeenAt: Instant
    ) {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                """
                UPDATE actors
                SET full_name = ?, email = ?, last_seen_at = ?
                WHERE id = ?
                """
            ).use { ps ->
                ps.setString(1, fullname)
                ps.setString(2, email)
                ps.setString(3, lastSeenAt.toString())
                ps.setString(4, toSql(id))
                ps.executeUpdate()
            }
        }
    }

    override fun updateRoles(id: ActorId, roles: List<ActorRole>) {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "UPDATE actors SET roles_json = ? WHERE id = ?"
            ).use { ps ->
                ps.setString(1, encodeRoles(roles))
                ps.setString(2, toSql(id))
                ps.executeUpdate()
            }
        }
    }

    override fun disable(id: ActorId, at: Instant) {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "UPDATE actors SET disabled_date = ? WHERE id = ?"
            ).use { ps ->
                ps.setString(1, at.toString())
                ps.setString(2, toSql(id))
                ps.executeUpdate()
            }
        }
    }

    override fun enable(id: ActorId,) {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "UPDATE actors SET disabled_date = NULL WHERE id = ?"
            ).use { ps ->
                ps.setString(1, toSql(id))
                ps.executeUpdate()
            }
        }
    }

    override fun findByIssuerAndSubjectOptional(issuer: String, subject: String): Actor? {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "SELECT * FROM actors WHERE issuer = ? AND subject = ?"
            ).use { ps ->
                ps.setString(1, issuer)
                ps.setString(2, subject)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return null
                    return readActor(rs)
                }
            }
        }
    }

    override fun findByIdOptional(id: ActorId): Actor? {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement(
                "SELECT * FROM actors WHERE id = ?"
            ).use { ps ->
                ps.setString(1, toSql(id))
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return null
                    return readActor(rs)
                }
            }
        }
    }

    override fun listAll(): List<Actor> {
        dbConnectionFactory.getConnection().use { c ->
            c.prepareStatement("SELECT * FROM actors ORDER BY created_at DESC").use { ps ->
                ps.executeQuery().use { rs ->
                    val actors = ArrayList<Actor>()
                    while (rs.next()) {
                        actors.add(readActor(rs))
                    }
                    return actors
                }
            }
        }
    }

    private fun toSql(actorId: ActorId): String { return actorId.value.toString() }

    private fun readActor(rs: java.sql.ResultSet): Actor {
        val rolesJson = rs.getString("roles_json")
        return Actor(
            id = ActorId(UUID.fromString(rs.getString("id"))),
            issuer = rs.getString("issuer"),
            subject = rs.getString("subject"),
            fullname = rs.getString("full_name"),
            email = rs.getString("email"),
            roles = decodeRoles(rolesJson),
            disabledDate = rs.getString("disabled_date")?.let { Instant.parse(it) },
            createdAt = Instant.parse(rs.getString("created_at")),
            lastSeenAt = Instant.parse(rs.getString("last_seen_at"))
        )
    }

    private fun encodeRoles(roles: List<ActorRole>): String {
        return json.encodeToString(listStringSerializer, roles.map { it.key })
    }

    private fun decodeRoles(rolesJson: String): List<ActorRole> {
        return json.decodeFromString(listStringSerializer, rolesJson)
            .map { ActorRole(it) }
    }

    companion object {
        @Language("SQLite")
        private const val SCHEMA = """
CREATE TABLE IF NOT EXISTS actors (
  id TEXT PRIMARY KEY UNIQUE,
  issuer TEXT NOT NULL,
  subject TEXT NOT NULL,
  full_name TEXT NOT NULL,
  email TEXT,
  roles_json TEXT NOT NULL,
  disabled_date TEXT,
  created_at TEXT NOT NULL,
  last_seen_at TEXT NOT NULL,
  UNIQUE(issuer, subject)
);
CREATE INDEX IF NOT EXISTS idx_actors_issuer_subject ON actors(issuer, subject);
CREATE INDEX IF NOT EXISTS idx_actors_created_at ON actors(created_at);
"""

        private val listStringSerializer = ListSerializer(String.serializer())
    }
}
