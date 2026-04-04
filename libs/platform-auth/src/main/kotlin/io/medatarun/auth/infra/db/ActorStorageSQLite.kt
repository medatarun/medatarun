package io.medatarun.auth.infra.db

import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.domain.role.Role
import io.medatarun.auth.domain.role.RoleId
import io.medatarun.auth.domain.role.RoleKey
import io.medatarun.auth.domain.role.RoleRef
import io.medatarun.auth.domain.role.RoleInMemory
import io.medatarun.auth.infra.db.tables.ActorTable
import io.medatarun.auth.infra.db.tables.ActorRoleTable
import io.medatarun.auth.infra.db.tables.RoleTable
import io.medatarun.auth.infra.db.tables.RolePermissionTable
import io.medatarun.auth.ports.needs.ActorStorage
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionStringBased
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.deleteWhere
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
                row[this.fullName] = fullname
                row[this.email] = email
                row[this.rolesJson] = encodeRoles(roles)
                row[this.disabledDate] = disabled
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
                row[this.fullName] = fullname
                row[this.email] = email
                row[this.lastSeenAt] = lastSeenAt
            }
        }
    }

    override fun updateRoles(id: ActorId, roles: List<ActorRole>) {
        dbConnectionFactory.withExposed {
            ActorTable.update(where = { ActorTable.id eq id }) { row ->
                row[this.rolesJson] = encodeRoles(roles)
            }
        }
    }

    override fun createRole(
        id: RoleId,
        key: RoleKey,
        name: String,
        description: String?,
        createdAt: Instant,
        lastUpdatedAt: Instant
    ) {
        dbConnectionFactory.withExposed {
            RoleTable.insert { row ->
                row[this.id] = id
                row[this.key] = key
                row[this.name] = name
                row[this.description] = description
                row[this.createdAt] = createdAt
                row[this.lastUpdatedAt] = lastUpdatedAt
            }
        }
    }

    override fun findRoleByRefOptional(roleRef: RoleRef): Role? {
        return when (roleRef) {
            is RoleRef.ById -> findRoleByIdOptional(roleRef.id)
            is RoleRef.ByKey -> findRoleByKeyOptional(roleRef.key.validated())
        }
    }

    override fun findRoleByIdOptional(roleId: RoleId): Role? {
        return dbConnectionFactory.withExposed {
            RoleTable.selectAll()
                .where { RoleTable.id eq roleId }
                .singleOrNull()
                ?.let { readRole(it) }
        }
    }

    override fun findRoleByKeyOptional(key: RoleKey): Role? {
        return dbConnectionFactory.withExposed {
            RoleTable.selectAll()
                .where { RoleTable.key eq key }
                .singleOrNull()
                ?.let { readRole(it) }
        }
    }

    override fun listRoles(): List<Role> {
        return dbConnectionFactory.withExposed {
            RoleTable.selectAll()
                .orderBy(RoleTable.createdAt to SortOrder.DESC)
                .map { readRole(it) }
        }
    }

    override fun listRolePermissions(roleId: RoleId): List<AppPermission> {
        return dbConnectionFactory.withExposed {
            RolePermissionTable.selectAll()
                .where { RolePermissionTable.authRoleId eq roleId }
                .map { it[RolePermissionTable.permission] }
                .sortedBy { it.key }
        }
    }

    override fun updateRoleName(roleId: RoleId, name: String, lastUpdatedAt: Instant) {
        dbConnectionFactory.withExposed {
            RoleTable.update(where = { RoleTable.id eq roleId }) { row ->
                row[this.name] = name
                row[this.lastUpdatedAt] = lastUpdatedAt
            }
        }
    }

    override fun updateRoleKey(roleId: RoleId, key: RoleKey, lastUpdatedAt: Instant) {
        dbConnectionFactory.withExposed {
            RoleTable.update(where = { RoleTable.id eq roleId }) { row ->
                row[this.key] = key
                row[this.lastUpdatedAt] = lastUpdatedAt
            }
        }
    }

    override fun updateRoleDescription(roleId: RoleId, description: String?, lastUpdatedAt: Instant) {
        dbConnectionFactory.withExposed {
            RoleTable.update(where = { RoleTable.id eq roleId }) { row ->
                row[this.description] = description
                row[this.lastUpdatedAt] = lastUpdatedAt
            }
        }
    }

    override fun rolePermissionExists(roleId: RoleId, permission: AppPermission): Boolean {
        return dbConnectionFactory.withExposed {
            RolePermissionTable.selectAll()
                .where {
                    (RolePermissionTable.authRoleId eq roleId) and
                        (RolePermissionTable.permission eq permission)
                }
                .empty()
                .not()
        }
    }

    override fun addRolePermission(roleId: RoleId, permission: AppPermission) {
        dbConnectionFactory.withExposed {
            RolePermissionTable.insert { row ->
                row[this.authRoleId] = roleId
                row[this.permission] = permission
            }
        }
    }

    override fun deleteRolePermission(roleId: RoleId, permission: AppPermission) {
        dbConnectionFactory.withExposed {
            RolePermissionTable.deleteWhere {
                (RolePermissionTable.authRoleId eq roleId) and
                    (RolePermissionTable.permission eq permission)
            }
        }
    }

    override fun deleteRole(roleId: RoleId) {
        dbConnectionFactory.withExposed {
            RolePermissionTable.deleteWhere { RolePermissionTable.authRoleId eq roleId }
            ActorRoleTable.deleteWhere { ActorRoleTable.roleId eq roleId }
            RoleTable.deleteWhere { RoleTable.id eq roleId }
        }
    }

    override fun disable(id: ActorId, at: Instant) {
        dbConnectionFactory.withExposed {
            ActorTable.update(where = { ActorTable.id eq id }) { row ->
                row[this.disabledDate] = at
            }
        }
    }

    override fun enable(id: ActorId,) {
        dbConnectionFactory.withExposed {
            ActorTable.update(where = { ActorTable.id eq id }) { row ->
                row[this.disabledDate] = null
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

    private fun readRole(row: ResultRow): Role {
        return RoleInMemory(
            id = row[RoleTable.id],
            key = row[RoleTable.key],
            name = row[RoleTable.name],
            description = row[RoleTable.description],
            createdAt = row[RoleTable.createdAt],
            lastUpdatedAt = row[RoleTable.lastUpdatedAt]
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

            oldToNewPermissions.forEach { (oldPermission, newPermission) ->
                RolePermissionTable.update(
                    where = { RolePermissionTable.permission eq AppPermissionStringBased(oldPermission) }
                ) { updateRow ->
                    updateRow[permission] = AppPermissionStringBased(newPermission)
                }
            }
        }
    }

    companion object {


        private val listStringSerializer = ListSerializer(String.serializer())
    }
}
