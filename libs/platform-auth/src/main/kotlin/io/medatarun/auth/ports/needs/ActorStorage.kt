package io.medatarun.auth.ports.needs

import io.medatarun.auth.domain.ActorNotFoundException
import io.medatarun.auth.domain.ActorPermission
import io.medatarun.auth.domain.RoleNotFoundByIdException
import io.medatarun.auth.domain.RoleNotFoundByKeyException
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


    fun findRoleByRefOptional(roleRef: RoleRef): Role? {
        return when (roleRef) {
            is RoleRef.ById -> findRoleByIdOptional(roleRef.id)
            is RoleRef.ByKey -> findRoleByKeyOptional(roleRef.key)
        }
    }

    fun findRoleByRef(roleRef: RoleRef): Role {
        return when (roleRef) {
            is RoleRef.ById -> findRoleById(roleRef.id)
            is RoleRef.ByKey -> findRoleByKey(roleRef.key)
        }
    }

    fun findRoleByIdOptional(roleId: RoleId): Role?

    fun findRoleById(roleId: RoleId): Role = findRoleByIdOptional(roleId) ?: throw RoleNotFoundByIdException(roleId)

    fun findRoleByKeyOptional(key: RoleKey): Role?

    fun findRoleByKey(roleKey: RoleKey): Role =
        findRoleByKeyOptional(roleKey) ?: throw RoleNotFoundByKeyException(roleKey)

    fun findRoleList(): List<Role>
    fun findRolePermissionList(roleId: RoleId): List<ActorPermission>

    /**
     * Returns true when [permission] already exists for [roleId].
     */
    fun roleHasPermission(roleId: RoleId, permission: ActorPermission): Boolean

    /**
     * Find an actor by [issuer] and [subject] (unique key).
     *
     * Returns null if not found
     */
    fun findActorByIssuerAndSubjectOptional(issuer: String, subject: String): Actor?

    /**
     * Find an actor by its unique identifier [id]
     *
     * Returns null if not found
     */
    fun findActorByIdOptional(id: ActorId): Actor?

    /**
     * Find an actor by its unique identifier [id].
     *
     * Throws an [ActorNotFoundException] if not found
     */
    fun findActorById(id: ActorId): Actor = findActorByIdOptional(id) ?: throw ActorNotFoundException()

    /**
     * List all known actors
     */
    fun findActorList(): List<Actor>

    /**
     * Lists all role ids affected to an actor
     */
    fun findActorRoleIdList(actorId: ActorId): Set<RoleId>

    /**
     * Finds the set of permission for this actor. It is the list of unique permissions found in all roles affected to this actor.
     */
    fun findActorPermissionSet(id: ActorId): Set<ActorPermission>

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    // Actors
    // ------

    /**
     * Inserts a new actor.
     *
     * Should raise an error (whatever error) if another actor exists with the same [id],
     * or the same couple ([issuer], [subject]).
     */
    fun actorCreate(
        id: ActorId,
        issuer: String,
        subject: String,
        fullname: String,
        email: String?,
        disabled: Instant?,
        createdAt: Instant,
        lastSeenAt: Instant
    )

    /**
     * Marks an actor disabled with this date.
     */
    fun actorDisable(id: ActorId, at: Instant)

    /**
     * Unmark "disabled" for an actor, making it enabled.
     */
    fun actorEnable(id: ActorId)

    /**
     * Updates an actor's profile information.
     */
    fun actorUpdateProfile(
        id: ActorId,
        fullname: String,
        email: String?,
        lastSeenAt: Instant
    )

    /**
     * Add the role to the actor
     */
    fun actorAddRole(actorId: ActorId, roleId: RoleId)

    /**
     * Remove the role from the actor
     */
    fun actorDeleteRole(actorId: ActorId, roleId: RoleId)

    // Roles
    // -----

    /**
     * Creates a new role.
     */
    fun roleCreate(
        id: RoleId,
        key: RoleKey,
        name: String,
        description: String?,
        createdAt: Instant,
        lastUpdatedAt: Instant
    )

    /**
     * Adds a permission to a role.
     */
    fun roleAddPermission(roleId: RoleId, permission: ActorPermission)

    /**
     * Deletes a permission from a role.
     */
    fun roleDeletePermission(roleId: RoleId, permission: ActorPermission)

    /**
     * Updates role name.
     */
    fun roleUpdateName(roleId: RoleId, name: String, lastUpdatedAt: Instant)

    /**
     * Updates role key.
     */
    fun roleUpdateKey(roleId: RoleId, key: RoleKey, lastUpdatedAt: Instant)

    /**
     * Updates role description.
     */
    fun roleUpdateDescription(roleId: RoleId, description: String?, lastUpdatedAt: Instant)

    /**
     * Deletes role permissions and role mappings, then the role itself.
     */
    fun roleDelete(roleId: RoleId)


}
