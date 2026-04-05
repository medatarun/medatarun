package io.medatarun.auth.internal.actors

import io.medatarun.auth.domain.*
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.domain.role.Role
import io.medatarun.auth.domain.role.RoleId
import io.medatarun.auth.domain.role.RoleKey
import io.medatarun.auth.domain.role.RoleRef
import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.auth.ports.exposed.AuthJwtExternalPrincipal
import io.medatarun.auth.ports.needs.ActorStorage
import io.medatarun.auth.ports.needs.AuthClock
import io.medatarun.auth.ports.needs.PermissionsRegistry
import org.slf4j.LoggerFactory
import java.time.Instant

class ActorServiceImpl(
    private val actorStorage: ActorStorage,
    private val clock: AuthClock,
    private val appRoles: PermissionsRegistry
) : ActorService {

    override fun syncFromJwtExternalPrincipal(principal: AuthJwtExternalPrincipal): Actor {
        val existing = actorStorage.findActorByIssuerAndSubjectOptional(principal.issuer, principal.subject)

        val actor = if (existing == null) {
            val created = create(
                issuer = principal.issuer,
                subject = principal.subject,
                fullname = actorDisplayName(principal),
                email = principal.email,
                roles = emptyList(),
                disabled = null
            )
            logger.info("Registered actor {} from issuer {}", created.id, created.issuer)
            created
        } else {
            updateActorProfile(existing, principal)
        }

        return actor
    }

    fun updateActorProfile(
        existing: Actor,
        principal: AuthJwtExternalPrincipal
    ): Actor {
        actorStorage.actorUpdateProfile(
            id = existing.id,
            fullname = actorDisplayName(principal),
            email = principal.email,
            lastSeenAt = clock.now()
        )
        return actorStorage.findActorById(existing.id)
    }

    override fun updateFullname(actorId: ActorId, fullname: String) {
        val actor = actorStorage.findActorById(actorId)

        actorStorage.actorUpdateProfile(actor.id, fullname, email = actor.email, lastSeenAt = actor.lastSeenAt)
    }

    override fun create(
        issuer: String,
        subject: String,
        fullname: String,
        email: String?,
        roles: List<ActorPermission>,
        disabled: Instant?
    ): Actor {
        ensurePermissionsExist(roles)
        val id = ActorId.generate()
        actorStorage.actorCreate(
            id = id,
            issuer = issuer,
            subject = subject,
            fullname = fullname,
            email = email,
            roles = roles,
            disabled = disabled,
            createdAt = clock.now(),
            lastSeenAt = clock.now()
        )
        return actorStorage.findActorByIdOptional(id) ?: throw ActorCreateFailedWithNotFoundException()
    }


    override fun listActors(): List<Actor> {
        return actorStorage.findActorList()
    }

    override fun findByIssuerAndSubjectOptional(issuer: String, subject: String): Actor? {
        return actorStorage.findActorByIssuerAndSubjectOptional(issuer, subject)
    }

    override fun findById(actorId: ActorId): Actor {
        return actorStorage.findActorById(actorId)
    }

    override fun findByIdOptional(actorId: ActorId): Actor? {
        return actorStorage.findActorByIdOptional(actorId)
    }

    override fun setRoles(actorId: ActorId, roles: List<ActorPermission>) {
        ensurePermissionsExist(roles)
        val existing = actorStorage.findActorById(actorId)
        actorStorage.deprecated__updateRoles(existing.id, roles)
    }

    override fun listRoles(): List<Role> {
        return actorStorage.findRoleList()
    }

    override fun findRoleByRef(roleRef: RoleRef): Role {
        return actorStorage.findRoleByRef(roleRef)
    }

    override fun listRolePermissions(roleRef: RoleRef): List<ActorPermission> {
        val role = actorStorage.findRoleByRef(roleRef)
        return actorStorage.findRolePermissionList(role.id)
    }

    override fun createRole(
        key: RoleKey,
        name: String,
        description: String?
    ): RoleId {
        if (actorStorage.findRoleByKeyOptional(key) != null) {
            throw RoleAlreadyExistsException(key.value)
        }
        val now = clock.now()
        val roleId = RoleId.generate()
        actorStorage.roleCreate(roleId, key, name, description, now, now)
        return roleId
    }

    override fun updateRoleName(roleRef: RoleRef, name: String) {
        val role = actorStorage.findRoleByRef(roleRef)
        actorStorage.roleUpdateName(role.id, name, clock.now())
    }

    override fun updateRoleKey(roleRef: RoleRef, key: RoleKey) {
        val role = actorStorage.findRoleByRef(roleRef)
        val existingRole = actorStorage.findRoleByKeyOptional(key)
        if (existingRole != null && existingRole.id != role.id) {
            throw RoleAlreadyExistsException(key.value)
        }
        actorStorage.roleUpdateKey(role.id, key, clock.now())
    }

    override fun updateRoleDescription(roleRef: RoleRef, description: String?) {
        val role = actorStorage.findRoleByRef(roleRef)
        actorStorage.roleUpdateDescription(role.id, description, clock.now())
    }

    override fun addRolePermission(roleRef: RoleRef, permission: ActorPermission) {
        val role = actorStorage.findRoleByRef(roleRef)
        ensurePermissionExists(permission)
        if (actorStorage.roleHasPermission(role.id, permission)) {
            throw RolePermissionAlreadyExistsException(role.id.asString(), permission.key)
        }
        actorStorage.roleAddPermission(role.id, permission)
    }

    override fun deleteRolePermission(roleRef: RoleRef, permission: ActorPermission) {
        val role = actorStorage.findRoleByRef(roleRef)
        if (!actorStorage.roleHasPermission(role.id, permission)) {
            throw RolePermissionNotFoundException(role.id.asString(), permission.key)
        }
        actorStorage.roleDeletePermission(role.id, permission)
    }

    override fun actorAddRole(actorId: ActorId, roleRef: RoleRef) {
        val role = actorStorage.findRoleByRef(roleRef)
        val actor = actorStorage.findActorById(actorId)
        val existingRoles = actorStorage.findActorRoleIdList(actorId)
        val alreadyHasRole = existingRoles.contains(role.id)
        if (alreadyHasRole) throw ActorAddRoleAlreadyExistException(actorId, role.id)
        actorStorage.actorAddRole(actor.id, role.id)
    }

    override fun actorDeleteRole(actorId: ActorId, roleRef: RoleRef) {
        val role = actorStorage.findRoleByRef(roleRef)
        val actor = actorStorage.findActorById(actorId)
        val existingRoles = actorStorage.findActorRoleIdList(actorId)
        val alreadyHasRole = existingRoles.contains(role.id)
        if (!alreadyHasRole) throw ActorDeleteRoleNotFoundException(actorId, role.id)
        actorStorage.actorDeleteRole(actor.id, role.id)
    }

    override fun deleteRole(roleRef: RoleRef) {
        val role = actorStorage.findRoleByRef(roleRef)
        actorStorage.roleDelete(role.id)
    }

    override fun actorDisable(actorId: ActorId, at: Instant?) {
        val existing = actorStorage.findActorById(actorId)
        if (at == null) actorStorage.actorEnable(existing.id)
        else actorStorage.actorDisable(existing.id, at)
    }


    private fun ensurePermissionsExist(permissions: List<ActorPermission>): List<ActorPermission> {
        permissions.forEach {
            if (!appRoles.isKnownPermission(it.key)) throw AuthUnknownPermissionException(it.key)
        }
        return permissions
    }

    private fun ensurePermissionExists(p: ActorPermission): ActorPermission {
        if (!appRoles.isKnownPermission(p)) throw AuthUnknownPermissionException(p.key)
        return p
    }

    private fun actorDisplayName(principal: AuthJwtExternalPrincipal): String {
        val name = principal.name
        if (!name.isNullOrBlank()) {
            return name
        }

        val fullname = principal.fullname
        if (!fullname.isNullOrBlank()) {
            return fullname
        }

        val preferredUsername = principal.preferredUsername
        if (!preferredUsername.isNullOrBlank()) {
            return preferredUsername
        }

        val email = principal.email
        if (!email.isNullOrBlank()) {
            return email
        }
        return principal.subject
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ActorServiceImpl::class.java)
    }
}
