package io.medatarun.auth.infra.db

import io.medatarun.auth.adapters.ActorInMemory
import io.medatarun.auth.domain.ActorPermission
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.domain.role.Role
import io.medatarun.auth.domain.role.RoleId
import io.medatarun.auth.domain.role.RoleInMemory
import io.medatarun.auth.domain.role.RoleKey
import io.medatarun.auth.infra.db.tables.ActorRoleTable
import io.medatarun.auth.infra.db.tables.ActorTable
import io.medatarun.auth.infra.db.tables.RolePermissionTable
import io.medatarun.auth.infra.db.tables.RoleTable
import io.medatarun.auth.ports.needs.ActorStorage
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.kernel.Service
import io.medatarun.security.AppPermissionKey
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant

class ActorStorageSQLite(private val dbConnectionFactory: DbConnectionFactory) : ActorStorage, Service {

    override fun actorCreate(
        id: ActorId,
        issuer: String,
        subject: String,
        fullname: String,
        email: String?,
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
                row[this.disabledDate] = disabled
                row[this.createdAt] = createdAt
                row[this.lastSeenAt] = lastSeenAt
            }
        }
    }

    override fun actorUpdateProfile(
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

    override fun roleCreate(
        id: RoleId,
        key: RoleKey,
        name: String,
        description: String?,
        autoAssign: Boolean,
        createdAt: Instant,
        lastUpdatedAt: Instant
    ) {
        dbConnectionFactory.withExposed {
            RoleTable.insert { row ->
                row[this.id] = id
                row[this.key] = key
                row[this.name] = name
                row[this.description] = description
                row[this.autoAssign] = autoAssign
                row[this.createdAt] = createdAt
                row[this.lastUpdatedAt] = lastUpdatedAt
            }
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

    override fun findRoleList(): List<Role> {
        return dbConnectionFactory.withExposed {
            RoleTable.selectAll()
                .orderBy(RoleTable.createdAt to SortOrder.DESC)
                .map { readRole(it) }
        }
    }

    override fun findRoleAutoAssign(): Role? {
        return dbConnectionFactory.withExposed {
            RoleTable.selectAll()
                .where { RoleTable.autoAssign eq true }
                .singleOrNull()
                ?.let { readRole(it) }
        }
    }

    override fun findRolePermissionList(roleId: RoleId): List<ActorPermission> {
        return dbConnectionFactory.withExposed {
            RolePermissionTable.selectAll()
                .where { RolePermissionTable.authRoleId eq roleId }
                .map { it[RolePermissionTable.permission] }
                .sortedBy { it.key }
        }
    }

    override fun roleUpdateName(roleId: RoleId, name: String, lastUpdatedAt: Instant) {
        dbConnectionFactory.withExposed {
            RoleTable.update(where = { RoleTable.id eq roleId }) { row ->
                row[this.name] = name
                row[this.lastUpdatedAt] = lastUpdatedAt
            }
        }
    }

    override fun roleUpdateKey(roleId: RoleId, key: RoleKey, lastUpdatedAt: Instant) {
        dbConnectionFactory.withExposed {
            RoleTable.update(where = { RoleTable.id eq roleId }) { row ->
                row[this.key] = key
                row[this.lastUpdatedAt] = lastUpdatedAt
            }
        }
    }

    override fun roleUpdateDescription(roleId: RoleId, description: String?, lastUpdatedAt: Instant) {
        dbConnectionFactory.withExposed {
            RoleTable.update(where = { RoleTable.id eq roleId }) { row ->
                row[this.description] = description
                row[this.lastUpdatedAt] = lastUpdatedAt
            }
        }
    }

    override fun roleUpdateAutoAssign(roleId: RoleId, autoAssign: Boolean, lastUpdatedAt: Instant) {
        dbConnectionFactory.withExposed {
            RoleTable.update(where = { RoleTable.id eq roleId }) { row ->
                row[this.autoAssign] = autoAssign
                row[this.lastUpdatedAt] = lastUpdatedAt
            }
        }
    }

    override fun roleManagedReplace(key: RoleKey, name: String, description: String?, createdAt: Instant, lastUpdatedAt: Instant) {
        dbConnectionFactory.withExposed {
            RoleTable.update(where = { RoleTable.key eq key }) { row ->
                row[this.name] = name
                row[this.description] = description
                row[this.createdAt] = createdAt
                row[this.lastUpdatedAt] = lastUpdatedAt
            }
        }
    }

    override fun roleHasPermission(roleId: RoleId, permission: ActorPermission): Boolean {
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

    override fun roleAddPermission(roleId: RoleId, permission: ActorPermission) {
        dbConnectionFactory.withExposed {
            RolePermissionTable.insert { row ->
                row[this.authRoleId] = roleId
                row[this.permission] = permission
            }
        }
    }

    override fun roleDeletePermission(roleId: RoleId, permission: ActorPermission) {
        dbConnectionFactory.withExposed {
            RolePermissionTable.deleteWhere {
                (RolePermissionTable.authRoleId eq roleId) and
                        (RolePermissionTable.permission eq permission)
            }
        }
    }

    override fun roleDelete(roleId: RoleId) {
        dbConnectionFactory.withExposed {
            RolePermissionTable.deleteWhere { RolePermissionTable.authRoleId eq roleId }
            ActorRoleTable.deleteWhere { ActorRoleTable.roleId eq roleId }
            RoleTable.deleteWhere { RoleTable.id eq roleId }
        }
    }

    override fun actorDisable(id: ActorId, at: Instant) {
        dbConnectionFactory.withExposed {
            ActorTable.update(where = { ActorTable.id eq id }) { row ->
                row[this.disabledDate] = at
            }
        }
    }

    override fun actorEnable(id: ActorId) {
        dbConnectionFactory.withExposed {
            ActorTable.update(where = { ActorTable.id eq id }) { row ->
                row[this.disabledDate] = null
            }
        }
    }

    override fun findActorByIssuerAndSubjectOptional(issuer: String, subject: String): Actor? {
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

    override fun findActorByIdOptional(id: ActorId): Actor? {
        return dbConnectionFactory.withExposed {
            ActorTable.selectAll()
                .where { ActorTable.id eq id }
                .singleOrNull()
                ?.let { readActor(it) }
        }
    }

    override fun findActorList(): List<Actor> {
        return dbConnectionFactory.withExposed {
            ActorTable.selectAll()
                .orderBy(ActorTable.createdAt to SortOrder.DESC)
                .map { readActor(it) }
        }
    }

    override fun findActorRoleIdList(actorId: ActorId): Set<RoleId> {
        return dbConnectionFactory.withExposed {
            ActorRoleTable.selectAll()
                .where { ActorRoleTable.actorId eq actorId }
                .map { it[ActorRoleTable.roleId] }
                .distinct()
                .toSet()
        }
    }

    override fun findActorPermissionSet(id: ActorId): Set<ActorPermission> {
        return dbConnectionFactory.withExposed {
            ActorRoleTable
                .innerJoin(RoleTable)
                .innerJoin(RolePermissionTable)
                .selectAll()
                .where { ActorRoleTable.actorId eq id }
                .orderBy(RolePermissionTable.permission to SortOrder.ASC)
                .map { it[RolePermissionTable.permission] }
                .distinct()
                .toSet()
        }
    }

    override fun actorAddRole(actorId: ActorId, roleId: RoleId) {
        dbConnectionFactory.withExposed {
            ActorRoleTable.insert { row ->
                row[this.actorId] = actorId
                row[this.roleId] = roleId
            }
        }
    }

    override fun actorDeleteRole(actorId: ActorId, roleId: RoleId) {
        dbConnectionFactory.withExposed {
            ActorRoleTable.deleteWhere {
                (ActorRoleTable.actorId eq actorId) and
                        (ActorRoleTable.roleId eq roleId)
            }
        }
    }

    private fun readActor(row: ResultRow): Actor {
        return ActorInMemory(
            id = row[ActorTable.id],
            issuer = row[ActorTable.issuer],
            subject = row[ActorTable.subject],
            fullname = row[ActorTable.fullName],
            email = row[ActorTable.email],
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
            autoAssign = row[RoleTable.autoAssign],
            createdAt = row[RoleTable.createdAt],
            lastUpdatedAt = row[RoleTable.lastUpdatedAt]
        )
    }

    fun renamePermissions(oldToNewPermissions: Map<String, AppPermissionKey>) {
        if (oldToNewPermissions.isEmpty()) {
            return
        }
        dbConnectionFactory.withExposed {
            oldToNewPermissions.forEach { (oldPermission, newPermission) ->
                RolePermissionTable.update(
                    where = { RolePermissionTable.permission eq ActorPermission(oldPermission) }
                ) { updateRow ->
                    updateRow[permission] = ActorPermission(newPermission.value)
                }
            }
        }
    }
}
