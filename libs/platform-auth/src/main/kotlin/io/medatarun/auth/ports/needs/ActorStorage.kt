package io.medatarun.auth.ports.needs

import io.medatarun.auth.domain.ActorNotFoundException
import io.medatarun.auth.domain.ActorPermission
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.domain.role.Role
import io.medatarun.auth.domain.role.RoleId
import io.medatarun.auth.domain.role.RoleKey
import io.medatarun.auth.domain.role.RoleRef
import java.time.Instant

/**
 * Interface used to provide storage of [Actor]
 */
interface ActorStorage {

    /**
     * Inserts a new actor.
     *
     * Should raise an error (whatever error) if another actor exists with the same [id],
     * or the same couple ([issuer], [subject]).
     */
    fun insert(
        id: ActorId,
        issuer: String,
        subject: String,
        fullname: String,
        email: String?,
        roles: List<ActorPermission>,
        disabled: Instant?,
        createdAt: Instant,
        lastSeenAt: Instant
    )

    /**
     * Updates an actor's profile information.
     */
    fun updateProfile(
        id: ActorId,
        fullname: String,
        email: String?,
        lastSeenAt: Instant
    )

    /**
     * Replaces roles for a given actor.
     */
    fun updateRoles(id: ActorId, roles: List<ActorPermission>)

    /**
     * Creates a new role.
     */
    fun createRole(
        id: RoleId,
        key: RoleKey,
        name: String,
        description: String?,
        createdAt: Instant,
        lastUpdatedAt: Instant
    )

    fun findRoleByRefOptional(roleRef: RoleRef): Role?
    fun findRoleByIdOptional(roleId: RoleId): Role?
    fun findRoleByKeyOptional(key: RoleKey): Role?
    fun listRoles(): List<Role>
    fun listRolePermissions(roleId: RoleId): List<ActorPermission>

    /**
     * Updates role name.
     */
    fun updateRoleName(roleId: RoleId, name: String, lastUpdatedAt: Instant)

    /**
     * Updates role key.
     */
    fun updateRoleKey(roleId: RoleId, key: RoleKey, lastUpdatedAt: Instant)

    /**
     * Updates role description.
     */
    fun updateRoleDescription(roleId: RoleId, description: String?, lastUpdatedAt: Instant)

    /**
     * Returns true when [permission] already exists for [roleId].
     */
    fun roleHasPermission(roleId: RoleId, permission: ActorPermission): Boolean

    /**
     * Adds a permission to a role.
     */
    fun addRolePermission(roleId: RoleId, permission: ActorPermission)

    /**
     * Deletes a permission from a role.
     */
    fun deleteRolePermission(roleId: RoleId, permission: ActorPermission)

    /**
     * Deletes role permissions and role mappings, then the role itself.
     */
    fun deleteRole(roleId: RoleId)

    /**
     * Marks an actor disabled with this date.
     */
    fun disable(id: ActorId, at: Instant)

    /**
     * Unmark "disabled" for an actor, making it enabled.
     */
    fun enable(id: ActorId)

    /**
     * Find an actor by [issuer] and [subject] (unique key).
     *
     * Returns null if not found
     */
    fun findByIssuerAndSubjectOptional(issuer: String, subject: String): Actor?

    /**
     * Find an actor by its unique identifier [id]
     *
     * Returns null if not found
     */
    fun findByIdOptional(id: ActorId): Actor?

    /**
     * Find an actor by its unique identifier [id].
     *
     * Throws an [ActorNotFoundException] if not found
     */
    fun findById(id: ActorId): Actor = findByIdOptional(id) ?: throw ActorNotFoundException()

    /**
     * List all known actors
     */
    fun listAll(): List<Actor>
}
