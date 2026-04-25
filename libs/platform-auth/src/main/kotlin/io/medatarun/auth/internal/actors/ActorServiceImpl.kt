package io.medatarun.auth.internal.actors

import io.medatarun.auth.adapters.ActorWithPermissionsInMemory
import io.medatarun.auth.domain.*
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.domain.actor.ActorWithPermissions
import io.medatarun.auth.domain.role.Role
import io.medatarun.auth.domain.role.RoleId
import io.medatarun.auth.domain.role.RoleKey
import io.medatarun.auth.domain.role.RoleRef
import io.medatarun.auth.internal.actors.ManagedRoles.Companion.ADMIN_ROLE_KEY
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
    private val permissionsRegistry: PermissionsRegistry,
    private val managedRoles: ManagedRoles
) : ActorService {


    override fun syncFromJwtExternalPrincipal(principal: AuthJwtExternalPrincipal): ActorWithPermissions {
        val existing = actorStorage.findActorByIssuerAndSubjectOptional(principal.issuer, principal.subject)

        val actor = if (existing == null) {
            val created = create(
                issuer = principal.issuer,
                subject = principal.subject,
                fullname = actorDisplayName(principal),
                email = principal.email,
                disabled = null
            )
            logger.info("Registered actor {} from issuer {}", created.id, created.issuer)
            created
        } else {
            updateActorProfile(existing, principal)
        }
        val permissionSet = actorStorage.findActorPermissionSet(actor.id)
        return ActorWithPermissionsInMemory(actor, permissionSet)
    }

    override fun findByIssuerAndSubjectWithPermissionsOptional(
        issuer: String,
        subject: String
    ): ActorWithPermissions? {
        val found = findByIssuerAndSubjectOptional(issuer, subject) ?: return null
        val permissionSet = actorStorage.findActorPermissionSet(found.id)
        return ActorWithPermissionsInMemory(found, permissionSet)
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
        disabled: Instant?
    ): Actor {
        val id = ActorId.generate()
        actorStorage.actorCreate(
            id = id,
            issuer = issuer,
            subject = subject,
            fullname = fullname,
            email = email,
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

    override fun listRoles(): List<Role> {
        return actorStorage.findRoleList()
    }

    override fun findRoleByRef(roleRef: RoleRef): Role {
        return actorStorage.findRoleByRef(roleRef)
    }

    override fun findRoleByRefOptional(roleRef: RoleRef): Role? {
        return actorStorage.findRoleByRefOptional(roleRef)
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
        // Special admin role: no you can not create another role with this key
        if (managedRoles.isManagedRole(key)) throw RoleCreateConflictsWithManagedKeyException(key)
        if (actorStorage.findRoleByKeyOptional(key) != null) {
            throw RoleAlreadyExistsException(key.value)
        }
        val now = clock.now()
        val roleId = RoleId.generate()
        actorStorage.roleCreate(roleId, key, name, description, false, now, now)
        return roleId
    }

    override fun updateRoleName(roleRef: RoleRef, name: String) {
        // Special admin role: yes, you can rename it
        val role = actorStorage.findRoleByRef(roleRef)
        actorStorage.roleUpdateName(role.id, name, clock.now())
    }

    override fun updateRoleKey(roleRef: RoleRef, key: RoleKey) {
        // Special admin role: no, you cannot rename the key
        if (managedRoles.isManagedRole(key)) throw RoleUpdateKeyConflictsWithManagedKeyException(key)
        val role = actorStorage.findRoleByRef(roleRef)
        val existingRole = actorStorage.findRoleByKeyOptional(key)
        if (existingRole != null && existingRole.id != role.id) {
            throw RoleAlreadyExistsException(key.value)
        }
        actorStorage.roleUpdateKey(role.id, key, clock.now())
    }

    override fun updateRoleDescription(roleRef: RoleRef, description: String?) {
        // Special admin role: yes, you can change description
        val role = actorStorage.findRoleByRef(roleRef)
        actorStorage.roleUpdateDescription(role.id, description, clock.now())
    }

    override fun roleUpdateAutoAssign(roleRef: RoleRef, value: Boolean) {
        val role = actorStorage.findRoleByRef(roleRef)
        val now = clock.now()
        val existing = actorStorage.findRoleAutoAssignOptional()

        if (existing != null && existing.id != role.id) {
            actorStorage.roleUpdateAutoAssign(existing.id, false, now)
        }
        actorStorage.roleUpdateAutoAssign(role.id, value, now)
    }

    override fun addRolePermission(roleRef: RoleRef, permission: ActorPermission) {
        // Special admin role: no, you cannot add permissions from here
        val role = actorStorage.findRoleByRef(roleRef)
        if (managedRoles.isManagedRole(role.key)) throw RoleUpdatePermissionsManagedRoleException(role.key)
        if (!permissionsRegistry.isKnownPermission(permission)) throw AuthUnknownPermissionException(permission.key)
        if (actorStorage.roleHasPermission(role.id, permission)) {
            throw RolePermissionAlreadyExistsException(role.id.asString(), permission.key)
        }
        actorStorage.roleAddPermission(role.id, permission)
    }

    override fun deleteRolePermission(roleRef: RoleRef, permission: ActorPermission) {
        // Special admin role: no, you cannot remove permissions from here
        val role = actorStorage.findRoleByRef(roleRef)
        if (managedRoles.isManagedRole(role.key)) throw RoleUpdatePermissionsManagedRoleException(role.key)
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

    override fun findActorPermissionSet(id: ActorId): Set<ActorPermission> {
        return actorStorage.findActorPermissionSet(id)
    }

    override fun findActorRoleIdSet(actorId: ActorId): Set<RoleId> {
        return actorStorage.findActorRoleIdList(actorId)
    }

    override fun actorHasRole(actorId: ActorId, roleId: RoleId): Boolean {
        return actorStorage.findActorRoleIdList(actorId).contains(roleId)
    }

    override fun findSpecialAdminRole(): Role {
        return actorStorage.findRoleByKeyOptional(ADMIN_ROLE_KEY)
            ?: throw RoleNotFoundByKeyException(ADMIN_ROLE_KEY)
    }

    override fun syncManagedRoles() {
        for (managedRole in managedRoles.findManagedRolesWithPermissions()) {

            // Find matching role or create it if it doesn't exist (by key)
            val found = actorStorage.findRoleByKeyOptional(managedRole.role.key)
            val autoAssignRole = actorStorage.findRoleAutoAssignOptional()
            val roleSafe = if (found == null) {
                actorStorage.roleCreate(
                    id = managedRole.role.id,
                    key = managedRole.role.key,
                    name = managedRole.role.name,
                    description = managedRole.role.description,
                    // If no other role has autoassign and this managed role has autoassign,
                    // because we create the new role, we add it. But if another role already
                    // has it, we don't touch it. Anyway this should only happend at installation
                    // time, so there should not be conflicts with other roles.
                    // This is to ensure that onboarding the application is smooth with already a
                    // role autoassign present to help admins create users faster.
                    autoAssign = if (autoAssignRole == null) managedRole.role.autoAssign else false,
                    createdAt = managedRole.role.createdAt,
                    lastUpdatedAt = managedRole.role.createdAt
                )
                actorStorage.findRoleById(managedRole.role.id)
            } else if (
                found.createdAt != managedRole.role.createdAt
                || found.lastUpdatedAt != managedRole.role.lastUpdatedAt
                || found.name != managedRole.role.name
                || found.description != managedRole.role.description
            ) {
                actorStorage.roleManagedReplace(
                    key = managedRole.role.key,
                    name = managedRole.role.name,
                    description = managedRole.role.description,
                    createdAt = managedRole.role.createdAt,
                    lastUpdatedAt = managedRole.role.createdAt
                )
                actorStorage.findRoleById(managedRole.role.id)
            } else found

            // Remove permissions that may exist in storage but not in the
            // current managed role definition
            val existingPermissions = actorStorage.findRolePermissionList(roleSafe.id)
            for (existingPermission in existingPermissions) {
                if (!managedRole.permissionKeys.contains(existingPermission)) {
                    actorStorage.roleDeletePermission(roleSafe.id, existingPermission)
                }
            }

            // Insert missing permissions
            for (permission in managedRole.permissionKeys) {
                if (!existingPermissions.contains(permission)) {
                    actorStorage.roleAddPermission(roleSafe.id, permission)
                }
            }

        }
    }

    override fun isManagedRole(key: RoleKey): Boolean {
        return managedRoles.isManagedRole(key)
    }

    override fun deleteRole(roleRef: RoleRef) {
        val role = actorStorage.findRoleByRef(roleRef)
        if (managedRoles.isManagedRole(role.key)) throw RoleDeleteManagedForbiddenException(role.key)
        actorStorage.roleDelete(role.id)
    }

    override fun actorDisable(actorId: ActorId, at: Instant?) {
        val existing = actorStorage.findActorById(actorId)
        if (at == null) actorStorage.actorEnable(existing.id)
        else actorStorage.actorDisable(existing.id, at)
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
