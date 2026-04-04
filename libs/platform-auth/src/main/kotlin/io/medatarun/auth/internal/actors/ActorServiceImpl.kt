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
        val existing = actorStorage.findByIssuerAndSubjectOptional(principal.issuer, principal.subject)

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
        actorStorage.updateProfile(
            id = existing.id,
            fullname = actorDisplayName(principal),
            email = principal.email,
            lastSeenAt = clock.now()
        )
        return actorStorage.findById(existing.id)
    }

    override fun updateFullname(actorId: ActorId, fullname: String) {
        val actor = actorStorage.findById(actorId)

        actorStorage.updateProfile(actor.id, fullname, email = actor.email, lastSeenAt = actor.lastSeenAt)
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
        actorStorage.insert(
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
        return actorStorage.findByIdOptional(id) ?: throw ActorCreateFailedWithNotFoundException()
    }


    override fun listActors(): List<Actor> {
        return actorStorage.listAll()
    }

    override fun findByIssuerAndSubjectOptional(issuer: String, subject: String): Actor? {
        return actorStorage.findByIssuerAndSubjectOptional(issuer, subject)
    }

    override fun findById(actorId: ActorId): Actor {
        return actorStorage.findById(actorId)
    }

    override fun findByIdOptional(actorId: ActorId): Actor? {
        return actorStorage.findByIdOptional(actorId)
    }

    override fun setRoles(actorId: ActorId, roles: List<ActorPermission>) {
        ensurePermissionsExist(roles)
        val existing = actorStorage.findById(actorId)
        actorStorage.updateRoles(existing.id, roles)
    }

    override fun listRoles(): List<Role> {
        return actorStorage.listRoles()
    }

    override fun findRoleByRef(roleRef: RoleRef): Role {
        return actorStorage.findRoleByRefOptional(roleRef) ?: throw RoleNotFoundException()
    }

    override fun listRolePermissions(roleRef: RoleRef): List<ActorPermission> {
        val role = findRoleByRef(roleRef)
        return actorStorage.listRolePermissions(role.id)
    }

    override fun createRole(
        key: RoleKey,
        name: String,
        description: String?
    ): RoleId {
        val validatedKey = key.validated()
        if (actorStorage.findRoleByKeyOptional(validatedKey) != null) {
            throw RoleAlreadyExistsException(validatedKey.value)
        }
        val now = clock.now()
        val roleId = RoleId.generate()
        actorStorage.createRole(roleId, validatedKey, name, description, now, now)
        return roleId
    }

    override fun updateRoleName(roleRef: RoleRef, name: String) {
        val role = findRoleByRef(roleRef)
        actorStorage.updateRoleName(role.id, name, clock.now())
    }

    override fun updateRoleKey(roleRef: RoleRef, key: RoleKey) {
        val role = findRoleByRef(roleRef)
        val validatedKey = key.validated()
        val existingRole = actorStorage.findRoleByKeyOptional(validatedKey)
        if (existingRole != null && existingRole.id != role.id) {
            throw RoleAlreadyExistsException(validatedKey.value)
        }
        actorStorage.updateRoleKey(role.id, validatedKey, clock.now())
    }

    override fun updateRoleDescription(roleRef: RoleRef, description: String?) {
        val role = findRoleByRef(roleRef)
        actorStorage.updateRoleDescription(role.id, description, clock.now())
    }

    override fun addRolePermission(roleRef: RoleRef, permission: ActorPermission) {
        val role = findRoleByRef(roleRef)
        ensurePermissionExists(permission)
        if (actorStorage.roleHasPermission(role.id, permission)) {
            throw RolePermissionAlreadyExistsException(role.id.asString(), permission.key)
        }
        actorStorage.addRolePermission(role.id, permission)
    }

    override fun deleteRolePermission(roleRef: RoleRef, permission: ActorPermission) {
        val role = findRoleByRef(roleRef)
        if (!actorStorage.roleHasPermission(role.id, permission)) {
            throw RolePermissionNotFoundException(role.id.asString(), permission.key)
        }
        actorStorage.deleteRolePermission(role.id, permission)
    }

    override fun deleteRole(roleRef: RoleRef) {
        val role = findRoleByRef(roleRef)
        actorStorage.deleteRole(role.id)
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

    override fun disable(actorId: ActorId, at: Instant?) {
        val existing = actorStorage.findById(actorId)
        if (at == null) actorStorage.enable(existing.id)
        else actorStorage.disable(existing.id, at)
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
